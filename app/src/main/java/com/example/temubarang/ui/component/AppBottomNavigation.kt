package com.example.temubarang.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.temubarang.ui.theme.AppSizes
import com.example.temubarang.ui.theme.AppSpacing
import com.example.temubarang.ui.theme.BrandIndigo
import com.example.temubarang.ui.theme.BrandSoftBlue

data class AppNavigationItem(val route: String, val label: String, @param:DrawableRes val icon: Int)

/**
 * Navbar transparan: tanpa bidang warna/Canvas solid. Area sentuh tiap tab
 * tetap lebar dan state dipilih melalui route Navigation Compose.
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
            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs)
            .selectableGroup(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val selected = item.route == selectedRoute
            Column(
                modifier = Modifier.weight(1f)
                    .heightIn(min = AppSizes.minTouch + AppSpacing.xxl)
                    .selectable(selected = selected, role = Role.Tab, onClick = { onSelect(item.route) })
                    .padding(vertical = AppSpacing.sm),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(40.dp)
                        .background(if (selected) BrandSoftBlue else Color.Transparent, CircleShape)
                        .then(if (selected) Modifier.border(1.dp, BrandIndigo.copy(alpha = .25f), CircleShape) else Modifier),
                    contentAlignment = Alignment.Center
                ) {
                    CompositionLocalProvider(
                        LocalContentColor provides if (selected) BrandIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                    ) { AppIcon(item.icon, item.label) }
                }
                Spacer(Modifier.height(AppSpacing.xs))
                Text(
                    item.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) BrandIndigo else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }
    }
}
