package com.catokids.app.ui.creator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.catokids.app.core.AppContainer
import com.catokids.app.core.CatoResult
import com.catokids.app.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CreatorUiState(
    val profile: Profile? = null,
    val classes: List<ClassRoom> = emptyList(),
    val games: List<CustomGame> = emptyList(),
    val courses: List<ExtraCourse> = emptyList(),
    val activities: List<Activity> = emptyList(),
    /** Homework / activity / course assignments this creator has set across their classes. */
    val assignedWork: List<Assignment> = emptyList(),
    val loading: Boolean = true,
) {
    /** Only teachers, schools and administrators ever reach these screens — enforced in navigation too. */
    val allowed: Boolean get() = profile?.role in setOf(Role.TEACHER, Role.SCHOOL, Role.ADMIN)
}

/**
 * Backs every screen under `creator/...`: the hub, the homework/activity/course/game
 * composers, and the submissions review list. One view model for the whole flow
 * because they all share the same class list and the same "my content" library.
 */
class CreatorViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow(CreatorUiState())
    val state: StateFlow<CreatorUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            container.auth.profile.collect { profile ->
                if (profile == null) {
                    _state.value = CreatorUiState(loading = false)
                } else {
                    _state.value = _state.value.copy(profile = profile, loading = true)
                    load(profile)
                }
            }
        }
    }

    private suspend fun load(profile: Profile) {
        val classes = when (profile.role) {
            Role.TEACHER -> container.roster.classesForTeacher(profile.id)
            Role.SCHOOL, Role.ADMIN -> container.roster.classesForSchool(profile.schoolId ?: "")
            else -> emptyList()
        }
        val games = container.content.myGames()
        val courses = container.content.myCourses()
        val activities = container.content.myActivities()
        val assignedWork = classes
            .flatMap { container.assignments.forClass(it.id) }
            .filter { it.type != AssignmentType.LESSON }
            .distinctBy { it.id }
        _state.value = _state.value.copy(
            classes = classes,
            games = games,
            courses = courses,
            activities = activities,
            assignedWork = assignedWork,
            loading = false,
        )
    }

    fun refresh() {
        val profile = _state.value.profile ?: return
        viewModelScope.launch { load(profile) }
    }

    // ---------- library: build once, assign to any class later ----------

    suspend fun saveGame(game: CustomGame): CatoResult<CustomGame> {
        val result = container.content.saveGame(game)
        if (result.isOk) refresh()
        return result
    }

    suspend fun saveCourse(course: ExtraCourse): CatoResult<ExtraCourse> {
        val result = container.content.saveCourse(course)
        if (result.isOk) refresh()
        return result
    }

    suspend fun saveActivity(activity: Activity): CatoResult<Activity> {
        val result = container.content.saveActivity(activity)
        if (result.isOk) refresh()
        return result
    }

    fun deleteGame(id: String) = viewModelScope.launch { container.content.deleteGame(id); refresh() }
    fun deleteCourse(id: String) = viewModelScope.launch { container.content.deleteCourse(id); refresh() }
    fun deleteActivity(id: String) = viewModelScope.launch { container.content.deleteActivity(id); refresh() }
    fun deleteAssignment(id: String) = viewModelScope.launch { container.assignments.delete(id); refresh() }

    fun togglePublishGame(game: CustomGame) = viewModelScope.launch {
        container.content.saveGame(game.copy(published = !game.published)); refresh()
    }
    fun togglePublishCourse(course: ExtraCourse) = viewModelScope.launch {
        container.content.saveCourse(course.copy(published = !course.published)); refresh()
    }
    fun togglePublishActivity(activity: Activity) = viewModelScope.launch {
        container.content.saveActivity(activity.copy(published = !activity.published)); refresh()
    }

    // ---------- putting content (or freeform homework) in front of a class ----------

    suspend fun assign(assignment: Assignment): CatoResult<Assignment> {
        val result = container.assignments.create(assignment)
        if (result.isOk) refresh()
        return result
    }

    // ---------- grading ----------

    suspend fun submissionsFor(assignmentId: String) = container.assignments.submissionsFor(assignmentId)

    suspend fun review(submissionId: String, status: SubmissionStatus, score: Int?, feedback: String?) =
        container.assignments.review(submissionId, status, score, feedback)

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = CreatorViewModel(container) as T
        }
    }
}
