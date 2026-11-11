package com.example.temubarang.features.found.repository

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.temubarang.features.found.model.FoundItem
import com.example.temubarang.features.found.model.FoundItemChanges
import com.example.temubarang.features.found.model.NewFoundItem
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

/** Semua query/network/file I/O berada di Repository, bukan di Composable. */
class FoundItemRepository(
    private val client: SupabaseClient,
    private val contentResolver: ContentResolver
) {
    private companion object {
        const val TABLE = "found_items"
        const val BUCKET = "temubarang-images"
        const val MAX_BYTES = 5 * 1024 * 1024
    }

    fun signedInUserId(): String? = client.auth.currentUserOrNull()?.id

    private fun requireUserId(): String = signedInUserId()
        ?: throw IllegalStateException("Sesi belum tersedia. Silakan login terlebih dahulu.")

    suspend fun list(): List<FoundItem> = withContext(Dispatchers.IO) {
        client.from(TABLE).select {
            order(column = "created_at", order = Order.DESCENDING)
        }.decodeList<FoundItem>()
    }

    suspend fun get(id: String): FoundItem = withContext(Dispatchers.IO) {
        client.from(TABLE).select {
            filter { eq("id", id) }
        }.decodeSingle<FoundItem>()
    }

    fun publicPhotoUrl(path: String?): String? = path?.takeIf { it.isNotBlank() }?.let {
        client.storage.from(BUCKET).publicUrl(it)
    }

    suspend fun create(
        title: String, description: String, location: String, date: String, imageUri: Uri?
    ) = withContext(Dispatchers.IO) {
        val userId = requireUserId()
        // Sesuai RLS Storage: <auth.uid()>/found/<uuid>.<ext>
        val path = imageUri?.let { uploadPhoto(userId, it) }
        client.from(TABLE).insert(
            NewFoundItem(
                reporterId = userId,
                title = title.trim(),
                description = description.trim(),
                foundLocation = location.trim(),
                foundAt = date.trim(),
                imagePath = path
            )
        )
        Unit
    }

    suspend fun update(
        id: String, title: String, description: String,
        location: String, date: String, status: String
    ) = withContext(Dispatchers.IO) {
        val userId = requireUserId()
        require(status in setOf("open", "returned", "closed")) { "Status laporan tidak valid." }
        client.from(TABLE).update(
            FoundItemChanges(
                title.trim(), description.trim(), location.trim(), date.trim(), status
            )
        ) {
            filter {
                eq("id", id)
                eq("reporter_id", userId)
            }
        }
        Unit
    }

    /**
     * Untuk foto kamera >5 MB, decode dengan sampling lalu encode ulang JPEG agar
     * sesuai batas Storage. File sumber tidak diubah; gambar galeri kecil tetap asli.
     */
    private fun compressLargeImage(uri: Uri): ByteArray {
        val dimensions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        val boundsStream = contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Foto tidak dapat dibaca.")
        boundsStream.use {
            // Saat inJustDecodeBounds=true, decodeStream memang mengembalikan null.
            BitmapFactory.decodeStream(it, null, dimensions)
        }
        require(dimensions.outWidth > 0 && dimensions.outHeight > 0) {
            "Foto tidak valid. Coba ambil foto lagi."
        }
        var sample = 1
        while (dimensions.outWidth / sample > 1920 || dimensions.outHeight / sample > 1920) {
            sample *= 2
        }
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        val bitmap = contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: throw IllegalArgumentException("Foto gagal diproses. Gunakan gambar lain.")
        try {
            for (quality in listOf(85, 72, 60, 45)) {
                val output = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
                val bytes = output.toByteArray()
                if (bytes.size <= MAX_BYTES && bytes.isNotEmpty()) return bytes
            }
        } finally {
            bitmap.recycle()
        }
        throw IllegalArgumentException("Foto masih lebih besar dari 5 MB. Gunakan resolusi kamera lebih rendah.")
    }

    private suspend fun uploadPhoto(userId: String, uri: Uri): String {
        val mime = contentResolver.getType(uri)
            ?: throw IllegalArgumentException("Format foto tidak diketahui. Gunakan JPG, PNG, atau WEBP.")
        val extension = when (mime) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> throw IllegalArgumentException("Hanya foto JPG, PNG, atau WEBP yang didukung.")
        }
        // Baca sampai batas 5 MB. Bila melebihi, gunakan kompresi agar upload tetap dapat dilakukan.
        val output = ByteArrayOutputStream()
        var tooLarge = false
        contentResolver.openInputStream(uri)?.use { stream ->
            val buffer = ByteArray(8192)
            var total = 0
            while (true) {
                val count = stream.read(buffer)
                if (count == -1) break
                total += count
                if (total > MAX_BYTES) {
                    tooLarge = true
                    break
                }
                output.write(buffer, 0, count)
            }
        } ?: throw IllegalArgumentException("Foto tidak dapat dibaca dari perangkat.")
        val bytes = if (tooLarge) compressLargeImage(uri) else output.toByteArray()
        require(bytes.isNotEmpty()) { "File foto kosong." }
        val actualMime = if (tooLarge) "image/jpeg" else mime
        val actualExt = if (tooLarge) "jpg" else extension
        val path = "$userId/found/${UUID.randomUUID()}.$actualExt"
        client.storage.from(BUCKET).upload(path, bytes) {
            contentType = ContentType.parse(actualMime)
            upsert = false
        }
        return path
    }
}
