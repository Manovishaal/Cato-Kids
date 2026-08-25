package com.catokids.app.data.model

/**
 * What a teacher, school or administrator builds for their class: a custom game,
 * an elective bundle of lessons, or an offline activity. Students only ever see
 * these through an [Assignment] — the creator screens that make them live behind
 * role-gated navigation, never in the student graph.
 */

/**
 * Prefixes a [CustomGame.id] into the plain-text lesson id [GameViewModel][com.catokids.app.ui.games.GameViewModel]
 * and the bundled curriculum share. A double underscore, not a colon: this id travels
 * inside a Navigation Compose path segment, and a UUID never contains "__", so the
 * split stays unambiguous either way.
 */
const val CUSTOM_GAME_LESSON_PREFIX = "customgame__"

enum class ActivityType(val wire: String, val label: String, val emoji: String) {
    CREATIVE   ("creative",    "Creative",    "🎨"),
    PHYSICAL   ("physical",    "Get moving",  "🤸"),
    CRAFT      ("craft",       "Craft",       "✂️"),
    READING    ("reading",     "Reading",     "📚"),
    OUTDOOR    ("outdoor",     "Outdoors",    "🌳"),
    SCREEN_FREE("screen_free", "Screen-free", "🧩");

    companion object {
        fun fromWire(value: String?): ActivityType =
            entries.firstOrNull { it.wire.equals(value, ignoreCase = true) } ?: CREATIVE
    }
}

/** A game a teacher built themselves, reusing one of the app's nine engines with their own content. */
data class CustomGame(
    val id: String,
    val createdBy: String?,
    val schoolId: String?,
    val title: String,
    val description: String = "",
    val subject: SubjectId? = null,
    val grade: Grade? = null,
    val gameType: GameType,
    val content: LessonContent,
    val published: Boolean = true,
) {
    /** Wraps this as a [Lesson] so the existing game engines and [GameHostScreen] need no changes. */
    fun toLesson(): Lesson = Lesson(
        id = "$CUSTOM_GAME_LESSON_PREFIX$id",
        subject = subject ?: SubjectId.LETTER_LAND,
        grade = grade ?: Grade.LKG,
        title = title,
        subtitle = "By your teacher",
        description = description,
        gameType = gameType,
        content = content,
    )
}

/** An elective bundle of existing curriculum lessons a teacher curated for their class. */
data class ExtraCourse(
    val id: String,
    val createdBy: String?,
    val schoolId: String?,
    val title: String,
    val description: String = "",
    val coverEmoji: String = "📘",
    val subject: SubjectId? = null,
    val grade: Grade? = null,
    val lessonIds: List<String> = emptyList(),
    val published: Boolean = true,
)

/** A hands-on task with no screen involved — the "homework" that isn't a lesson at all. */
data class Activity(
    val id: String,
    val createdBy: String?,
    val schoolId: String?,
    val title: String,
    val instructions: String = "",
    val activityType: ActivityType = ActivityType.CREATIVE,
    val grade: Grade? = null,
    val pointsReward: Int = 10,
    val published: Boolean = true,
)
