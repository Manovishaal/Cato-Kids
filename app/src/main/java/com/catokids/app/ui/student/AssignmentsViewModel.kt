package com.catokids.app.ui.student

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

/** One assignment plus everything needed to show its status on the child's list. */
data class StudentAssignmentStatus(
    val assignment: Assignment,
    val submission: AssignmentSubmission?,
    val gameCompleted: Boolean,
    val course: ExtraCourse? = null,
) {
    val isDone: Boolean
        get() = when {
            assignment.customGameId != null -> gameCompleted
            else -> submission != null
        }

    val isReviewed: Boolean get() = submission?.status == SubmissionStatus.REVIEWED
}

data class AssignmentsUiState(
    val profile: Profile? = null,
    val items: List<StudentAssignmentStatus> = emptyList(),
    val loading: Boolean = true,
) {
    val pending: List<StudentAssignmentStatus> get() = items.filterNot { it.isDone }
    val done: List<StudentAssignmentStatus> get() = items.filter { it.isDone }
}

class AssignmentsViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow(AssignmentsUiState())
    val state: StateFlow<AssignmentsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            container.auth.profile.collect { profile ->
                if (profile == null) {
                    _state.value = AssignmentsUiState(loading = false)
                } else {
                    _state.value = _state.value.copy(profile = profile, loading = true)
                    load(profile)
                }
            }
        }
    }

    private suspend fun load(profile: Profile) {
        val assignments = container.assignments.myAssignments().filter { it.type != AssignmentType.LESSON }
        val submissions = container.assignments.mySubmissions().associateBy { it.assignmentId }
        val progress = container.progress.snapshot(profile.id)
        val items = assignments.map { a ->
            val gameCompleted = a.customGameId?.let { gid ->
                progress.any { it.lessonId == "$CUSTOM_GAME_LESSON_PREFIX$gid" && it.completed }
            } ?: false
            val course = a.courseId?.let { container.content.course(it) }
            StudentAssignmentStatus(
                assignment = a, submission = submissions[a.id], gameCompleted = gameCompleted, course = course,
            )
        }
        _state.value = _state.value.copy(items = items, loading = false)
    }

    fun refresh() {
        val profile = _state.value.profile ?: return
        viewModelScope.launch { load(profile) }
    }

    fun find(assignmentId: String): StudentAssignmentStatus? =
        _state.value.items.firstOrNull { it.assignment.id == assignmentId }

    suspend fun submit(assignmentId: String, answer: String): CatoResult<AssignmentSubmission> {
        val result = container.assignments.submit(assignmentId, answer)
        if (result.isOk) {
            container.auth.addRewards(stars = 0, coins = find(assignmentId)?.assignment?.pointsReward ?: 0)
            refresh()
        }
        return result
    }

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = AssignmentsViewModel(container) as T
        }
    }
}
