package com.catokids.app.data.model

import kotlinx.serialization.Serializable

/** The three Cato Kids book strands. */
@Serializable
enum class SubjectId(
    val wire: String,
    val title: String,
    val bookTitle: String,
    val emoji: String,
    val blurb: String,
) {
    LETTER_LAND("letter_land", "Letter Land",   "Reading, Writing & Activities", "🔤", "Letters, sounds and first words"),
    NUMBER_LAND("number_land", "Number Land",   "Concepts and Number Fun",       "🔢", "Counting, shapes and patterns"),
    KNOW_MY_WORLD("know_my_world", "Know My World", "Fun Activities",            "🌍", "The world around me");

    companion object {
        fun fromWire(value: String?): SubjectId =
            entries.firstOrNull { it.wire == value } ?: LETTER_LAND
    }
}

/** Every playable game engine in the app. */
@Serializable
enum class GameType(val wire: String, val title: String, val emoji: String) {
    TRACE       ("TRACE",        "Trace it",      "✏️"),
    TAP_ALL     ("TAP_ALL",      "Find them all", "👆"),
    COUNT_TAP   ("COUNT_TAP",    "Count and tap", "🖐️"),
    MATCH_PAIRS ("MATCH_PAIRS",  "Match pairs",   "🃏"),
    SORT_BUCKETS("SORT_BUCKETS", "Sort it out",   "🧺"),
    SHAPE_HUNT  ("SHAPE_HUNT",   "Shape hunt",    "🔷"),
    LISTEN_PICK ("LISTEN_PICK",  "Listen and pick", "👂"),
    JUMBLED_WORD("JUMBLED_WORD", "Build the word", "🧩"),
    QUIZ        ("QUIZ",         "Quiz time",     "🏆");

    companion object {
        fun fromWire(value: String?): GameType =
            entries.firstOrNull { it.wire == value } ?: QUIZ
    }
}

@Serializable
data class Option(
    val label: String,
    val emoji: String = "",
    val correct: Boolean = false,
)

@Serializable
data class MatchPair(
    val left: String,
    val right: String,
    val emoji: String = "",
)

@Serializable
data class BucketItem(
    val label: String,
    val emoji: String,
    val bucket: String,
)

/**
 * One playable round. Only the fields relevant to the lesson's [GameType] are used,
 * which keeps the whole curriculum a single JSON-serialisable shape that round-trips
 * to the `lessons.content` jsonb column without polymorphic gymnastics.
 */
@Serializable
data class GameRound(
    val id: String,
    val prompt: String = "",
    val speak: String? = null,
    val glyph: String = "",
    val target: String = "",
    val emoji: String = "",
    val count: Int = 0,
    val options: List<Option> = emptyList(),
    val pairs: List<MatchPair> = emptyList(),
    val buckets: List<String> = emptyList(),
    val items: List<BucketItem> = emptyList(),
    val explanation: String? = null,
)

@Serializable
data class LessonContent(
    val intro: String = "",
    val speak: String? = null,
    val rounds: List<GameRound> = emptyList(),
)

@Serializable
data class Lesson(
    val id: String,
    val subject: SubjectId,
    val grade: Grade,
    val title: String,
    val subtitle: String = "",
    val description: String = "",
    val order: Int = 0,
    val gameType: GameType,
    val content: LessonContent,
) {
    val roundCount: Int get() = content.rounds.size
}
