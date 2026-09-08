package com.catokids.app.ui.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.catokids.app.core.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Cato's Toy Box — a little shape-sorting game built around the six 3D toy models
 * (rocket, treasure chest, alphabet block, Cato himself, a coin token, a shape sorter).
 * Five quick rounds, a chest to open, a rocket launch — every model gets a turn.
 */
enum class ToyBoxPhase { INTRO, ROUND, CHEST, ROCKET }

enum class ToyAnswerFeedback { NONE, CORRECT, WRONG }

/** A single answer tile. [emoji] renders as a picture; when it's null, [label] is shown as text (for letters). */
data class ToyChoice(val label: String, val emoji: String? = null)

data class ToyBoxRound(
    val modelAsset: String,
    val prompt: String,
    val choices: List<ToyChoice>,
    val correctIndex: Int,
    /** When set, only this one part of the model is drawn — e.g. just "peg_cube" out of the sorter. */
    val spotlightPart: String? = null,
)

object ToyBoxRounds {
    val ALL = listOf(
        ToyBoxRound(
            modelAsset = "sorter",
            spotlightPart = "peg_cylinder",
            prompt = "Spin this peg — it rolls! Which hole matches its shape?",
            choices = listOf(
                ToyChoice("Circle", "⚪"),
                ToyChoice("Square", "🟧"),
                ToyChoice("Triangle", "🔺"),
                ToyChoice("Star", "⭐"),
            ),
            correctIndex = 0,
        ),
        ToyBoxRound(
            modelAsset = "sorter",
            spotlightPart = "peg_cube",
            prompt = "This peg has flat sides and corners. Which hole matches?",
            choices = listOf(
                ToyChoice("Triangle", "🔺"),
                ToyChoice("Square", "🟧"),
                ToyChoice("Star", "⭐"),
                ToyChoice("Circle", "⚪"),
            ),
            correctIndex = 1,
        ),
        ToyBoxRound(
            modelAsset = "sorter",
            spotlightPart = "peg_prism",
            prompt = "Count this peg's sides. Which hole is the same shape?",
            choices = listOf(
                ToyChoice("Square", "🟧"),
                ToyChoice("Circle", "⚪"),
                ToyChoice("Triangle", "🔺"),
                ToyChoice("Star", "⭐"),
            ),
            correctIndex = 2,
        ),
        ToyBoxRound(
            modelAsset = "block",
            prompt = "Spin Cato's wooden block all the way round. What letter is hiding on it?",
            choices = listOf(
                ToyChoice("B"),
                ToyChoice("A"),
                ToyChoice("C"),
                ToyChoice("D"),
            ),
            correctIndex = 1,
        ),
        ToyBoxRound(
            modelAsset = "token",
            prompt = "Look closely at the middle of the coin. What shape shines there?",
            choices = listOf(
                ToyChoice("Moon", "🌙"),
                ToyChoice("Heart", "❤️"),
                ToyChoice("Sun", "☀️"),
                ToyChoice("Star", "⭐"),
            ),
            correctIndex = 3,
        ),
    )
}

data class ToyBoxUiState(
    val phase: ToyBoxPhase = ToyBoxPhase.INTRO,
    val roundIndex: Int = 0,
    val feedback: ToyAnswerFeedback = ToyAnswerFeedback.NONE,
    val selectedIndex: Int? = null,
    val coinsEarned: Int = 0,
    val chestOpened: Boolean = false,
) {
    val round: ToyBoxRound? get() = ToyBoxRounds.ALL.getOrNull(roundIndex)
    val totalRounds: Int get() = ToyBoxRounds.ALL.size
}

class ToyBoxViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow(ToyBoxUiState())
    val state: StateFlow<ToyBoxUiState> = _state.asStateFlow()

    fun start() {
        _state.update { it.copy(phase = ToyBoxPhase.ROUND) }
    }

    /** No wrong-answer penalty — a small child just gets to try the next tile. */
    fun choose(index: Int) {
        val current = _state.value
        val round = current.round ?: return
        if (current.feedback == ToyAnswerFeedback.CORRECT) return
        if (index == round.correctIndex) {
            viewModelScope.launch { runCatching { container.auth.addRewards(stars = 0, coins = 5) } }
            _state.update { it.copy(feedback = ToyAnswerFeedback.CORRECT, selectedIndex = index, coinsEarned = it.coinsEarned + 5) }
        } else {
            _state.update { it.copy(feedback = ToyAnswerFeedback.WRONG, selectedIndex = index) }
        }
    }

    fun clearWrongFeedback() {
        _state.update {
            if (it.feedback == ToyAnswerFeedback.WRONG) it.copy(feedback = ToyAnswerFeedback.NONE, selectedIndex = null) else it
        }
    }

    fun nextRound() {
        val current = _state.value
        val next = current.roundIndex + 1
        _state.update {
            if (next >= it.totalRounds) {
                it.copy(phase = ToyBoxPhase.CHEST, feedback = ToyAnswerFeedback.NONE, selectedIndex = null)
            } else {
                it.copy(roundIndex = next, feedback = ToyAnswerFeedback.NONE, selectedIndex = null)
            }
        }
    }

    fun openChest() {
        if (_state.value.chestOpened) return
        viewModelScope.launch { runCatching { container.auth.addRewards(stars = 1, coins = 20) } }
        _state.update { it.copy(chestOpened = true, coinsEarned = it.coinsEarned + 20) }
    }

    fun launchRocket() {
        _state.update { it.copy(phase = ToyBoxPhase.ROCKET) }
    }

    fun playAgain() {
        _state.value = ToyBoxUiState(phase = ToyBoxPhase.ROUND)
    }

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = ToyBoxViewModel(container) as T
        }
    }
}
