package com.catokids.app

import com.catokids.app.data.curriculum.CatoCurriculum
import com.catokids.app.ui.games.GameUiState
import org.junit.Assert.assertEquals
import org.junit.Test

class ScoringTest {

    private fun stateWith(correct: Int, total: Int): GameUiState {
        val lesson = CatoCurriculum.all.first { it.content.rounds.size >= total }
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
}
