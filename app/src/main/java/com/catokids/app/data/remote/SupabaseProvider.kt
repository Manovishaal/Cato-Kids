package com.catokids.app.data.remote

import com.catokids.app.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseProvider {

    /** True when the build actually carries backend credentials. */
    val isConfigured: Boolean =
        BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL.ifBlank { "https://localhost" },
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY.ifBlank { "anon" },
        ) {
            install(Auth)
            install(Postgrest)
        }
    }
}
