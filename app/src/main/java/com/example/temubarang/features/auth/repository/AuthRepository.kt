package com.example.temubarang.features.auth.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Repository berinteraksi dengan Supabase Auth (bukan tabel public.users). */
class AuthRepository(private val client: SupabaseClient) {
    val sessionStatus: Flow<SessionStatus> get() = client.auth.sessionStatus

    suspend fun signIn(email: String, password: String) = withContext(Dispatchers.IO) {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        Unit
    }

    suspend fun signUp(name: String, email: String, password: String) = withContext(Dispatchers.IO) {
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject { put("full_name", name) }
        }
        Unit
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        client.auth.signOut()
    }
}
