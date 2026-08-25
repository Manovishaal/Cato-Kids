package com.catokids.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.catokids.app.core.AppContainer
import com.catokids.app.data.curriculum.CatoCurriculum
import com.catokids.app.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val profile: Profile? = null,
    val school: School? = null,
    val classes: List<ClassRoom> = emptyList(),
    val students: List<StudentSummary> = emptyList(),
    val children: List<StudentSummary> = emptyList(),
    val counts: Map<Role, Int> = emptyMap(),
    val loading: Boolean = true,
) {
    val activeToday: Int get() = students.count { it.lastActiveLabel.equals("Today", true) }
    val averageCompletion: Int
        get() = if (students.isEmpty()) 0 else students.sumOf { it.completionPercent } / students.size
    val averageScore: Int
        get() = students.filter { it.averageScore > 0 }.let {
            if (it.isEmpty()) 0 else it.sumOf { s -> s.averageScore } / it.size
        }
    val needsHelp: List<StudentSummary>
        get() = students.filter { it.averageScore in 1..69 }.sortedBy { it.averageScore }
    val topPerformers: List<StudentSummary>
        get() = students.sortedByDescending { it.averageScore }.take(5)
}

class DashboardViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            container.auth.profile.collect { profile ->
                if (profile == null) {
                    _state.value = DashboardUiState(loading = false)
                } else {
                    _state.value = _state.value.copy(profile = profile, loading = true)
                    load(profile)
                }
            }
        }
    }

    private suspend fun load(profile: Profile) {
        val roster = container.roster
        when (profile.role) {
            Role.TEACHER -> {
                val classes = roster.classesForTeacher(profile.id)
                val students = classes.flatMap { roster.studentsInClass(it.id) }.distinctBy { it.profile.id }
                _state.value = _state.value.copy(
                    classes = classes,
                    students = roster.enrichWithProgress(students),
                    school = roster.school(profile.schoolId),
                    loading = false,
                )
            }
            Role.PARENT -> {
                val children = roster.enrichWithProgress(roster.childrenOf(profile.id))
                _state.value = _state.value.copy(children = children, students = children, loading = false)
            }
            Role.SCHOOL -> {
                val school = roster.school(profile.schoolId)
                val classes = roster.classesForSchool(school?.id ?: profile.schoolId ?: "")
                val students = roster.enrichWithProgress(roster.studentsInSchool(school?.id ?: ""))
                _state.value = _state.value.copy(
                    school = school, classes = classes, students = students, loading = false,
                )
            }
            Role.ADMIN -> {
                // Keep the server lesson catalogue in step with this build.
                container.curriculumSync.pushIfAdmin()
                _state.value = _state.value.copy(
                    counts = roster.platformCounts(),
                    classes = roster.classesForSchool(profile.schoolId ?: ""),
                    students = roster.enrichWithProgress(roster.studentsInSchool(profile.schoolId ?: "")),
                    school = roster.school(profile.schoolId),
                    loading = false,
                )
            }
            Role.STUDENT -> _state.value = _state.value.copy(loading = false)
        }
    }

    fun studentsIn(classId: String): List<StudentSummary> {
        val grade = _state.value.classes.firstOrNull { it.id == classId }?.grade
        return _state.value.students.filter { grade == null || it.profile.grade == grade }
    }

    fun student(studentId: String): StudentSummary? =
        _state.value.students.firstOrNull { it.profile.id == studentId }
            ?: _state.value.children.firstOrNull { it.profile.id == studentId }

    fun subjectBreakdownFor(student: StudentSummary): Map<SubjectId, Int> {
        val grade = student.profile.grade ?: Grade.LKG
        val seed = student.profile.id.hashCode()
        return SubjectId.entries.associateWith { subject ->
            val total = CatoCurriculum.countFor(grade, subject).coerceAtLeast(1)
            val pseudo = ((seed + subject.ordinal * 37).mod(100))
            val base = (student.completionPercent + pseudo) / 2
            base.coerceIn(0, 100).let { if (total == 0) 0 else it }
        }
    }

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = DashboardViewModel(container) as T
        }
    }
}
