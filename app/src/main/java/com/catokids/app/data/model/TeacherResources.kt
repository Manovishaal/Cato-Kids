package com.catokids.app.data.model

/**
 * The 16 developmental / curricular domains the Cato Kids program is built around.
 * Every training write-up in [com.catokids.app.data.local.TeacherResourceLibrary] and
 * every activity in [com.catokids.app.data.local.ActivityLibrary] is tagged to one of
 * these, so a teacher can browse either "by domain" or "by class."
 */
enum class DevelopmentalDomain(val wire: String, val title: String, val emoji: String) {
    LANGUAGE_DEVELOPMENT ("language_development",  "Language Development",  "🗣️"),
    COGNITIVE_DEVELOPMENT("cognitive_development", "Cognitive Development", "🧠"),
    EARLY_PRACTICAL_LIFE ("early_practical_life",  "Early Practical Life",  "🧺"),
    FINE_MOTOR           ("fine_motor",            "Fine Motor",            "✋"),
    GROSS_MOTOR          ("gross_motor",            "Gross Motor",           "🤸"),
    SOCIAL_SKILLS        ("social_skills",          "Social Skills",         "🤝"),
    EMOTIONAL_SKILLS     ("emotional_skills",       "Emotional Skills",      "💗"),
    ART_AND_CRAFT        ("art_and_craft",          "Art & Craft",           "🎨"),
    ENGLISH              ("english",                "English",               "🔤"),
    MATH                 ("math",                   "Math",                  "🔢"),
    SCIENCE_EVS          ("science_evs",             "Science (EVS)",         "🌍"),
    STEAM                ("steam",                   "STEAM",                 "🧪"),
    STORIES              ("stories",                 "Stories",               "📖"),
    COMPREHENSION        ("comprehension",           "Comprehension",         "💭"),
    CVC_WORDS            ("cvc_words",               "CVC Words",             "🐱"),
    TWO_LETTER_WORDS     ("two_letter_words",        "Two-letter Words",      "🔡");

    companion object {
        fun fromWire(value: String?): DevelopmentalDomain? =
            entries.firstOrNull { it.wire.equals(value, ignoreCase = true) }
    }
}

/**
 * One grade-specific "how to teach this" briefing a teacher opens before running lessons
 * or activities in a domain. Deliberately grade-specific — what "cognitive development"
 * means to teach a Pre-KG three-year-old is not what it means for a UKG five-year-old.
 */
data class TeachingResource(
    val domain: DevelopmentalDomain,
    val grade: Grade,
    val overview: String,
    val goals: List<String>,
    val teachingTips: List<String>,
    val lookFor: String,
)

/**
 * One of the 80 bundled, ready-to-run interactive activities in the library. A teacher
 * reads the objective and instructions here to understand what it teaches and how to run
 * it, then assigns it as-is to a class — no authoring required, unlike a custom [Activity].
 */
data class LibraryActivity(
    val id: String,
    val title: String,
    val domain: DevelopmentalDomain,
    val grade: Grade,
    val activityType: ActivityType,
    val objective: String,
    val instructions: String,
    val materials: List<String> = emptyList(),
    val durationMinutes: Int = 15,
    val pointsReward: Int = 10,
)
