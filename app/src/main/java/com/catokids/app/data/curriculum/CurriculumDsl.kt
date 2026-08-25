package com.catokids.app.data.curriculum

import com.catokids.app.data.model.*

/** Tiny builder so the curriculum files read like the workbooks they come from. */
internal class LessonBuilder(
    private val subject: SubjectId,
    private val grade: Grade,
) {
    private val lessons = mutableListOf<Lesson>()

    fun lesson(
        id: String,
        title: String,
        subtitle: String = "",
        description: String = "",
        game: GameType,
        intro: String = "",
        speak: String? = null,
        rounds: List<GameRound>,
    ) {
        lessons += Lesson(
            id = "${grade.wire.lowercase()}_${subject.wire}_$id",
            subject = subject,
            grade = grade,
            title = title,
            subtitle = subtitle,
            description = description,
            order = lessons.size,
            gameType = game,
            content = LessonContent(intro = intro, speak = speak, rounds = rounds),
        )
    }

    fun build(): List<Lesson> = lessons.toList()
}

internal fun lessons(subject: SubjectId, grade: Grade, block: LessonBuilder.() -> Unit): List<Lesson> =
    LessonBuilder(subject, grade).apply(block).build()

// ---------- round helpers ----------

internal fun traceRound(id: String, glyph: String, prompt: String, speak: String? = null, emoji: String = "") =
    GameRound(id = id, glyph = glyph, prompt = prompt, speak = speak ?: glyph, emoji = emoji)

internal fun tapAllRound(id: String, target: String, prompt: String, pool: List<String>, speak: String? = null) =
    GameRound(
        id = id,
        target = target,
        prompt = prompt,
        speak = speak,
        options = pool.map { Option(label = it, correct = it == target) },
    )

internal fun countRound(id: String, emoji: String, count: Int, prompt: String, choices: List<Int>) =
    GameRound(
        id = id,
        emoji = emoji,
        count = count,
        prompt = prompt,
        speak = prompt,
        target = count.toString(),
        options = choices.map { Option(label = it.toString(), correct = it == count) },
    )

internal fun pickRound(
    id: String,
    prompt: String,
    speak: String? = null,
    correct: Pair<String, String>,
    distractors: List<Pair<String, String>>,
    explanation: String? = null,
) = GameRound(
    id = id,
    prompt = prompt,
    speak = speak ?: prompt,
    target = correct.first,
    explanation = explanation,
    options = (listOf(correct to true) + distractors.map { it to false })
        .map { (p, ok) -> Option(label = p.first, emoji = p.second, correct = ok) },
)

internal fun matchRound(id: String, prompt: String, pairs: List<MatchPair>) =
    GameRound(id = id, prompt = prompt, speak = prompt, pairs = pairs)

internal fun sortRound(id: String, prompt: String, buckets: List<String>, items: List<BucketItem>) =
    GameRound(id = id, prompt = prompt, speak = prompt, buckets = buckets, items = items)

internal fun wordRound(id: String, word: String, emoji: String, hint: String) =
    GameRound(id = id, target = word.uppercase(), emoji = emoji, prompt = hint, speak = word)
