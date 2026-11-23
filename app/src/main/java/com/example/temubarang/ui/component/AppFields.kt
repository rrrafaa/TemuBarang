package com.example.temubarang.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import com.example.temubarang.R
import com.example.temubarang.ui.theme.AppRadius
import com.example.temubarang.ui.theme.AppSizes
import com.example.temubarang.ui.theme.AppSpacing
import com.example.temubarang.ui.theme.AppStatusColors

/** Search yang sama di Beranda, Hilang dan Ditemukan. Query tetap dimiliki screen. */
@Composable
fun AppSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Cari barang atau lokasi",
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth().heightIn(min = AppSizes.search),
        placeholder = { Text(placeholder) },
        leadingIcon = { AppIcon(R.drawable.ic_search_duotone, null) },
        enabled = enabled, singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        shape = RoundedCornerShape(AppRadius.search),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

/** Input dasar semua form, termasuk validasi, area teks dan jenis keyboard. */
@Composable
fun AppFormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    minLines: Int = 1,
    isError: Boolean = false,
    imeAction: ImeAction = ImeAction.Next,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = placeholder?.let { example -> { Text(example) } },
        supportingText = supportingText?.let { message -> { Text(message) } },
        enabled = enabled, isError = isError,
        singleLine = singleLine, minLines = minLines,
        keyboardOptions = KeyboardOptions(
            capitalization = capitalization, keyboardType = keyboardType, imeAction = imeAction
        ),
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        shape = RoundedCornerShape(AppRadius.field),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

/** Card seragam untuk form dan detail laporan. */
@Composable
fun AppSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadius.card),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(Modifier.padding(AppSpacing.lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            if (!subtitle.isNullOrBlank()) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            content()
        }
    }
}

@Composable
fun AppFilterChip(selected: Boolean, onClick: () -> Unit, label: String, enabled: Boolean = true) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) }, enabled = enabled,
        shape = RoundedCornerShape(AppRadius.field))
}

@Composable
fun AppStatusBadge(label: String, positive: Boolean = true) {
    Surface(
        shape = RoundedCornerShape(AppRadius.badge),
        color = if (positive) AppStatusColors.successBackground else AppStatusColors.warningBackground
    ) {
        Text(label, modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
            style = MaterialTheme.typography.labelSmall,
            color = if (positive) AppStatusColors.successText else AppStatusColors.warningText)
    }
}

/** Baris label dan nilai yang sama di kedua halaman detail. */
@Composable
fun AppDetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Text(label, style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface)
    }
}

/** Pilihan dropdown untuk kode dan label yang dikontrol oleh ViewModel/screen. */
@Composable
fun AppDropdownField(
    label: String,
    options: Map<String, String>,
    selected: String,
    onSelect: (String) -> Unit,
    enabled: Boolean = true
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text(label, style = MaterialTheme.typography.titleSmall)
        Box(Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true }, enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AppRadius.field)
            ) {
                Text(options[selected] ?: "Pilih salah satu")
                Spacer(Modifier.weight(1f))
                Text("⌄")
            }
            DropdownMenu(expanded = expanded && enabled, onDismissRequest = { expanded = false }) {
                options.forEach { (code, value) ->
                    DropdownMenuItem(text = { Text(value) }, onClick = {
                        expanded = false
                        onSelect(code)
                    })
                }
            }
        }
    }
}
