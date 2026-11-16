package com.catokids.app.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.catokids.app.core.AppContainer
import com.catokids.app.data.curriculum.CatoCurriculum
import com.catokids.app.data.model.*
import com.catokids.app.data.repository.ProgressSummary
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class StudentUiState(
    val profile: Profile? = null,
    val grade: Grade = Grade.LKG,
    val progress: List<LessonProgress> = emptyList(),
    val summary: ProgressSummary = ProgressSummary(),
    val bySubject: Map<SubjectId, ProgressSummary> = emptyMap(),
    val loading: Boolean = true,
    /** Explore (offline demo) mode — the only place a child may switch class themselves. */
    val demo: Boolean = false,
) {
    val nextLesson: Lesson?
        get() {
            val done = progress.filter { it.completed }.map { it.lessonId }.toSet()
            return CatoCurriculum.forGrade(grade).firstOrNull { it.id !in done }
        }

    /** The most recently played lesson in this class that isn't finished yet. */
    val continueLesson: Lesson?
        get() {
            val mine = CatoCurriculum.forGrade(grade).map { it.id }.toSet()
            return progress.filter { it.lessonId in mine && !it.completed }
                .maxByOrNull { it.lastPlayedAtMillis }
                ?.let { CatoCurriculum.lesson(it.lessonId) }
        }

    /**
     * One unlock rule for everywhere a lesson can be opened: the first two lessons of a
     * book are always open; after that a lesson opens once the one before it is done.
     */
    fun isLocked(lesson: Lesson): Boolean {
        val book = CatoCurriculum.forGradeAndSubject(lesson.grade, lesson.subject)
        val i = book.indexOfFirst { it.id == lesson.id }
        return i > 1 && !isDone(lesson.id) && !isDone(book[i - 1].id)
    }

    fun starsFor(lessonId: String): Int = progress.firstOrNull { it.lessonId == lessonId }?.stars ?: 0
    fun isDone(lessonId: String): Boolean = progress.any { it.lessonId == lessonId && it.completed }
}

class StudentViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow(StudentUiState())
    val state: StateFlow<StudentUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            container.auth.isDemo.collect { _state.value = _state.value.copy(demo = it) }
        }
        viewModelScope.launch {
            container.auth.profile.collect { profile ->
                if (profile == null) {
                    _state.value = StudentUiState(loading = false)
                    return@collect
                }
                val grade = profile.grade ?: Grade.LKG
                _state.value = _state.value.copy(
                    profile = profile, grade = grade, loading = false,
                )
                observeProgress(profile.id, grade)
            }
        }
    }

    private var observing: String? = null

    private fun observeProgress(userId: String, grade: Grade) {
        if (observing == userId) return
        observing = userId
        viewModelScope.launch {
            container.progress.observe(userId).collect { rows ->
                _state.value = _state.value.copy(
                    progress = rows,
                    summary = container.progress.summaryFor(rows, grade),
                    bySubject = container.progress.subjectBreakdown(rows, grade),
                )
            }
        }
    }

    fun setGrade(grade: Grade) {
        _state.value = _state.value.copy(grade = grade)
        viewModelScope.launch {
            container.auth.updateProfile(grade = grade)
            val rows = _state.value.progress
            _state.value = _state.value.copy(
                summary = container.progress.summaryFor(rows, grade),
                bySubject = container.progress.subjectBreakdown(rows, grade),
            )
        }
    }

    fun lessonsFor(subject: SubjectId): List<Lesson> =
        CatoCurriculum.forGradeAndSubject(_state.value.grade, subject)

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = StudentViewModel(container) as T
        }
    }
}
