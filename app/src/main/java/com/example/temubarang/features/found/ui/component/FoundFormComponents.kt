package com.example.temubarang.features.found.ui.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.temubarang.ui.component.AppFilterChip
import com.example.temubarang.ui.component.AppFormField
import com.example.temubarang.ui.component.AppSectionCard
import com.example.temubarang.ui.theme.AppSpacing

/** Memakai input dasar yang sama dengan form Barang Hilang dan Profil. */
@Composable
fun FoundFormField(
    label: String,
    example: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    minLines: Int = 1,
    hint: String? = null
) {
    AppFormField(value = value, onValueChange = onValueChange, label = label,
        modifier = modifier, placeholder = example, supportingText = hint,
        enabled = enabled, singleLine = minLines == 1, minLines = minLines)
}

@Composable
fun FoundChoiceRow(
    label: String,
    options: Map<String, String>,
    selected: String,
    onSelect: (String) -> Unit,
    enabled: Boolean = true
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text(label, style = MaterialTheme.typography.titleSmall)
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            options.forEach { (code, text) ->
                AppFilterChip(selected = selected == code,
                    onClick = { onSelect(code) }, label = text, enabled = enabled)
            }
        }
    }
}

@Composable
fun FoundSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    AppSectionCard(title = title, content = content)
}
