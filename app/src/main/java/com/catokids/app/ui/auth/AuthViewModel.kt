package com.catokids.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.catokids.app.core.AppContainer
import com.catokids.app.core.CatoResult
import com.catokids.app.data.model.Grade
import com.catokids.app.data.model.Profile
import com.catokids.app.data.model.Role
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val info: String? = null,
    val profile: Profile? = null,
    val restored: Boolean = false,
)

class AuthViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    val backendConfigured: Boolean get() = container.auth.isConfigured

    init {
        viewModelScope.launch {
            val profile = container.auth.restore()
            _state.value = _state.value.copy(profile = profile, restored = true)
            profile?.let { container.progress.sync(it.id) }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(error = null, info = null)
    }

    fun signIn(email: String, password: String) {
        if (!validate(email, password)) return
        _state.value = _state.value.copy(loading = true, error = null, info = null)
        viewModelScope.launch {
            when (val r = container.auth.signIn(email, password)) {
                is CatoResult.Ok -> {
                    _state.value = _state.value.copy(loading = false, profile = r.value)
                    container.progress.sync(r.value.id)
                }
                is CatoResult.Err -> _state.value = _state.value.copy(loading = false, error = r.message)
            }
        }
    }

    fun register(
        fullName: String,
        email: String,
        password: String,
        confirm: String,
        role: Role,
        grade: Grade?,
        phone: String?,
    ) {
        when {
            fullName.isBlank() -> return fail("Please enter a name.")
            !validate(email, password) -> return
            password != confirm -> return fail("The two passwords don't match.")
            role == Role.STUDENT && grade == null -> return fail("Please choose Pre-KG, LKG or UKG.")
        }
        _state.value = _state.value.copy(loading = true, error = null, info = null)
        viewModelScope.launch {
            when (val r = container.auth.register(fullName, email, password, role, grade, phone)) {
                is CatoResult.Ok -> {
                    _state.value = _state.value.copy(loading = false, profile = r.value)
                    container.progress.sync(r.value.id)
                }
                is CatoResult.Err -> _state.value = _state.value.copy(loading = false, error = r.message)
            }
        }
    }

    fun sendReset(email: String) {
        if (email.isBlank()) return fail("Please enter your email address.")
        _state.value = _state.value.copy(loading = true, error = null, info = null)
        viewModelScope.launch {
            when (val r = container.auth.sendPasswordReset(email)) {
                is CatoResult.Ok -> _state.value = _state.value.copy(
                    loading = false,
                    info = "We've sent a reset link to $email.",
                )
                is CatoResult.Err -> _state.value = _state.value.copy(loading = false, error = r.message)
            }
        }
    }

    fun exploreOffline(role: Role, grade: Grade? = null) {
        viewModelScope.launch {
            val p = container.auth.startDemo(role, demoNameFor(role), grade)
            _state.value = _state.value.copy(profile = p, error = null, info = null)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            container.auth.signOut()
            _state.value = AuthUiState(restored = true)
        }
    }

    private fun demoNameFor(role: Role) = when (role) {
        Role.STUDENT -> "Little Explorer"
        Role.TEACHER -> "Ms. Priya"
        Role.PARENT  -> "Anita Kumar"
        Role.ADMIN   -> "Platform Admin"
        Role.SCHOOL  -> "Mother Goose Primary"
    }

    private fun validate(email: String, password: String): Boolean {
        if (!email.contains('@') || !email.contains('.')) {
            fail("Please enter a valid email address."); return false
        }
        if (password.length < 6) {
            fail("Password needs at least 6 characters."); return false
        }
        return true
    }

    private fun fail(message: String) {
        _state.value = _state.value.copy(loading = false, error = message)
    }

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = AuthViewModel(container) as T
        }
    }
}
