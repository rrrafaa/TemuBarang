package com.example.temubarang.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.temubarang.R
import com.example.temubarang.ui.theme.AppRadius
import com.example.temubarang.ui.theme.AppSpacing
import com.example.temubarang.ui.theme.BrandSoftBlue

/** UI pemilihan foto bersama; pemanggil menangani kamera/galeri dan URI. */
@Composable
fun AppPhotoPicker(
    hasPhoto: Boolean,
    onGallery: () -> Unit,
    onCamera: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    required: Boolean = false,
    showError: Boolean = false,
    message: String? = null,
    onRemove: (() -> Unit)? = null,
    preview: (@Composable () -> Unit)? = null
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        if (hasPhoto && preview != null) {
            preview()
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth().height(124.dp),
                color = if (showError) MaterialTheme.colorScheme.errorContainer else BrandSoftBlue,
                shape = RoundedCornerShape(AppRadius.field),
                border = BorderStroke(1.dp, if (showError) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.outline)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                    AppIcon(R.drawable.ic_image_duotone, null)
                    Text(if (required) "Unggah foto barang *" else "Tambahkan foto barang",
                        style = MaterialTheme.typography.titleSmall)
                    Text("JPG, PNG atau WEBP", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        // Tumpuk tombol agar tetap dapat dipakai pada 320dp dan font besar.
        OutlinedButton(onClick = onGallery, enabled = enabled,
            modifier = Modifier.fillMaxWidth()) {
            AppIcon(R.drawable.ic_image_duotone, null)
            Text(if (hasPhoto) " Ganti foto dari galeri" else " Pilih dari galeri")
        }
        OutlinedButton(onClick = onCamera, enabled = enabled,
            modifier = Modifier.fillMaxWidth()) {
            AppIcon(R.drawable.ic_camera_duotone, null)
            Text(" Ambil foto dengan kamera")
        }
        if (hasPhoto && onRemove != null) {
            TextButton(onClick = onRemove, enabled = enabled) { Text("Batalkan pilihan foto") }
        }
        if (showError) {
            Text("Silakan tambahkan foto barang.", color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall)
        }
        if (!message.isNullOrBlank()) {
            Text(message, color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall)
        }
    }
}
