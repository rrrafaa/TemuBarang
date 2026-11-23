package com.example.temubarang.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.temubarang.ui.theme.AppRadius
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Date picker reusable dengan keluaran YYYY-MM-DD dan batas hari ini. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerField(
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
    Box(modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = display, onValueChange = {}, readOnly = true, enabled = enabled,
            label = { Text(label) },
            supportingText = { Text("Ketuk untuk memilih tanggal") },
            trailingIcon = { Text("📅") },
            shape = RoundedCornerShape(AppRadius.field),
            modifier = Modifier.fillMaxWidth()
        )
        Box(Modifier.matchParentSize().clickable(enabled = enabled) { open = true })
    }
    if (open) {
        val today = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val initial = runCatching {
            LocalDate.parse(value).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }.getOrNull()
        val picker = rememberDatePickerState(
            initialSelectedDateMillis = initial,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= today
            }
        )
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    picker.selectedDateMillis?.let { millis ->
                        onValueChange(
                            Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()
                        )
                    }
                    open = false
                }) { Text("Pilih") }
            },
            dismissButton = {
                TextButton(onClick = { open = false }) { Text("Batal") }
            }
        ) { DatePicker(state = picker) }
    }
}
