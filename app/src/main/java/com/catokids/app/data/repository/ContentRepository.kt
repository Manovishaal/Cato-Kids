package com.catokids.app.data.repository

import com.catokids.app.core.CatoResult
import com.catokids.app.core.catoRunCatching
import com.catokids.app.data.model.Activity
import com.catokids.app.data.model.CustomGame
import com.catokids.app.data.model.ExtraCourse
import com.catokids.app.data.remote.ActivityDto
import com.catokids.app.data.remote.CustomGameDto
import com.catokids.app.data.remote.ExtraCourseDto
import com.catokids.app.data.remote.SupabaseProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

/**
 * The three things a teacher, school or administrator can build for their class:
 * custom games (a new lesson using one of the nine engines), extra courses (a
 * curated bundle of existing lessons) and activities (an offline task). Every
 * write is scoped to the signed-in creator by Row Level Security — this class
 * just shapes the requests, it doesn't re-check who's allowed to do what.
 */
class ContentRepository(private val auth: AuthRepository) {

    private val online: Boolean
        get() = SupabaseProvider.isConfigured && !auth.isDemo.value

    // ---------- custom games ----------

    suspend fun myGames(): List<CustomGame> {
        val uid = auth.profile.value?.id ?: return emptyList()
        if (!online) return SampleData.demoGames
        return runCatching {
            SupabaseProvider.client.from("custom_games")
                .select { filter { eq("created_by", uid) }; order("created_at", Order.DESCENDING) }
                .decodeList<CustomGameDto>().map { it.toDomain() }
        }.getOrDefault(emptyList())
    }

    suspend fun game(id: String): CustomGame? {
        if (id.isBlank()) return null
        if (!online) return SampleData.demoGames.firstOrNull { it.id == id }
        return runCatching {
            SupabaseProvider.client.from("custom_games")
                .select { filter { eq("id", id) } }
                .decodeSingleOrNull<CustomGameDto>()?.toDomain()
        }.getOrNull() ?: SampleData.demoGames.firstOrNull { it.id == id }
    }

    suspend fun saveGame(game: CustomGame): CatoResult<CustomGame> = catoRunCatching {
        if (!online) return@catoRunCatching game.copy(id = game.id.ifBlank { "local-game-${System.currentTimeMillis()}" })
        val dto = CustomGameDto.from(game)
        if (game.id.isBlank()) {
            SupabaseProvider.client.from("custom_games").insert(dto) { select() }
                .decodeSingle<CustomGameDto>().toDomain()
        } else {
            SupabaseProvider.client.from("custom_games").update(dto) { filter { eq("id", game.id) }; select() }
                .decodeSingle<CustomGameDto>().toDomain()
        }
    }

    suspend fun deleteGame(id: String): CatoResult<Unit> = catoRunCatching {
        if (!online) return@catoRunCatching Unit
        SupabaseProvider.client.from("custom_games").delete { filter { eq("id", id) } }
    }

    // ---------- extra courses ----------

    suspend fun myCourses(): List<ExtraCourse> {
        val uid = auth.profile.value?.id ?: return emptyList()
        if (!online) return SampleData.demoCourses
        return runCatching {
            SupabaseProvider.client.from("extra_courses")
                .select { filter { eq("created_by", uid) }; order("created_at", Order.DESCENDING) }
                .decodeList<ExtraCourseDto>().map { it.toDomain() }
        }.getOrDefault(emptyList())
    }

    suspend fun course(id: String): ExtraCourse? {
        if (id.isBlank()) return null
        if (!online) return SampleData.demoCourses.firstOrNull { it.id == id }
        return runCatching {
            SupabaseProvider.client.from("extra_courses")
                .select { filter { eq("id", id) } }
                .decodeSingleOrNull<ExtraCourseDto>()?.toDomain()
        }.getOrNull() ?: SampleData.demoCourses.firstOrNull { it.id == id }
    }

    suspend fun saveCourse(course: ExtraCourse): CatoResult<ExtraCourse> = catoRunCatching {
        if (!online) return@catoRunCatching course.copy(id = course.id.ifBlank { "local-course-${System.currentTimeMillis()}" })
        val dto = ExtraCourseDto.from(course)
        if (course.id.isBlank()) {
            SupabaseProvider.client.from("extra_courses").insert(dto) { select() }
                .decodeSingle<ExtraCourseDto>().toDomain()
        } else {
            SupabaseProvider.client.from("extra_courses").update(dto) { filter { eq("id", course.id) }; select() }
                .decodeSingle<ExtraCourseDto>().toDomain()
        }
    }

    suspend fun deleteCourse(id: String): CatoResult<Unit> = catoRunCatching {
        if (!online) return@catoRunCatching Unit
        SupabaseProvider.client.from("extra_courses").delete { filter { eq("id", id) } }
    }

    // ---------- activities ----------

    suspend fun myActivities(): List<Activity> {
        val uid = auth.profile.value?.id ?: return emptyList()
        if (!online) return SampleData.demoActivities
        return runCatching {
            SupabaseProvider.client.from("activities")
                .select { filter { eq("created_by", uid) }; order("created_at", Order.DESCENDING) }
                .decodeList<ActivityDto>().map { it.toDomain() }
        }.getOrDefault(emptyList())
    }

    suspend fun activity(id: String): Activity? {
        if (id.isBlank()) return null
        if (!online) return SampleData.demoActivities.firstOrNull { it.id == id }
        return runCatching {
            SupabaseProvider.client.from("activities")
                .select { filter { eq("id", id) } }
                .decodeSingleOrNull<ActivityDto>()?.toDomain()
        }.getOrNull() ?: SampleData.demoActivities.firstOrNull { it.id == id }
    }

    suspend fun saveActivity(activity: Activity): CatoResult<Activity> = catoRunCatching {
        if (!online) return@catoRunCatching activity.copy(id = activity.id.ifBlank { "local-activity-${System.currentTimeMillis()}" })
        val dto = ActivityDto.from(activity)
        if (activity.id.isBlank()) {
            SupabaseProvider.client.from("activities").insert(dto) { select() }
                .decodeSingle<ActivityDto>().toDomain()
        } else {
            SupabaseProvider.client.from("activities").update(dto) { filter { eq("id", activity.id) }; select() }
                .decodeSingle<ActivityDto>().toDomain()
        }
    }

    suspend fun deleteActivity(id: String): CatoResult<Unit> = catoRunCatching {
        if (!online) return@catoRunCatching Unit
        SupabaseProvider.client.from("activities").delete { filter { eq("id", id) } }
    }
}
