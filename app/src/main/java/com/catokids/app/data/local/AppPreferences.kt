package com.catokids.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.catokids.app.data.model.Grade
import com.catokids.app.data.model.LessonProgress
import com.catokids.app.data.model.Role
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cato_kids_prefs")

@Serializable
private data class StoredProgress(
    val lessonId: String,
    val stars: Int,
    val bestScore: Int,
    val attempts: Int,
    val secondsSpent: Int,
    val completed: Boolean,
    val lastPlayedAtMillis: Long,
)

/**
 * Explicit serializer rather than the reified `encodeToString<T>` helper — that one
 * moved between a top-level extension and a member across kotlinx-serialization
 * versions, and resolves to the wrong overload on some of them.
 */
private val PROGRESS_LIST = ListSerializer(StoredProgress.serializer())

/**
 * Local preferences plus an offline mirror of the child's progress, so lessons keep
 * working (and keep scoring) with no connection at all.
 */
class AppPreferences(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private object Keys {
        val SOUND         = booleanPreferencesKey("sound_on")
        val MUSIC         = booleanPreferencesKey("music_on")
        val ONBOARDED     = booleanPreferencesKey("onboarded")
        val LAST_ROLE     = stringPreferencesKey("last_role")
        val DEMO_ROLE     = stringPreferencesKey("demo_role")
        val DEMO_NAME     = stringPreferencesKey("demo_name")
        val DEMO_GRADE    = stringPreferencesKey("demo_grade")
        val SELECTED_CHILD = stringPreferencesKey("selected_child")
        fun progress(userId: String) = stringPreferencesKey("progress_$userId")
    }

    val soundOn: Flow<Boolean> = context.dataStore.data.map { it[Keys.SOUND] ?: true }
    val musicOn: Flow<Boolean> = context.dataStore.data.map { it[Keys.MUSIC] ?: false }
    val onboarded: Flow<Boolean> = context.dataStore.data.map { it[Keys.ONBOARDED] ?: false }
    val lastRole: Flow<Role?> = context.dataStore.data.map { p -> p[Keys.LAST_ROLE]?.let { Role.fromWire(it) } }

    suspend fun setSound(on: Boolean) = context.dataStore.edit { it[Keys.SOUND] = on }.let { }
    suspend fun setMusic(on: Boolean) = context.dataStore.edit { it[Keys.MUSIC] = on }.let { }
    suspend fun setOnboarded(v: Boolean) = context.dataStore.edit { it[Keys.ONBOARDED] = v }.let { }
    suspend fun setLastRole(role: Role) = context.dataStore.edit { it[Keys.LAST_ROLE] = role.wire }.let { }
    suspend fun setSelectedChild(id: String?) = context.dataStore.edit { p ->
        if (id == null) p.remove(Keys.SELECTED_CHILD) else p[Keys.SELECTED_CHILD] = id
    }.let { }
    val selectedChild: Flow<String?> = context.dataStore.data.map { it[Keys.SELECTED_CHILD] }

    // ---------- demo (offline) session ----------

    suspend fun saveDemoSession(role: Role, name: String, grade: Grade?) = context.dataStore.edit {
        it[Keys.DEMO_ROLE] = role.wire
        it[Keys.DEMO_NAME] = name
        if (grade != null) it[Keys.DEMO_GRADE] = grade.wire else it.remove(Keys.DEMO_GRADE)
    }.let { }

    suspend fun clearDemoSession() = context.dataStore.edit {
        it.remove(Keys.DEMO_ROLE); it.remove(Keys.DEMO_NAME); it.remove(Keys.DEMO_GRADE)
    }.let { }

    suspend fun demoSession(): Triple<Role, String, Grade?>? {
        val prefs = context.dataStore.data.first()
        val role = prefs[Keys.DEMO_ROLE] ?: return null
        return Triple(
            Role.fromWire(role),
            prefs[Keys.DEMO_NAME] ?: "Explorer",
            Grade.fromWire(prefs[Keys.DEMO_GRADE]),
        )
    }

    // ---------- offline progress mirror ----------

    fun progressFlow(userId: String): Flow<List<LessonProgress>> =
        context.dataStore.data.map { prefs -> decode(userId, prefs[Keys.progress(userId)]) }

    suspend fun progressNow(userId: String): List<LessonProgress> =
        decode(userId, context.dataStore.data.first()[Keys.progress(userId)])

    suspend fun saveProgress(userId: String, progress: LessonProgress) {
        context.dataStore.edit { prefs ->
            val current = decode(userId, prefs[Keys.progress(userId)])
                .filterNot { it.lessonId == progress.lessonId }
            val next = current + progress
            prefs[Keys.progress(userId)] =
                json.encodeToString(PROGRESS_LIST, next.map { it.toStored() })
        }
    }

    suspend fun replaceProgress(userId: String, all: List<LessonProgress>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.progress(userId)] =
                json.encodeToString(PROGRESS_LIST, all.map { it.toStored() })
        }
    }

    private fun decode(userId: String, raw: String?): List<LessonProgress> = runCatching {
        if (raw.isNullOrBlank()) emptyList()
        else json.decodeFromString(PROGRESS_LIST, raw).map {
            LessonProgress(
                studentId = userId,
                lessonId = it.lessonId,
                stars = it.stars,
                bestScore = it.bestScore,
                attempts = it.attempts,
                secondsSpent = it.secondsSpent,
                completed = it.completed,
                lastPlayedAtMillis = it.lastPlayedAtMillis,
            )
        }
    }.getOrDefault(emptyList())

    private fun LessonProgress.toStored() = StoredProgress(
        lessonId, stars, bestScore, attempts, secondsSpent, completed, lastPlayedAtMillis,
    )
}
