package com.example.temubarang.features.found.ui.camera

import android.content.ClipData
import android.content.Intent
import android.content.Context
import androidx.activity.result.contract.ActivityResultContracts
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

class CameraPhotoContract : ActivityResultContracts.TakePicture() {
    override fun createIntent(context: Context, input: Uri): Intent =
        super.createIntent(context, input).apply {
            clipData = ClipData.newRawUri("Foto TemuBarang", input)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
}
