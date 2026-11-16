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
    cornerRadius: Dp = 16.dp,
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
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = BrandNavy
                )
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = BrandMuted)
                }
            }
            content()
        }
    }
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
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        supportingText = { Text(hint) },
        singleLine = singleLine,
        minLines = minLines,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = imeAction
        ),
        enabled = enabled,
        modifier = modifier.fillMaxWidth()
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
    val errorColor = MaterialTheme.colorScheme.error
    val borderColor = if (showError) errorColor else BrandBlue.copy(alpha = 0.55f)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (previewUrl == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (showError) MaterialTheme.colorScheme.errorContainer else BrandSoftBlue)
                    .dashedBorder(borderColor)
                    .clickable(enabled = enabled, onClick = onPickFile),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("📤", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Unggah foto barang",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandNavy
                    )
                    Text(
                        "Ketuk di sini untuk memilih file\nFormat JPG, PNG, atau WEBP",
                        style = MaterialTheme.typography.bodySmall,
                        color = BrandMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LostPhoto(previewUrl, Modifier.fillMaxWidth().height(210.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onPickFile, enabled = enabled) { Text("Ganti foto") }
                if (onRemove != null) {
                    TextButton(
                        onClick = onRemove,
                        enabled = enabled,
                        colors = ButtonDefaults.textButtonColors(contentColor = errorColor)
                    ) { Text("Batalkan pilihan") }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onTakePhoto, enabled = enabled) {
                Text("📷  Atau ambil foto langsung")
            }
        }

        if (showError) {
            Text("Foto barang wajib diunggah.", color = errorColor, style = MaterialTheme.typography.bodySmall)
        }
        LostMessage(message)
    }
}

/**
 * Kolom tanggal yang membuka kalender, sehingga pengguna tidak perlu mengetik format manual.
 * Nilai tetap disimpan sebagai YYYY-MM-DD. Tanggal di masa depan tidak dapat dipilih.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    var open by remember { mutableStateOf(false) }
    val display = remember(value) {
        runCatching {
            LocalDate.parse(value).format(
                DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("id-ID"))
            )
        }.getOrDefault(value)
    }

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = display,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            supportingText = { Text("Ketuk untuk memilih tanggal") },
            trailingIcon = { Text("📅") },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )
        // Lapisan transparan agar ketukan di mana pun pada kolom membuka kalender.
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(enabled = enabled) { open = true }
        )
    }

    if (open) {
        val todayUtcMillis = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val initialMillis = runCatching {
            LocalDate.parse(value).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }.getOrNull()
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    utcTimeMillis <= todayUtcMillis
            }
        )
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        onValueChange(
                            Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()
                        )
                    }
                    open = false
                }) { Text("Pilih") }
            },
            dismissButton = { TextButton(onClick = { open = false }) { Text("Batal") } }
        ) {
            DatePicker(state = pickerState)
        }
    }
}