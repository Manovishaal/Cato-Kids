package com.catokids.app.data.repository

import com.catokids.app.core.CatoResult
import com.catokids.app.core.catoRunCatching
import com.catokids.app.data.local.AppPreferences
import com.catokids.app.data.model.Grade
import com.catokids.app.data.model.Profile
import com.catokids.app.data.model.Role
import com.catokids.app.data.remote.ProfileDto
import com.catokids.app.data.remote.ProfileUpdateDto
import com.catokids.app.data.remote.SupabaseProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

/**
 * Email + password authentication against Supabase, with a fully offline
 * "explore" mode so the app is usable on a device with no account and no network.
 */
class AuthRepository(private val prefs: AppPreferences) {

    private val _profile = MutableStateFlow<Profile?>(null)
    val profile: StateFlow<Profile?> = _profile.asStateFlow()

    private val _isDemo = MutableStateFlow(false)
    val isDemo: StateFlow<Boolean> = _isDemo.asStateFlow()

    val isConfigured: Boolean get() = SupabaseProvider.isConfigured

    /** Restores a session at launch. Returns the signed-in profile, or null. */
    suspend fun restore(): Profile? {
        prefs.demoSession()?.let { (role, name, grade) ->
            val p = demoProfile(role, name, grade)
            _profile.value = p
            _isDemo.value = true
            return p
        }
        if (!isConfigured) return null
        return runCatching {
            val client = SupabaseProvider.client
            client.auth.awaitInitialization()
            val user = client.auth.currentUserOrNull() ?: return null
            fetchProfile(user.id)?.also {
                _profile.value = it
                _isDemo.value = false
            }
        }.getOrNull()
    }

    suspend fun signIn(email: String, password: String): CatoResult<Profile> = catoRunCatching {
        val client = SupabaseProvider.client
        client.auth.signInWith(Email) {
            this.email = email.trim()
            this.password = password
        }
        val user = client.auth.currentUserOrNull() ?: error("Sign-in did not return a user.")
        val profile = fetchProfile(user.id) ?: Profile(
            id = user.id,
            role = Role.STUDENT,
            fullName = email.substringBefore('@'),
            email = email,
        )
        _profile.value = profile
        _isDemo.value = false
        prefs.setLastRole(profile.role)
        profile
    }

    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        role: Role,
        grade: Grade?,
        phone: String?,
    ): CatoResult<Profile> = catoRunCatching {
        val client = SupabaseProvider.client
        client.auth.signUpWith(Email) {
            this.email = email.trim()
            this.password = password
            data = buildJsonObject {
                put("full_name", JsonPrimitive(fullName.trim()))
                put("role", JsonPrimitive(role.wire))
                if (grade != null) put("grade", JsonPrimitive(grade.wire))
                if (!phone.isNullOrBlank()) put("phone", JsonPrimitive(phone.trim()))
            }
        }

        // Projects with "confirm email" switched on return no session yet.
        val user = client.auth.currentUserOrNull()
            ?: error("Account created. Please check your email to confirm it, then sign in.")

        val profile = fetchProfile(user.id) ?: Profile(
            id = user.id, role = role, fullName = fullName, email = email, grade = grade,
        )
        _profile.value = profile
        _isDemo.value = false
        prefs.setLastRole(role)
        profile
    }

    suspend fun sendPasswordReset(email: String): CatoResult<Unit> = catoRunCatching {
        SupabaseProvider.client.auth.resetPasswordForEmail(email.trim())
    }

    suspend fun signOut() {
        if (_isDemo.value) {
            prefs.clearDemoSession()
        } else if (isConfigured) {
            runCatching { SupabaseProvider.client.auth.signOut() }
        }
        _isDemo.value = false
        _profile.value = null
    }

    // ---------- offline explore mode ----------

    suspend fun startDemo(role: Role, name: String, grade: Grade?): Profile {
        val p = demoProfile(role, name, grade)
        prefs.saveDemoSession(role, name, grade)
        prefs.setLastRole(role)
        _profile.value = p
        _isDemo.value = true
        return p
    }

    private fun demoProfile(role: Role, name: String, grade: Grade?) = Profile(
        id = "demo-${role.wire}",
        role = role,
        fullName = name,
        email = null,
        grade = grade ?: if (role == Role.STUDENT) Grade.LKG else null,
    )

    // ---------- profile ----------

    suspend fun refreshProfile(): Profile? {
        val current = _profile.value ?: return null
        if (_isDemo.value || !isConfigured) return current
        return fetchProfile(current.id)?.also { _profile.value = it }
    }

    suspend fun updateProfile(
        fullName: String? = null,
        phone: String? = null,
        grade: Grade? = null,
    ): CatoResult<Profile> = catoRunCatching {
        val current = _profile.value ?: error("You are not signed in.")
        val updated = current.copy(
            fullName = fullName ?: current.fullName,
            phone = phone ?: current.phone,
            grade = grade ?: current.grade,
        )
        if (!_isDemo.value && isConfigured) {
            SupabaseProvider.client.from("profiles").update(
                ProfileUpdateDto(
                    fullName = fullName,
                    phone = phone,
                    grade = grade?.wire,
                )
            ) { filter { eq("id", current.id) } }
        }
        _profile.value = updated
        updated
    }

    /** Locally bumps the reward counters and mirrors them to Supabase when possible. */
    suspend fun addRewards(stars: Int, coins: Int) {
        val current = _profile.value ?: return
        val updated = current.copy(stars = current.stars + stars, coins = current.coins + coins)
        _profile.value = updated
        if (_isDemo.value || !isConfigured) return
        runCatching {
            SupabaseProvider.client.from("profiles").update(
                ProfileUpdateDto(stars = updated.stars, coins = updated.coins)
            ) { filter { eq("id", current.id) } }
        }
    }

    private suspend fun fetchProfile(userId: String): Profile? = runCatching {
        SupabaseProvider.client.from("profiles")
            .select { filter { eq("id", userId) } }
            .decodeSingleOrNull<ProfileDto>()
            ?.toDomain()
    }.getOrNull()
}
