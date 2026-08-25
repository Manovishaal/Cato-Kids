package com.catokids.app.data.repository

import com.catokids.app.data.curriculum.CatoCurriculum
import com.catokids.app.data.remote.LessonSeedDto
import com.catokids.app.data.remote.SupabaseProvider
import io.github.jan.supabase.postgrest.from

/**
 * Pushes the bundled curriculum into Supabase so teachers can assign lessons and
 * reports can name them. Row Level Security only lets an administrator write to
 * `lessons`, so this is a no-op for everyone else — it runs quietly when an admin
 * signs in and keeps the server catalogue in step with the shipped app.
 */
class CurriculumSyncRepository(private val auth: AuthRepository) {

    suspend fun pushIfAdmin(): Int {
        val profile = auth.profile.value ?: return 0
        if (profile.role != com.catokids.app.data.model.Role.ADMIN) return 0
        if (auth.isDemo.value || !SupabaseProvider.isConfigured) return 0

        val rows = CatoCurriculum.all.map { lesson ->
            LessonSeedDto(
                id = lesson.id,
                subjectId = lesson.subject.wire,
                grade = lesson.grade.wire,
                title = lesson.title,
                subtitle = lesson.subtitle.ifBlank { null },
                description = lesson.description.ifBlank { null },
                sortOrder = lesson.order,
                gameType = lesson.gameType.wire,
                content = lesson.content,
            )
        }
        return runCatching {
            // Chunked so a slow connection still makes progress.
            rows.chunked(40).forEach { batch ->
                SupabaseProvider.client.from("lessons").upsert(batch) { onConflict = "id" }
            }
            rows.size
        }.getOrDefault(0)
    }
}
