package com.catokids.app.data.repository

import com.catokids.app.core.CatoResult
import com.catokids.app.core.catoRunCatching
import com.catokids.app.data.model.Assignment
import com.catokids.app.data.model.AssignmentSubmission
import com.catokids.app.data.model.SubmissionStatus
import com.catokids.app.data.remote.AssignmentDto
import com.catokids.app.data.remote.AssignmentSubmissionDto
import com.catokids.app.data.remote.SubmissionReviewDto
import com.catokids.app.data.remote.SupabaseProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

/**
 * Homework, activities, extra courses and plain lesson assignments all live in the
 * same `assignments` row shape (see [Assignment]) — only [Assignment.type] and which
 * of the optional foreign keys is set changes what a screen does with one. Row Level
 * Security already scopes a student's view to their own classes, so [myAssignments]
 * needs no class id at all.
 */
class AssignmentRepository(private val auth: AuthRepository) {

    private val online: Boolean
        get() = SupabaseProvider.isConfigured && !auth.isDemo.value

    // ---------- creator side ----------

    suspend fun forClass(classId: String): List<Assignment> {
        if (!online) return SampleData.demoAssignments.filter { it.classId == classId }
        return runCatching {
            SupabaseProvider.client.from("assignments")
                .select { filter { eq("class_id", classId) }; order("created_at", Order.DESCENDING) }
                .decodeList<AssignmentDto>().map { it.toDomain() }
        }.getOrDefault(emptyList())
    }

    suspend fun create(assignment: Assignment): CatoResult<Assignment> = catoRunCatching {
        if (!online) return@catoRunCatching assignment.copy(id = "local-${System.currentTimeMillis()}")
        SupabaseProvider.client.from("assignments")
            .insert(AssignmentDto.from(assignment)) { select() }
            .decodeSingle<AssignmentDto>().toDomain()
    }

    suspend fun delete(id: String): CatoResult<Unit> = catoRunCatching {
        if (!online) return@catoRunCatching Unit
        SupabaseProvider.client.from("assignments").delete { filter { eq("id", id) } }
    }

    // ---------- student side ----------

    /** Every assignment the signed-in student (or their class) can see. */
    suspend fun myAssignments(): List<Assignment> {
        if (!online) return SampleData.demoAssignments
        return runCatching {
            SupabaseProvider.client.from("assignments")
                .select { order("created_at", Order.DESCENDING) }
                .decodeList<AssignmentDto>().map { it.toDomain() }
        }.getOrDefault(emptyList()).ifEmpty { SampleData.demoAssignments }
    }

    // ---------- submissions ----------

    suspend fun submissionsFor(assignmentId: String): List<AssignmentSubmission> {
        if (!online) return SampleData.demoSubmissions.filter { it.assignmentId == assignmentId }
        return runCatching {
            SupabaseProvider.client.from("assignment_submissions")
                .select { filter { eq("assignment_id", assignmentId) } }
                .decodeList<AssignmentSubmissionDto>().map { it.toDomain() }
        }.getOrDefault(emptyList())
    }

    suspend fun mySubmissions(): List<AssignmentSubmission> {
        val userId = auth.profile.value?.id ?: return emptyList()
        if (!online) return SampleData.demoSubmissions.filter { it.studentId == userId || it.studentId.startsWith("demo") }
        return runCatching {
            SupabaseProvider.client.from("assignment_submissions")
                .select { filter { eq("student_id", userId) } }
                .decodeList<AssignmentSubmissionDto>().map { it.toDomain() }
        }.getOrDefault(emptyList())
    }

    suspend fun submit(assignmentId: String, answerText: String): CatoResult<AssignmentSubmission> = catoRunCatching {
        val userId = auth.profile.value?.id ?: error("You are not signed in.")
        if (!online) {
            return@catoRunCatching AssignmentSubmission(
                id = "local-${System.currentTimeMillis()}",
                assignmentId = assignmentId,
                studentId = userId,
                answerText = answerText,
                status = SubmissionStatus.SUBMITTED,
                submittedAtMillis = System.currentTimeMillis(),
            )
        }
        val dto = AssignmentSubmissionDto(
            assignmentId = assignmentId,
            studentId = userId,
            answerText = answerText,
            status = "submitted",
        )
        val saved = SupabaseProvider.client.from("assignment_submissions")
            .upsert(dto) { onConflict = "assignment_id,student_id"; select() }
            .decodeSingle<AssignmentSubmissionDto>().toDomain()
        saved.copy(submittedAtMillis = System.currentTimeMillis())
    }

    /** Teacher/school/admin marking one child's work. */
    suspend fun review(
        submissionId: String,
        status: SubmissionStatus,
        score: Int?,
        feedback: String?,
    ): CatoResult<Unit> = catoRunCatching {
        if (!online) return@catoRunCatching Unit
        val reviewerId = auth.profile.value?.id
        SupabaseProvider.client.from("assignment_submissions").update(
            SubmissionReviewDto(
                status = status.wire,
                score = score,
                teacherFeedback = feedback?.ifBlank { null },
                reviewedBy = reviewerId,
            )
        ) { filter { eq("id", submissionId) } }
    }
}
