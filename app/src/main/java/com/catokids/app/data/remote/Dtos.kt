package com.catokids.app.data.remote

import com.catokids.app.data.model.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileDto(
    val id: String,
    val role: String = "student",
    @SerialName("full_name") val fullName: String = "",
    val email: String? = null,
    val phone: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("school_id") val schoolId: String? = null,
    val grade: String? = null,
    val coins: Int = 0,
    val stars: Int = 0,
    @SerialName("streak_days") val streakDays: Int = 0,
) {
    fun toDomain() = Profile(
        id = id,
        role = Role.fromWire(role),
        fullName = fullName,
        email = email,
        phone = phone,
        avatarUrl = avatarUrl,
        schoolId = schoolId,
        grade = Grade.fromWire(grade),
        coins = coins,
        stars = stars,
        streakDays = streakDays,
    )
}

@Serializable
data class ProfileUpdateDto(
    @SerialName("full_name") val fullName: String? = null,
    val phone: String? = null,
    val grade: String? = null,
    @SerialName("school_id") val schoolId: String? = null,
    val coins: Int? = null,
    val stars: Int? = null,
)

@Serializable
data class SchoolDto(
    val id: String,
    val name: String,
    val code: String,
    val city: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val email: String? = null,
) {
    fun toDomain() = School(id, name, code, city, address, phone, email)
}

@Serializable
data class ClassDto(
    val id: String,
    val name: String,
    val grade: String,
    @SerialName("school_id") val schoolId: String? = null,
    @SerialName("teacher_id") val teacherId: String? = null,
    @SerialName("academic_year") val academicYear: String? = null,
) {
    fun toDomain() = ClassRoom(
        id = id,
        name = name,
        grade = Grade.fromWire(grade) ?: Grade.PREKG,
        schoolId = schoolId,
        teacherId = teacherId,
        academicYear = academicYear,
    )
}

@Serializable
data class ClassStudentDto(
    @SerialName("class_id") val classId: String,
    @SerialName("student_id") val studentId: String,
)

@Serializable
data class ParentChildDto(
    @SerialName("parent_id") val parentId: String,
    @SerialName("student_id") val studentId: String,
    val relationship: String? = null,
)

@Serializable
data class LessonProgressDto(
    @SerialName("student_id") val studentId: String,
    @SerialName("lesson_id")  val lessonId: String,
    val stars: Int = 0,
    @SerialName("best_score") val bestScore: Int = 0,
    val attempts: Int = 0,
    @SerialName("seconds_spent") val secondsSpent: Int = 0,
    val completed: Boolean = false,
) {
    fun toDomain() = LessonProgress(
        studentId = studentId,
        lessonId = lessonId,
        stars = stars,
        bestScore = bestScore,
        attempts = attempts,
        secondsSpent = secondsSpent,
        completed = completed,
    )

    companion object {
        fun from(p: LessonProgress) = LessonProgressDto(
            studentId = p.studentId,
            lessonId = p.lessonId,
            stars = p.stars,
            bestScore = p.bestScore,
            attempts = p.attempts,
            secondsSpent = p.secondsSpent,
            completed = p.completed,
        )
    }
}

@Serializable
data class QuizAttemptDto(
    @SerialName("student_id") val studentId: String,
    @SerialName("lesson_id")  val lessonId: String? = null,
    val score: Int = 0,
    @SerialName("total_questions") val totalQuestions: Int = 0,
    @SerialName("correct_count")   val correctCount: Int = 0,
    @SerialName("wrong_count")     val wrongCount: Int = 0,
    @SerialName("duration_seconds") val durationSeconds: Int = 0,
)

@Serializable
data class LessonSeedDto(
    val id: String,
    @SerialName("subject_id") val subjectId: String,
    val grade: String,
    val title: String,
    val subtitle: String? = null,
    val description: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("game_type") val gameType: String,
    val content: LessonContent = LessonContent(),
)
