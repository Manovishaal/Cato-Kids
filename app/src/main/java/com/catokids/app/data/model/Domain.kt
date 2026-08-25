package com.catokids.app.data.model

@JvmInline
value class UserId(val value: String)

data class Profile(
    val id: String,
    val role: Role,
    val fullName: String,
    val email: String? = null,
    val phone: String? = null,
    val avatarUrl: String? = null,
    val schoolId: String? = null,
    val grade: Grade? = null,
    val coins: Int = 0,
    val stars: Int = 0,
    val streakDays: Int = 0,
) {
    val initials: String
        get() = fullName.trim().split(" ").filter { it.isNotBlank() }
            .take(2).joinToString("") { it.first().uppercase() }
            .ifBlank { "?" }

    val firstName: String get() = fullName.trim().substringBefore(' ').ifBlank { "friend" }
}

data class School(
    val id: String,
    val name: String,
    val code: String,
    val city: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val email: String? = null,
)

data class ClassRoom(
    val id: String,
    val name: String,
    val grade: Grade,
    val schoolId: String?,
    val teacherId: String?,
    val academicYear: String? = null,
    val studentCount: Int = 0,
)

data class LessonProgress(
    val studentId: String,
    val lessonId: String,
    val stars: Int = 0,
    val bestScore: Int = 0,
    val attempts: Int = 0,
    val secondsSpent: Int = 0,
    val completed: Boolean = false,
    val lastPlayedAtMillis: Long = 0L,
)

data class QuizAttempt(
    val id: String,
    val studentId: String,
    val lessonId: String?,
    val score: Int,
    val totalQuestions: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val durationSeconds: Int,
    val createdAtMillis: Long = 0L,
)

data class Assignment(
    val id: String,
    val classId: String,
    val lessonId: String,
    val assignedBy: String?,
    val dueDate: String?,
    val note: String?,
)

/** A student row as a teacher / parent / school sees it. */
data class StudentSummary(
    val profile: Profile,
    val lessonsCompleted: Int = 0,
    val totalLessons: Int = 0,
    val stars: Int = 0,
    val averageScore: Int = 0,
    val lastActiveLabel: String = "—",
) {
    val completionPercent: Int
        get() = if (totalLessons == 0) 0 else (lessonsCompleted * 100) / totalLessons
}
