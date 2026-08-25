package com.catokids.app

import com.catokids.app.data.curriculum.CatoCurriculum
import com.catokids.app.data.model.Grade
import com.catokids.app.data.model.GameType
import com.catokids.app.data.model.SubjectId
import org.junit.Assert.*
import org.junit.Test

/**
 * The curriculum is data, and bad data means a child gets stuck on a broken round.
 * These tests are the guard rail.
 */
class CurriculumTest {

    private val all = CatoCurriculum.all

    @Test
    fun `every grade has all three books`() {
        Grade.entries.forEach { grade ->
            SubjectId.entries.forEach { subject ->
                val lessons = CatoCurriculum.forGradeAndSubject(grade, subject)
                assertTrue("$grade / $subject has no lessons", lessons.isNotEmpty())
            }
        }
    }

    @Test
    fun `lesson ids are unique`() {
        val dupes = all.groupBy { it.id }.filter { it.value.size > 1 }.keys
        assertTrue("Duplicate lesson ids: $dupes", dupes.isEmpty())
    }

    @Test
    fun `no lesson is empty`() {
        all.forEach { lesson ->
            assertTrue("${lesson.id} has no rounds", lesson.content.rounds.isNotEmpty())
            assertTrue("${lesson.id} has no title", lesson.title.isNotBlank())
        }
    }

    @Test
    fun `round ids are unique inside each lesson`() {
        all.forEach { lesson ->
            val ids = lesson.content.rounds.map { it.id }
            assertEquals("${lesson.id} has duplicate round ids", ids.size, ids.distinct().size)
        }
    }

    @Test
    fun `choice rounds have exactly one correct answer`() {
        val singleAnswer = setOf(GameType.QUIZ, GameType.LISTEN_PICK, GameType.SHAPE_HUNT, GameType.COUNT_TAP)
        all.filter { it.gameType in singleAnswer }.forEach { lesson ->
            lesson.content.rounds.forEach { round ->
                val correct = round.options.count { it.correct }
                assertEquals(
                    "${lesson.id}/${round.id} should have exactly one correct option (has $correct)",
                    1, correct,
                )
                assertTrue("${lesson.id}/${round.id} needs at least two options", round.options.size >= 2)
            }
        }
    }

    @Test
    fun `no choice round has duplicate option labels`() {
        // Tap-all boards are meant to repeat the target letter, so they are excluded.
        all.filter { it.gameType != GameType.TAP_ALL }.forEach { lesson ->
            lesson.content.rounds.forEach { round ->
                if (round.options.size > 1) {
                    val labels = round.options.map { it.label }
                    assertEquals(
                        "${lesson.id}/${round.id} has repeated options: $labels",
                        labels.size, labels.distinct().size,
                    )
                }
            }
        }
    }

    @Test
    fun `tap-all rounds contain findable targets`() {
        all.filter { it.gameType == GameType.TAP_ALL }.forEach { lesson ->
            lesson.content.rounds.forEach { round ->
                val hits = round.options.count { it.correct }
                assertTrue("${lesson.id}/${round.id} has no correct tiles", hits > 0)
                assertTrue("${lesson.id}/${round.id} is all correct tiles", hits < round.options.size)
            }
        }
    }

    @Test
    fun `sorting rounds only use declared buckets`() {
        all.filter { it.gameType == GameType.SORT_BUCKETS }.forEach { lesson ->
            lesson.content.rounds.forEach { round ->
                assertTrue("${lesson.id}/${round.id} has no buckets", round.buckets.isNotEmpty())
                assertTrue("${lesson.id}/${round.id} has no items", round.items.isNotEmpty())
                round.items.forEach { item ->
                    assertTrue(
                        "${lesson.id}/${round.id}: '${item.label}' goes to unknown bucket '${item.bucket}'",
                        item.bucket in round.buckets,
                    )
                }
                round.buckets.forEach { bucket ->
                    assertTrue(
                        "${lesson.id}/${round.id}: bucket '$bucket' would be left empty",
                        round.items.any { it.bucket == bucket },
                    )
                }
            }
        }
    }

    @Test
    fun `matching rounds have unique sides`() {
        all.filter { it.gameType == GameType.MATCH_PAIRS }.forEach { lesson ->
            lesson.content.rounds.forEach { round ->
                assertTrue("${lesson.id}/${round.id} has no pairs", round.pairs.isNotEmpty())
                val lefts = round.pairs.map { it.left }
                val rights = round.pairs.map { it.right }
                assertEquals("${lesson.id}/${round.id} repeats a left item", lefts.size, lefts.distinct().size)
                assertEquals("${lesson.id}/${round.id} repeats a right item", rights.size, rights.distinct().size)
            }
        }
    }

    @Test
    fun `word building rounds have a real word`() {
        all.filter { it.gameType == GameType.JUMBLED_WORD }.forEach { lesson ->
            lesson.content.rounds.forEach { round ->
                assertTrue("${lesson.id}/${round.id} has no target word", round.target.length >= 2)
                assertEquals(
                    "${lesson.id}/${round.id} target should be uppercase",
                    round.target.uppercase(), round.target,
                )
            }
        }
    }

    @Test
    fun `tracing rounds have a glyph`() {
        all.filter { it.gameType == GameType.TRACE }.forEach { lesson ->
            lesson.content.rounds.forEach { round ->
                assertTrue("${lesson.id}/${round.id} has nothing to trace", round.glyph.isNotBlank())
            }
        }
    }

    @Test
    fun `counting rounds are consistent`() {
        all.filter { it.gameType == GameType.COUNT_TAP }.forEach { lesson ->
            lesson.content.rounds.forEach { round ->
                assertTrue("${lesson.id}/${round.id} counts nothing", round.count > 0)
                assertTrue("${lesson.id}/${round.id} has no picture", round.emoji.isNotBlank())
                val correct = round.options.first { it.correct }.label
                assertEquals(
                    "${lesson.id}/${round.id}: shown ${round.count} but answer is $correct",
                    round.count.toString(), correct,
                )
            }
        }
    }

    @Test
    fun `every game engine is exercised somewhere`() {
        val used = all.map { it.gameType }.toSet()
        val missing = GameType.entries - used
        assertTrue("These game types have no lessons: $missing", missing.isEmpty())
    }

    @Test
    fun `the syllabus is a reasonable size`() {
        assertTrue("Only ${all.size} lessons", all.size >= 45)
        Grade.entries.forEach {
            assertTrue("${it.label} has only ${CatoCurriculum.countFor(it)} lessons", CatoCurriculum.countFor(it) >= 12)
        }
    }
}
