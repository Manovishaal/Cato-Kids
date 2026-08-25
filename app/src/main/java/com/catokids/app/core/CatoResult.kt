package com.catokids.app.core

/** Small Result type so UI layers never see raw exceptions. */
sealed interface CatoResult<out T> {
    data class Ok<T>(val value: T) : CatoResult<T>
    data class Err(val message: String, val cause: Throwable? = null) : CatoResult<Nothing>

    val isOk: Boolean get() = this is Ok
    fun getOrNull(): T? = (this as? Ok)?.value
}

inline fun <T> catoRunCatching(friendly: (Throwable) -> String = ::friendlyMessage, block: () -> T): CatoResult<T> =
    try {
        CatoResult.Ok(block())
    } catch (ce: kotlinx.coroutines.CancellationException) {
        throw ce
    } catch (t: Throwable) {
        CatoResult.Err(friendly(t), t)
    }

fun friendlyMessage(t: Throwable): String {
    val raw = t.message.orEmpty()
    return when {
        raw.contains("Invalid login credentials", true) -> "That email and password don't match. Try again."
        raw.contains("User already registered", true)   -> "That email already has an account. Try signing in."
        raw.contains("Password should be", true)        -> "Please choose a password with at least 6 characters."
        raw.contains("Email not confirmed", true)       -> "Please confirm your email address first."
        raw.contains("Unable to resolve host", true) ||
        raw.contains("failed to connect", true) ||
        raw.contains("timeout", true)                   -> "No internet connection. Check your network and try again."
        raw.isBlank()                                   -> "Something went wrong. Please try again."
        else -> raw.take(160)
    }
}
