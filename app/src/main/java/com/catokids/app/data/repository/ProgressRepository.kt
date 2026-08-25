package com.catokids.app.data.repository

import com.catokids.app.data.curriculum.CatoCurriculum
import com.catokids.app.data.local.AppPreferences
import com.catokids.app.data.model.Grade
import com.catokids.app.data.model.LessonProgress
import com.catokids.app.data.model.SubjectId
import com.catokids.app.data.remote.LessonProgressDto
import com.catokids.app.data.remote.QuizAttemptDto
import com.catokids.app.data.remote.SupabaseProvider
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow

/**
 * Offline-first progress. Every result is written to the device immediately and
 * pushed to Supabase opportunistically; a failed push never loses the child's stars.
 */
class ProgressRepository(
    private val prefs: AppPreferences,
    private val auth: AuthRepository,
) {

    fun observe(userId: String): Flow<List<LessonProgress>> = prefs.progressFlow(userId)

    suspend fun snapshot(userId: String): List<LessonProgress> = prefs.progressNow(userId)

    /** Pulls the server copy and merges it with local (best score wins). */
    suspend fun sync(userId: String) {
        if (auth.isDemo.value || !SupabaseProvider.isConfigured) return
        runCatching {
            val remote = SupabaseProvider.client.from("lesson_progress")
                .select { filter { eq("student_id", userId) } }
                .decodeList<LessonProgressDto>()
                .map { it.toDomain() }

            val local = prefs.progressNow(userId)
            val merged = (remote + local)
                .groupBy { it.lessonId }
                .map { (_, rows) ->
                    rows.reduce { a, b ->
                        a.copy(
                            stars = maxOf(a.stars, b.stars),
                            bestScore = maxOf(a.bestScore, b.bestScore),
                            attempts = maxOf(a.attempts, b.attempts),
                            secondsSpent = maxOf(a.secondsSpent, b.secondsSpent),
                            completed = a.completed || b.completed,
                            lastPlayedAtMillis = maxOf(a.lastPlayedAtMillis, b.lastPlayedAtMillis),
                        )
                    }
                }
            prefs.replaceProgress(userId, merged)
        }
    }

    suspend fun recordResult(
        userId: String,
        lessonId: String,
        stars: Int,
        score: Int,
        correct: Int,
        total: Int,
        seconds: Int,
    ): LessonProgress {
        val existing = prefs.progressNow(userId).firstOrNull { it.lessonId == lessonId }
        val merged = LessonProgress(
            studentId = userId,
            lessonId = lessonId,
            stars = maxOf(stars, existing?.stars ?: 0),
            bestScore = maxOf(score, existing?.bestScore ?: 0),
            attempts = (existing?.attempts ?: 0) + 1,
            secondsSpent = (existing?.secondsSpent ?: 0) + seconds,
            completed = true,
            lastPlayedAtMillis = System.currentTimeMillis(),
        )
        prefs.saveProgress(userId, merged)

        val gainedStars = merged.stars - (existing?.stars ?: 0)
        auth.addRewards(stars = maxOf(0, gainedStars), coins = correct * 2)

        if (!auth.isDemo.value && SupabaseProvider.isConfigured) {
            runCatching {
                SupabaseProvider.client.from("lesson_progress")
                    .upsert(LessonProgressDto.from(merged)) { onConflict = "student_id,lesson_id" }
                SupabaseProvider.client.from("quiz_attempts").insert(
                    QuizAttemptDto(
                        studentId = userId,
                        lessonId = lessonId,
                        score = score,
                        totalQuestions = total,
                        correctCount = correct,
                        wrongCount = total - correct,
                        durationSeconds = seconds,
                    )
                )
            }
        }
        return merged
    }

    // ---------- derived stats ----------

    fun summaryFor(progress: List<LessonProgress>, grade: Grade): ProgressSummary {
        val lessons = CatoCurriculum.forGrade(grade)
        val ids = lessons.map { it.id }.toSet()
        val mine = progress.filter { it.lessonId in ids }
        val completed = mine.count { it.completed }
        return ProgressSummary(
            totalLessons = lessons.size,
            completedLessons = completed,
            stars = mine.sumOf { it.stars },
            maxStars = lessons.size * 3,
            averageScore = if (mine.isEmpty()) 0 else mine.sumOf { it.bestScore } / mine.size,
            minutesPlayed = mine.sumOf { it.secondsSpent } / 60,
        )
    }

    fun subjectBreakdown(progress: List<LessonProgress>, grade: Grade): Map<SubjectId, ProgressSummary> =
        SubjectId.entries.associateWith { subject ->
            val lessons = CatoCurriculum.forGradeAndSubject(grade, subject)
            val ids = lessons.map { it.id }.toSet()
            val mine = progress.filter { it.lessonId in ids }
            ProgressSummary(
                totalLessons = lessons.size,
                completedLessons = mine.count { it.completed },
                stars = mine.sumOf { it.stars },
                maxStars = lessons.size * 3,
                averageScore = if (mine.isEmpty()) 0 else mine.sumOf { it.bestScore } / mine.size,
                minutesPlayed = mine.sumOf { it.secondsSpent } / 60,
            )
        }
}

data class ProgressSummary(
    val totalLessons: Int = 0,
    val completedLessons: Int = 0,
    val stars: Int = 0,
    val maxStars: Int = 0,
    val averageScore: Int = 0,
    val minutesPlayed: Int = 0,
) {
    val percent: Int get() = if (totalLessons == 0) 0 else (completedLessons * 100) / totalLessons
    val fraction: Float get() = if (totalLessons == 0) 0f else completedLessons.toFloat() / totalLessons
}
