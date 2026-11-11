package com.example.temubarang.features.home.repository

import com.example.temubarang.features.home.model.HomeLostItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Hanya baca data yang memang boleh dilihat pengguna terautentikasi menurut RLS. */
class HomeRepository(private val client: SupabaseClient) {
    suspend fun listLostItems(): List<HomeLostItem> = withContext(Dispatchers.IO) {
        client.from("lost_items").select {
            order(column = "created_at", order = Order.DESCENDING)
        }.decodeList<HomeLostItem>()
    }

    fun photoUrl(path: String?): String? = path?.takeIf { it.isNotBlank() }?.let {
        client.storage.from("temubarang-images").publicUrl(it)
    }
}
