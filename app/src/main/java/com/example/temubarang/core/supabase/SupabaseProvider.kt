package com.example.temubarang.core.supabase

import com.example.temubarang.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

/** Satu client bersama. Jangan membuat client baru di setiap Composable. */
object SupabaseProvider {
    val isConfigured: Boolean
        get() {
            val url = BuildConfig.SUPABASE_URL.trim()
            val key = BuildConfig.SUPABASE_PUBLISHABLE_KEY.trim()
            return url.startsWith("https://") &&
                url != "https://PROJECT_REF.supabase.co" &&
                !url.contains("xxxxxxxx", ignoreCase = true) &&
                key.isNotBlank() &&
                !key.contains("xxxxx", ignoreCase = true) &&
                !key.contains("CONTOH", ignoreCase = true) &&
                !key.contains("your_", ignoreCase = true) &&
                !key.startsWith("sb_secret_") &&
                !key.contains("service_role", ignoreCase = true)
        }

    val client: SupabaseClient by lazy {
        require(isConfigured) { "Lengkapi SUPABASE_URL dan SUPABASE_PUBLISHABLE_KEY dalam local.properties" }
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
        }
    }
}
