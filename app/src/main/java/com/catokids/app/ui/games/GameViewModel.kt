package com.catokids.app.ui.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.catokids.app.core.AppContainer
import com.catokids.app.data.curriculum.CatoCurriculum
import com.catokids.app.data.model.CUSTOM_GAME_LESSON_PREFIX
import com.catokids.app.data.model.GameRound
import com.catokids.app.data.model.Lesson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class Feedback { NONE, CORRECT, WRONG }

data class GameUiState(
    val lesson: Lesson? = null,
    /** True only while a teacher-authored custom game is still being fetched. */
    val loading: Boolean = false,
    val roundIndex: Int = 0,
    val correct: Int = 0,
    val wrong: Int = 0,
    val feedback: Feedback = Feedback.NONE,
    val explanation: String? = null,
    val finished: Boolean = false,
    val startedAtMillis: Long = System.currentTimeMillis(),
    /** Toy games let a child keep trying; these count the tries for grown-ups' reports. */
    val misses: Int = 0,
    val roundMisses: Int = 0,
    val firstTry: Int = 0,
) {
    val round: GameRound? get() = lesson?.content?.rounds?.getOrNull(roundIndex)
    val totalRounds: Int get() = lesson?.content?.rounds?.size ?: 0
    val progress: Float get() = if (totalRounds == 0) 0f else roundIndex.toFloat() / totalRounds
    private val toy: Boolean get() = lesson?.grade == com.catokids.app.data.model.Grade.PREKG
    /**
     * Pre-KG toys always finish with the right answer, so their score counts the wrong tries
     * on the way; reports also get "right first time" through [firstTry].
     */
    val score: Int get() = when {
        totalRounds == 0 -> 0
        // Each wrong try costs a quarter of a round, and a round never drops below half:
        // a 3-year-old who slips once and then gets it still did well.
        toy -> (totalRounds * 4 - misses.coerceAtMost(totalRounds * 2)) * 25 / totalRounds
        else -> correct * 100 / totalRounds
    }
    val stars: Int get() = when {
        totalRounds == 0 -> 0
        toy && score < 40 -> 1           // a little one always earns a star for finishing
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

    private val isCustom = lessonId.startsWith(CUSTOM_GAME_LESSON_PREFIX)

    private val _state = MutableStateFlow(
        if (isCustom) GameUiState(loading = true) else GameUiState(lesson = CatoCurriculum.lesson(lessonId))
    )
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    init {
        if (isCustom) {
            viewModelScope.launch {
                val game = container.content.game(lessonId.removePrefix(CUSTOM_GAME_LESSON_PREFIX))
                _state.value = _state.value.copy(
                    lesson = game?.toLesson(),
                    loading = false,
                    startedAtMillis = System.currentTimeMillis(),
                )
                speakCurrentPrompt()
            }
        } else {
            speakCurrentPrompt()
        }
    }

    fun speakCurrentPrompt() {
        val s = _state.value
        val lesson = s.lesson; val round = s.round
        // Pre-KG toys say their own tiny line ("Feed me A!") — never the lesson's long prompt.
        val text = if (lesson != null && round != null && lesson.grade == com.catokids.app.data.model.Grade.PREKG)
            toySpeech(lesson, round)
        else round?.speak ?: round?.prompt ?: lesson?.content?.speak
        container.speech.speak(text)
    }

    fun speak(text: String?) = container.speech.speak(text)

    /** Called by every game when the child finishes a round. */
    fun submit(isCorrect: Boolean) {
        val s = _state.value
        if (s.feedback != Feedback.NONE) return
        // Pre-KG gets a rising chime and a cheer that changes each time, so praise
        // stays fresh for the youngest children; older grades keep the plain beep.
        val playful = s.lesson?.grade == com.catokids.app.data.model.Grade.PREKG
        when {
            isCorrect && playful -> container.sounds.chime()
            isCorrect            -> container.sounds.correct()
            else                 -> container.sounds.wrong()
        }
        _state.value = s.copy(
            correct = s.correct + if (isCorrect) 1 else 0,
            wrong = s.wrong + if (isCorrect) 0 else 1,
            firstTry = s.firstTry + if (isCorrect && s.roundMisses == 0) 1 else 0,
            feedback = if (isCorrect) Feedback.CORRECT else Feedback.WRONG,
            explanation = s.round?.explanation,
        )
        container.speech.speak(
            when {
                isCorrect && playful -> PRAISE[(s.correct + s.roundIndex) % PRAISE.size]
                isCorrect            -> "Well done!"
                playful              -> "Oops! Try again!"
                else                 -> "Not quite. Try the next one."
            }
        )
    }

    fun next() {
        val s = _state.value
        val isLast = s.roundIndex >= s.totalRounds - 1
        if (isLast) {
            _state.value = s.copy(feedback = Feedback.NONE, finished = true)
            container.sounds.celebrate()
            persist()
        } else {
            _state.value = s.copy(roundIndex = s.roundIndex + 1, feedback = Feedback.NONE, explanation = null, roundMisses = 0)
            speakCurrentPrompt()
        }
    }

    /** A toy game's wrong move: no buzzer, no score change for the child — just counted. */
    fun miss() {
        val s = _state.value
        _state.value = s.copy(misses = s.misses + 1, roundMisses = s.roundMisses + 1)
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
                // For toys this is "rounds right first time", so reports see real wrong tries.
                correct = if (s.lesson?.grade == com.catokids.app.data.model.Grade.PREKG) s.firstTry else s.correct,
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

/** Pre-KG cheers, rotated so the same words do not repeat round after round. */
private val PRAISE = listOf("Yay!", "Good job!", "Super!", "Wow!", "Hooray!")
