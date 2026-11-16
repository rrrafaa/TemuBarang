package com.example.temubarang.features.profile.repository

import com.example.temubarang.features.profile.model.Profile
import com.example.temubarang.features.profile.model.ReporterProfile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ProfileRepository(private val client: SupabaseClient) {
    private fun userId() = checkNotNull(client.auth.currentUserOrNull()?.id) { "Silakan login kembali." }

    suspend fun load(): Profile = withContext(Dispatchers.IO) {
        val id = userId()
        client.from("profiles").select { filter { eq("id", id) } }.decodeList<Profile>().singleOrNull()
            ?: Profile(id, "", "", "", "", false)
    }

    suspend fun save(profile: Profile): Profile = withContext(Dispatchers.IO) {
        require(profile.id == userId()) { "Profil hanya dapat diperbarui oleh pemiliknya." }
        client.from("profiles").upsert(profile) { select() }.decodeSingle<Profile>()
    }

    suspend fun reporter(foundItemId: String): ReporterProfile? = withContext(Dispatchers.IO) {
        client.postgrest.rpc("get_found_reporter", buildJsonObject { put("p_found_item_id", foundItemId) })
            .decodeList<ReporterProfile>().singleOrNull()
    }
}
