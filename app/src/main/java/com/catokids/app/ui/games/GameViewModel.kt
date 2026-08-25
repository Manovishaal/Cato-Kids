package com.catokids.app.ui.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.catokids.app.core.AppContainer
import com.catokids.app.data.curriculum.CatoCurriculum
import com.catokids.app.data.model.GameRound
import com.catokids.app.data.model.Lesson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class Feedback { NONE, CORRECT, WRONG }

data class GameUiState(
    val lesson: Lesson? = null,
    val roundIndex: Int = 0,
    val correct: Int = 0,
    val wrong: Int = 0,
    val feedback: Feedback = Feedback.NONE,
    val explanation: String? = null,
    val finished: Boolean = false,
    val startedAtMillis: Long = System.currentTimeMillis(),
) {
    val round: GameRound? get() = lesson?.content?.rounds?.getOrNull(roundIndex)
    val totalRounds: Int get() = lesson?.content?.rounds?.size ?: 0
    val progress: Float get() = if (totalRounds == 0) 0f else roundIndex.toFloat() / totalRounds
    val score: Int get() = if (totalRounds == 0) 0 else (correct * 100) / totalRounds
    val stars: Int get() = when {
        totalRounds == 0 -> 0
        score >= 90 -> 3
        score >= 70 -> 2
        score >= 40 -> 1
        else -> 0
    }
    val elapsedSeconds: Int get() = ((System.currentTimeMillis() - startedAtMillis) / 1000).toInt()
}

class GameViewModel(
    private val container: AppContainer,
    lessonId: String,
) : ViewModel() {

    private val _state = MutableStateFlow(GameUiState(lesson = CatoCurriculum.lesson(lessonId)))
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    init {
        speakCurrentPrompt()
    }

    fun speakCurrentPrompt() {
        val s = _state.value
        val text = s.round?.speak ?: s.round?.prompt ?: s.lesson?.content?.speak
        container.speech.speak(text)
    }

    fun speak(text: String?) = container.speech.speak(text)

    /** Called by every game when the child finishes a round. */
    fun submit(isCorrect: Boolean) {
        val s = _state.value
        if (s.feedback != Feedback.NONE) return
        if (isCorrect) container.sounds.correct() else container.sounds.wrong()
        _state.value = s.copy(
            correct = s.correct + if (isCorrect) 1 else 0,
            wrong = s.wrong + if (isCorrect) 0 else 1,
            feedback = if (isCorrect) Feedback.CORRECT else Feedback.WRONG,
            explanation = s.round?.explanation,
        )
        container.speech.speak(if (isCorrect) "Well done!" else "Not quite. Try the next one.")
    }

    fun next() {
        val s = _state.value
        val isLast = s.roundIndex >= s.totalRounds - 1
        if (isLast) {
            _state.value = s.copy(feedback = Feedback.NONE, finished = true)
            container.sounds.celebrate()
            persist()
        } else {
            _state.value = s.copy(roundIndex = s.roundIndex + 1, feedback = Feedback.NONE, explanation = null)
            speakCurrentPrompt()
        }
    }

    fun retryRound() {
        _state.value = _state.value.copy(feedback = Feedback.NONE, explanation = null)
        speakCurrentPrompt()
    }

    fun restart() {
        _state.value = GameUiState(lesson = _state.value.lesson)
        speakCurrentPrompt()
    }

    private fun persist() {
        val s = _state.value
        val lesson = s.lesson ?: return
        val userId = container.auth.profile.value?.id ?: return
        viewModelScope.launch {
            container.progress.recordResult(
                userId = userId,
                lessonId = lesson.id,
                stars = s.stars,
                score = s.score,
                correct = s.correct,
                total = s.totalRounds,
                seconds = s.elapsedSeconds,
            )
        }
    }

    fun tapSound() = container.sounds.tap()

    override fun onCleared() {
        container.speech.stop()
        super.onCleared()
    }

    companion object {
        fun factory(container: AppContainer, lessonId: String) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                GameViewModel(container, lessonId) as T
        }
    }
}
