package com.example.temubarang.features.lost.repository

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.temubarang.features.lost.model.LostItemChanges
import com.example.temubarang.features.lost.model.LostItemChangesWithPhoto
import com.example.temubarang.features.lost.model.LostItem
import com.example.temubarang.features.lost.model.LostStatusChange
import com.example.temubarang.features.lost.model.NewLostItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID

/** Semua query, upload, dan file I/O berada di Repository, bukan di Composable. */
class LostItemRepository(
    private val client: SupabaseClient,
    private val contentResolver: ContentResolver
) {
    private companion object {
        const val TABLE = "lost_items"
        const val BUCKET = "temubarang-images"
        const val MAX_BYTES = 5 * 1024 * 1024
        const val MAX_SIDE = 1600
    }

    fun signedInUserId(): String? = client.auth.currentUserOrNull()?.id

    private fun requireUserId(): String = signedInUserId()
        ?: throw IllegalStateException("Sesi belum tersedia. Silakan login terlebih dahulu.")

    suspend fun list(): List<LostItem> = withContext(Dispatchers.IO) {
        requireUserId()
        client.from(TABLE).select {
            order(column = "created_at", order = Order.DESCENDING)
        }.decodeList<LostItem>()
    }

    suspend fun get(id: String): LostItem = withContext(Dispatchers.IO) {
        requireUserId()
        client.from(TABLE).select { filter { eq("id", id) } }.decodeSingle<LostItem>()
    }

    suspend fun create(
        title: String,
        description: String,
        location: String,
        date: String,
        imageUri: Uri?
    ) = withContext(Dispatchers.IO) {
        val userId = requireUserId()
        val path = imageUri?.let { uploadPhoto(userId, it) }
        try {
            val inserted = client.from(TABLE).insert(
                NewLostItem(
                    reporterId = userId,
                    title = title.trim(),
                    description = description.trim(),
                    lastSeenLocation = location.trim(),
                    lostAt = date.trim(),
                    imagePath = path
                )
            ) { select() }.decodeList<LostItem>().singleOrNull()
            check(inserted != null) {
                "Laporan belum tersimpan. Periksa izin pengguna dan koneksi database."
            }
            inserted
        } catch (e: Exception) {
            // Jangan meninggalkan foto yatim bila baris database gagal disimpan.
            if (path != null) runCatching { client.storage.from(BUCKET).delete(path) }
            throw e
        }
    }

    suspend fun updateStatus(id: String, status: String) = withContext(Dispatchers.IO) {
        val userId = requireUserId()
        require(status in setOf("open", "found", "closed")) { "Status laporan tidak valid." }
        val updated = client.from(TABLE).update(LostStatusChange(status)) {
            filter {
                eq("id", id)
                eq("reporter_id", userId)
            }
            select()
        }.decodeList<LostItem>().singleOrNull()
        check(updated != null) { "Status tidak berubah. Laporan mungkin sudah dihapus atau akses ditolak." }
        updated
    }

    suspend fun update(
        id: String, title: String, description: String,
        location: String, date: String, newImageUri: Uri?, oldImagePath: String?
    ): LostItem = withContext(Dispatchers.IO) {
        val userId = requireUserId()
        if (newImageUri == null) {
            client.from(TABLE).update(
                LostItemChanges(title.trim(), description.trim(), location.trim(), date.trim())
            ) {
                filter { eq("id", id); eq("reporter_id", userId) }
                select()
            }.decodeList<LostItem>().singleOrNull()
                ?: throw IllegalStateException("Perubahan laporan tidak tersimpan. Periksa akses laporan.")
        } else {
            val newPath = uploadPhoto(userId, newImageUri)
            val updated = try {
                client.from(TABLE).update(
                    LostItemChangesWithPhoto(title.trim(), description.trim(), location.trim(), date.trim(), newPath)
                ) {
                    filter { eq("id", id); eq("reporter_id", userId) }
                    select()
                }.decodeList<LostItem>().singleOrNull()
                    ?: throw IllegalStateException("Perubahan foto belum tersimpan. Periksa akses laporan.")
            } catch (e: Exception) {
                runCatching { client.storage.from(BUCKET).delete(newPath) }
                throw e
            }
            if (!oldImagePath.isNullOrBlank()) {
                runCatching { client.storage.from(BUCKET).delete(oldImagePath) }
            }
            updated
        }
    }

    suspend fun delete(id: String, imagePath: String?) = withContext(Dispatchers.IO) {
        val userId = requireUserId()
        val deleted = client.from(TABLE).delete {
            filter {
                eq("id", id)
                eq("reporter_id", userId)
            }
            select()
        }.decodeList<LostItem>().singleOrNull()
        check(deleted != null) { "Laporan tidak terhapus. Periksa akses dan status laporan." }

        val savedPhotoPath = deleted.imagePath?.takeIf { it.startsWith("$userId/lost/") }
        if (savedPhotoPath != null) {
            runCatching { client.storage.from(BUCKET).delete(savedPhotoPath) }
        }
        Unit
    }

    fun publicPhotoUrl(path: String?): String? = path?.takeIf { it.isNotBlank() }?.let {
        client.storage.from(BUCKET).publicUrl(it)
    }

    /** Foto dikecilkan dan dikompres ke JPEG agar selalu di bawah batas 5 MB Storage. */
    private fun compressToJpeg(uri: Uri): ByteArray {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        val boundsStream = contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Foto tidak dapat dibaca.")
        boundsStream.use { BitmapFactory.decodeStream(it, null, bounds) }
        require(bounds.outWidth > 0 && bounds.outHeight > 0) {
            "Foto tidak valid. Pilih gambar lain."
        }
        var sample = 1
        while (bounds.outWidth / sample > MAX_SIDE || bounds.outHeight / sample > MAX_SIDE) {
            sample *= 2
        }
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        val bitmap = contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: throw IllegalArgumentException("Foto gagal diproses. Pilih gambar lain.")
        try {
            for (quality in listOf(85, 70, 55, 40)) {
                val output = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
                val bytes = output.toByteArray()
                if (bytes.isNotEmpty() && bytes.size <= MAX_BYTES) return bytes
            }
        } finally {
            bitmap.recycle()
        }
        throw IllegalArgumentException("Foto masih lebih besar dari 5 MB. Pilih gambar lain.")
    }

    private suspend fun uploadPhoto(userId: String, uri: Uri): String {
        val bytes = compressToJpeg(uri)
        val path = "$userId/lost/${UUID.randomUUID()}.jpg"
        client.storage.from(BUCKET).upload(path, bytes) {
            contentType = ContentType.Image.JPEG
            upsert = false
        }
        return path
    }
}