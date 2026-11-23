package com.example.temubarang.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.temubarang.ui.theme.BrandIndigo

data class AppNavigationItem(val route: String, val label: String, @param:DrawableRes val icon: Int)

/**
 * Navbar tanpa panel/bidang warna: area kosong mengikuti background Scaffold.
 * Menu aktif tetap dikenali dari ikon berwarna dan label tebal, bukan bidang yang menutupi layar.
 */
@Composable
fun AppBottomNavigation(
    items: List<AppNavigationItem>,
    selectedRoute: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return
    Row(
        modifier = modifier.fillMaxWidth().navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 8.dp).selectableGroup(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val selected = item.route == selectedRoute
            Column(
                modifier = Modifier.weight(1f).heightIn(min = 64.dp)
                    .selectable(selected = selected, role = Role.Tab) { onSelect(item.route) }
                    .padding(horizontal = 2.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides
                        if (selected) BrandIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                ) { AppIcon(item.icon, item.label) }
                Spacer(Modifier.height(8.dp))
                Text(
                    item.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) BrandIndigo else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }
    }
}
