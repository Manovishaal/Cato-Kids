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

// ---------------------------------------------------------------- creator tools

@Serializable
data class AssignmentDto(
    val id: String? = null,
    @SerialName("class_id") val classId: String,
    @SerialName("lesson_id") val lessonId: String? = null,
    @SerialName("assigned_by") val assignedBy: String? = null,
    @SerialName("due_date") val dueDate: String? = null,
    val note: String? = null,
    val title: String? = null,
    val type: String = "lesson",
    val instructions: String? = null,
    @SerialName("points_reward") val pointsReward: Int = 10,
    @SerialName("requires_submission") val requiresSubmission: Boolean = false,
    @SerialName("custom_game_id") val customGameId: String? = null,
    @SerialName("course_id") val courseId: String? = null,
    @SerialName("activity_id") val activityId: String? = null,
) {
    fun toDomain() = Assignment(
        id = id.orEmpty(),
        classId = classId,
        lessonId = lessonId,
        assignedBy = assignedBy,
        dueDate = dueDate,
        note = note,
        title = title.orEmpty(),
        type = AssignmentType.fromWire(type),
        instructions = instructions.orEmpty(),
        pointsReward = pointsReward,
        requiresSubmission = requiresSubmission,
        customGameId = customGameId,
        courseId = courseId,
        activityId = activityId,
    )

    companion object {
        fun from(a: Assignment) = AssignmentDto(
            id = a.id.ifBlank { null },
            classId = a.classId,
            lessonId = a.lessonId,
            assignedBy = a.assignedBy,
            dueDate = a.dueDate,
            note = a.note,
            title = a.title.ifBlank { null },
            type = a.type.wire,
            instructions = a.instructions.ifBlank { null },
            pointsReward = a.pointsReward,
            requiresSubmission = a.requiresSubmission,
            customGameId = a.customGameId,
            courseId = a.courseId,
            activityId = a.activityId,
        )
    }
}

@Serializable
data class AssignmentSubmissionDto(
    val id: String? = null,
    @SerialName("assignment_id") val assignmentId: String,
    @SerialName("student_id") val studentId: String,
    @SerialName("answer_text") val answerText: String? = null,
    val status: String = "submitted",
    val score: Int? = null,
    @SerialName("teacher_feedback") val teacherFeedback: String? = null,
    @SerialName("reviewed_by") val reviewedBy: String? = null,
) {
    fun toDomain() = AssignmentSubmission(
        id = id.orEmpty(),
        assignmentId = assignmentId,
        studentId = studentId,
        answerText = answerText,
        status = SubmissionStatus.fromWire(status),
        score = score,
        teacherFeedback = teacherFeedback,
        reviewedBy = reviewedBy,
    )
}

/** Only the fields a teacher is allowed to change once a child has handed work in. */
@Serializable
data class SubmissionReviewDto(
    val status: String,
    val score: Int? = null,
    @SerialName("teacher_feedback") val teacherFeedback: String? = null,
    @SerialName("reviewed_by") val reviewedBy: String? = null,
)

@Serializable
data class CustomGameDto(
    val id: String? = null,
    @SerialName("created_by") val createdBy: String? = null,
    @SerialName("school_id") val schoolId: String? = null,
    val title: String,
    val description: String? = null,
    @SerialName("subject_id") val subjectId: String? = null,
    val grade: String? = null,
    @SerialName("game_type") val gameType: String,
    val content: LessonContent = LessonContent(),
    @SerialName("is_published") val isPublished: Boolean = true,
) {
    fun toDomain() = CustomGame(
        id = id.orEmpty(),
        createdBy = createdBy,
        schoolId = schoolId,
        title = title,
        description = description.orEmpty(),
        subject = subjectId?.let { SubjectId.fromWire(it) },
        grade = Grade.fromWire(grade),
        gameType = GameType.fromWire(gameType),
        content = content,
        published = isPublished,
    )

    companion object {
        fun from(g: CustomGame) = CustomGameDto(
            id = g.id.ifBlank { null },
            createdBy = g.createdBy,
            schoolId = g.schoolId,
            title = g.title,
            description = g.description.ifBlank { null },
            subjectId = g.subject?.wire,
            grade = g.grade?.wire,
            gameType = g.gameType.wire,
            content = g.content,
            isPublished = g.published,
        )
    }
}

@Serializable
data class ExtraCourseDto(
    val id: String? = null,
    @SerialName("created_by") val createdBy: String? = null,
    @SerialName("school_id") val schoolId: String? = null,
    val title: String,
    val description: String? = null,
    @SerialName("cover_emoji") val coverEmoji: String = "📘",
    @SerialName("subject_id") val subjectId: String? = null,
    val grade: String? = null,
    @SerialName("lesson_ids") val lessonIds: List<String> = emptyList(),
    @SerialName("is_published") val isPublished: Boolean = true,
) {
    fun toDomain() = ExtraCourse(
        id = id.orEmpty(),
        createdBy = createdBy,
        schoolId = schoolId,
        title = title,
        description = description.orEmpty(),
        coverEmoji = coverEmoji,
        subject = subjectId?.let { SubjectId.fromWire(it) },
        grade = Grade.fromWire(grade),
        lessonIds = lessonIds,
        published = isPublished,
    )

    companion object {
        fun from(c: ExtraCourse) = ExtraCourseDto(
            id = c.id.ifBlank { null },
            createdBy = c.createdBy,
            schoolId = c.schoolId,
            title = c.title,
            description = c.description.ifBlank { null },
            coverEmoji = c.coverEmoji,
            subjectId = c.subject?.wire,
            grade = c.grade?.wire,
            lessonIds = c.lessonIds,
            isPublished = c.published,
        )
    }
}

@Serializable
data class ActivityDto(
    val id: String? = null,
    @SerialName("created_by") val createdBy: String? = null,
    @SerialName("school_id") val schoolId: String? = null,
    val title: String,
    val instructions: String = "",
    @SerialName("activity_type") val activityType: String = "creative",
    val grade: String? = null,
    @SerialName("points_reward") val pointsReward: Int = 10,
    @SerialName("is_published") val isPublished: Boolean = true,
) {
    fun toDomain() = Activity(
        id = id.orEmpty(),
        createdBy = createdBy,
        schoolId = schoolId,
        title = title,
        instructions = instructions,
        activityType = ActivityType.fromWire(activityType),
        grade = Grade.fromWire(grade),
        pointsReward = pointsReward,
        published = isPublished,
    )

    companion object {
        fun from(a: Activity) = ActivityDto(
            id = a.id.ifBlank { null },
            createdBy = a.createdBy,
            schoolId = a.schoolId,
            title = a.title,
            instructions = a.instructions,
            activityType = a.activityType.wire,
            grade = a.grade?.wire,
            pointsReward = a.pointsReward,
            isPublished = a.published,
        )
    }
}

// ---------------------------------------------------------------- avatar & shop

@Serializable
data class StudentAvatarDto(
    @SerialName("student_id") val studentId: String,
    val config: AvatarConfig = AvatarConfig(),
)

@Serializable
data class StudentInventoryDto(
    val id: String? = null,
    @SerialName("student_id") val studentId: String,
    @SerialName("item_key") val itemKey: String,
)
