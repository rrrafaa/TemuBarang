package com.example.temubarang.features.found.ui.camera

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/** Kamera perangkat menulis JPG ke content:// URI dari FileProvider. */
object CameraPhotoUri {
    fun create(context: Context): Uri {
        val directory = File(context.cacheDir, "camera_photos").apply {
            check(exists() || mkdirs()) { "Tidak bisa membuat folder sementara kamera." }
        }
        val destination = File.createTempFile("temubarang_", ".jpg", directory)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            destination
        )
    }

    /** Hasil kamera valid hanya ketika URI berisi data, bukan file sementara 0 byte. */
    fun hasPhoto(context: Context, uri: Uri): Boolean = try {
        context.contentResolver.openInputStream(uri)?.use { it.read() != -1 } ?: false
    } catch (_: Exception) {
        false
    }
}
