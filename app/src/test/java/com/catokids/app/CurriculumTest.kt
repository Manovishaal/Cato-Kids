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
    fun `every PreKG letter has its workbook craft in a lesson and in the library`() {
        val inLessons = CatoCurriculum.forGradeAndSubject(Grade.PREKG, SubjectId.LETTER_LAND)
            .flatMap { it.content.crafts }
        val letters = inLessons.map { it.letter }.filter { it.isNotBlank() }.toSet()
        assertEquals(('A'..'Z').map { it.toString() }.toSet(), letters)

        val library = com.catokids.app.data.local.ActivityLibrary.all.map { it.id }.toSet()
        inLessons.forEach { assertTrue("${it.id} missing from the Activity Library", it.id in library) }
    }

    @Test
    fun `crafts are complete and uniquely named`() {
        val crafts = all.flatMap { it.content.crafts }
        assertEquals("Duplicate craft ids", crafts.size, crafts.map { it.id }.distinct().size)
        crafts.forEach { craft ->
            assertTrue("${craft.id} has no title", craft.title.isNotBlank())
            assertTrue("${craft.id} has no picture", craft.emoji.isNotBlank())
            assertTrue("${craft.id} has no steps", craft.steps.isNotEmpty())
            assertTrue("${craft.id} has no materials", craft.materials.isNotEmpty())
        }
    }

    @Test
    fun `library activity ids are unique`() {
        val ids = com.catokids.app.data.local.ActivityLibrary.all.map { it.id }
        val dupes = ids.groupBy { it }.filter { it.value.size > 1 }.keys
        assertTrue("Duplicate activity ids: $dupes", dupes.isEmpty())
    }

    /**
     * Lessons must appear in the same order as the pages of the printed workbooks. Topics
     * the app teaches that the book doesn't have sit just before each book's quiz.
     */
    @Test
    fun `lessons follow the workbook page order`() {
        fun ids(grade: Grade, subject: SubjectId) = CatoCurriculum.forGradeAndSubject(grade, subject)
            .sortedBy { it.order }
            .map { it.id.removePrefix("${grade.wire.lowercase()}_${subject.wire}_") }

        val letterGroups = listOf("a_d", "e_h", "i_l", "m_p", "q_t", "u_z")
        val preKgLetters = letterGroups.flatMap { g ->
            listOf("meet_$g", "write_$g", "learnt_$g") +
                (if (g == "a_d") listOf("find_a") else emptyList()) +
                (if (g == "i_l") listOf("strokes") else emptyList())
        } + listOf("big_small", "quiz")

        val expected = mapOf(
            (Grade.PREKG to SubjectId.LETTER_LAND) to preKgLetters,
            (Grade.PREKG to SubjectId.NUMBER_LAND) to listOf("big_small_size", "tall_short", "heavy_light", "more_less",
                "count_1_5", "count_6_10", "colors", "shapes", "match_qty", "count_11_15", "odd_one_out",
                "count_16_20", "trace_num", "trace_6_10", "trace_11_20", "trace_16_20", "quiz"),
            (Grade.PREKG to SubjectId.KNOW_MY_WORLD) to listOf("my_family", "my_school", "my_body", "fruits",
                "vegetables", "fruits_veg", "trees", "animals", "find_me", "birds", "seasons", "keep_clean",
                "manners", "quiz"),
            (Grade.LKG to SubjectId.LETTER_LAND) to (1..6).flatMap { listOf("write_$it", "find_$it") } +
                listOf("case_match", "vowels", "beginning_sounds", "cvc_intro", "quiz"),
            (Grade.LKG to SubjectId.NUMBER_LAND) to listOf("more_less", "shapes2", "write_1_10", "count_6_10",
                "before_after", "patterns", "add_intro", "quiz"),
            (Grade.LKG to SubjectId.KNOW_MY_WORLD) to listOf("animal_homes", "helpers", "transport", "living",
                "weather", "quiz"),
            (Grade.UKG to SubjectId.LETTER_LAND) to listOf("sight_words", "cvc_a", "cvc_e_i", "cvc_o_u",
                "write_words", "rhyme", "blends", "quiz"),
            (Grade.UKG to SubjectId.NUMBER_LAND) to listOf("count_20", "write_teens", "addition", "subtraction",
                "shapes3", "skip", "time_money", "quiz"),
            (Grade.UKG to SubjectId.KNOW_MY_WORLD) to listOf("plants", "young_ones", "good_habits", "safety",
                "earth", "senses", "quiz"),
        )
        expected.forEach { (key, order) ->
            assertEquals("${key.first} / ${key.second} is out of book order", order, ids(key.first, key.second))
        }
    }

    /** The README quotes the lesson count; keep it true. */
    @Test
    fun `README lesson count matches the curriculum`() {
        val readme = java.io.File("../README.md").readText()
        val quoted = Regex("""\*\*(\d+) lessons\*\*""").find(readme)?.groupValues?.get(1)?.toInt()
        assertEquals("README says $quoted lessons", all.size, quoted)
    }

    @Test
    fun `the syllabus is a reasonable size`() {
        assertTrue("Only ${all.size} lessons", all.size >= 45)
        Grade.entries.forEach {
            assertTrue("${it.label} has only ${CatoCurriculum.countFor(it)} lessons", CatoCurriculum.countFor(it) >= 12)
        }
    }
}
