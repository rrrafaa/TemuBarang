package com.example.temubarang.features.found.ui.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            // The accessibility label stays available without a floating visual label.
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = label },
            placeholder = { Text(example) }, enabled = enabled,
            singleLine = minLines == 1, minLines = minLines,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
            supportingText = hint?.let { text -> { Text(text) } }
        )
    }
}

/** A single horizontal row; long options remain reachable on narrow displays. */
@Composable
fun FoundChoiceRow(
    label: String,
    options: Map<String, String>,
    selected: String,
    onSelect: (String) -> Unit,
    enabled: Boolean = true
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.titleSmall)
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { (code, text) ->
                FilterChip(selected = selected == code, onClick = { onSelect(code) },
                    label = { Text(text) }, enabled = enabled)
            }
        }
    }
}

@Composable
fun FoundSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            content()
        }
    }
}
