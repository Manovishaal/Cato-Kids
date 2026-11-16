package com.catokids.app

import com.catokids.app.data.curriculum.CatoCurriculum
import com.catokids.app.data.model.Grade
import com.catokids.app.ui.games.GameUiState
import org.junit.Assert.assertEquals
import org.junit.Test

class ScoringTest {

    private fun stateWith(correct: Int, total: Int): GameUiState {
        // Quiz-style grades; Pre-KG toys score differently (see below).
        val lesson = CatoCurriculum.all.first { it.grade != Grade.PREKG && it.content.rounds.size >= total }
        val trimmed = lesson.copy(content = lesson.content.copy(rounds = lesson.content.rounds.take(total)))
        return GameUiState(lesson = trimmed, correct = correct, wrong = total - correct)
    }

    @Test
    fun `perfect run earns three stars`() {
        assertEquals(3, stateWith(5, 5).stars)
        assertEquals(100, stateWith(5, 5).score)
    }

    @Test
    fun `most right earns two stars`() {
        val s = stateWith(4, 5)      // 80%
        assertEquals(2, s.stars)
        assertEquals(80, s.score)
    }

    @Test
    fun `half right earns one star`() {
        val s = stateWith(3, 6)      // 50%
        assertEquals(1, s.stars)
    }

    @Test
    fun `mostly wrong earns none`() {
        assertEquals(0, stateWith(1, 5).stars)
    }

    @Test
    fun `empty lesson does not divide by zero`() {
        val s = GameUiState(lesson = null)
        assertEquals(0, s.score)
        assertEquals(0, s.stars)
        assertEquals(0f, s.progress, 0.001f)
    }

    private fun toyWith(firstTry: Int, total: Int, misses: Int = total - firstTry): GameUiState {
        val lesson = CatoCurriculum.all.first { it.grade == Grade.PREKG && it.content.rounds.size >= total }
        val trimmed = lesson.copy(content = lesson.content.copy(rounds = lesson.content.rounds.take(total)))
        // A toy always ends right, so "correct" is every round; first-try is the real measure.
        return GameUiState(lesson = trimmed, correct = total, firstTry = firstTry, misses = misses)
    }

    @Test
    fun `each wrong try costs a quarter of a round`() {
        assertEquals(100, toyWith(4, 4).score)
        assertEquals(75, toyWith(0, 1, misses = 1).score)      // one slip in a one-round toy
        assertEquals(87, toyWith(2, 4).score)                  // two slips over four rounds (87.5)
    }

    @Test
    fun `wrong tries never take a toy below half`() {
        assertEquals(50, toyWith(0, 2, misses = 30).score)
        assertEquals(1, toyWith(0, 2, misses = 30).stars)
    }

    @Test
    fun `a little one always earns a star for finishing a toy`() {
        assertEquals(1, toyWith(0, 4, misses = 99).stars)
        assertEquals(3, toyWith(4, 4).stars)
    }
}
