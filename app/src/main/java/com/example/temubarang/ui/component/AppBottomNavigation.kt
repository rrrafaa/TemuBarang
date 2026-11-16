package com.example.temubarang.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

data class AppNavigationItem(val route: String, val label: String, @param:DrawableRes val icon: Int)

/** Komponen presentasi: aturan back stack tetap di NavHost. */
@Composable
fun AppBottomNavigation(
    items: List<AppNavigationItem>,
    selectedRoute: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier) {
        items.forEach { item ->
            NavigationBarItem(
                selected = selectedRoute == item.route,
                onClick = { onSelect(item.route) },
                icon = { AppIcon(item.icon, null) },
                label = { Text(item.label) },
                alwaysShowLabel = true
            )
        }
    }
}
