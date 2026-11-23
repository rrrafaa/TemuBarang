package com.example.temubarang.features.lost.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.temubarang.ui.component.AppFormField
import com.example.temubarang.ui.component.AppSectionCard
import com.example.temubarang.ui.component.AppPhotoPicker
import com.example.temubarang.ui.component.AppDatePickerField
import com.example.temubarang.ui.theme.AppRadius
import com.example.temubarang.ui.theme.BrandBlue
import com.example.temubarang.ui.theme.BrandMuted
import com.example.temubarang.ui.theme.BrandNavy
import com.example.temubarang.ui.theme.BrandSoftBlue
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Garis tepi putus-putus untuk area unggah file. */
fun Modifier.dashedBorder(
    color: Color,
    cornerRadius: Dp = AppRadius.field,
    strokeWidth: Dp = 1.5.dp
): Modifier = this.drawBehind {
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(cornerRadius.toPx()),
        style = Stroke(
            width = strokeWidth.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f), 0f)
        )
    )
}

/** Kartu pengelompokan isi form: judul bagian, keterangan singkat, lalu kontennya. */
@Composable
fun FormSection(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    AppSectionCard(title = title, subtitle = subtitle, content = content)
}

@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    hint: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    minLines: Int = 1,
    imeAction: ImeAction = ImeAction.Next
) {
    AppFormField(
        value = value, onValueChange = onValueChange, label = label,
        modifier = modifier, supportingText = hint, enabled = enabled,
        singleLine = singleLine, minLines = minLines, imeAction = imeAction
    )
}

/**
 * Area unggah foto. Pengguna mengetuk area ini untuk memilih file,
 * dan tersedia pilihan kedua untuk langsung memotret.
 * previewUrl boleh berupa content:// (foto baru) atau URL Storage (foto lama saat edit).
 */
@Composable
fun PhotoUploadField(
    previewUrl: String?,
    showError: Boolean,
    enabled: Boolean,
    message: String?,
    onPickFile: () -> Unit,
    onTakePhoto: () -> Unit,
    onRemove: (() -> Unit)?
) {
    AppPhotoPicker(
        hasPhoto = previewUrl != null,
        preview = {
            if (previewUrl != null) LostPhoto(previewUrl, Modifier.fillMaxWidth().height(210.dp))
        },
        required = true,
        showError = showError,
        message = message,
        enabled = enabled,
        onGallery = onPickFile,
        onCamera = onTakePhoto,
        onRemove = onRemove
    )
}

/**
 * Kolom tanggal yang membuka kalender, sehingga pengguna tidak perlu mengetik format manual.
 * Nilai tetap disimpan sebagai YYYY-MM-DD. Tanggal di masa depan tidak dapat dipilih.
 */
@Composable
fun DatePickerField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    AppDatePickerField(label, value, onValueChange, enabled, modifier)
}
