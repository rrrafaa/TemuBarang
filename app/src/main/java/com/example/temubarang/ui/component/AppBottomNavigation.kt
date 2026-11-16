package com.example.temubarang.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.temubarang.ui.theme.BrandBackground
import com.example.temubarang.ui.theme.BrandIndigo

data class AppNavigationItem(val route: String, val label: String, @param:DrawableRes val icon: Int)

/** Raised active badge and curved cradle; all five real destinations remain tabs. */
@Composable
fun AppBottomNavigation(
    items: List<AppNavigationItem>, selectedRoute: String?, onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return
    val active = items.indexOfFirst { it.route == selectedRoute }
    val surface = MaterialTheme.colorScheme.surface
    Column(modifier.background(BrandBackground).navigationBarsPadding().padding(horizontal = 10.dp)) {
        Box(Modifier.fillMaxWidth().heightIn(min = 108.dp)) {
            Canvas(Modifier.matchParentSize()) {
                val top = 26.dp.toPx()
                val corner = 20.dp.toPx()
                val cx = (active + .5f) * size.width / items.size
                val half = minOf(36.dp.toPx(), size.width / items.size / 2)
                val path = Path().apply {
                    if (active == 0) moveTo(0f, top) else {
                        moveTo(0f, top + corner)
                        quadraticTo(0f, top, corner, top)
                    }
                    if (active >= 0) {
                        lineTo(cx - half, top)
                        cubicTo(cx - half * .7f, top, cx - half * .8f, 64.dp.toPx(), cx, 64.dp.toPx())
                        cubicTo(cx + half * .8f, 64.dp.toPx(), cx + half * .7f, top, cx + half, top)
                    }
                    if (active == items.lastIndex) lineTo(size.width, top) else {
                        lineTo(size.width - corner, top)
                        quadraticTo(size.width, top, size.width, top + corner)
                    }
                    lineTo(size.width, size.height - corner)
                    quadraticTo(size.width, size.height, size.width - corner, size.height)
                    lineTo(corner, size.height)
                    quadraticTo(0f, size.height, 0f, size.height - corner)
                    close()
                }
                drawPath(path, surface)
            }
            Row(Modifier.fillMaxWidth().selectableGroup()) {
                items.forEachIndexed { index, item ->
                    val selected = index == active
                    Column(
                        Modifier.weight(1f).selectable(selected, role = Role.Tab, onClick = { onSelect(item.route) })
                            .padding(top = if (selected) 0.dp else 34.dp, bottom = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (selected) Box(
                            Modifier.size(60.dp).background(BrandBackground, CircleShape).padding(5.dp)
                                .background(surface, CircleShape).border(1.dp, BrandIndigo.copy(alpha = .15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            CompositionLocalProvider(LocalContentColor provides BrandIndigo) { AppIcon(item.icon, null) }
                        } else Box(Modifier.height(26.dp), contentAlignment = Alignment.Center) {
                            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) { AppIcon(item.icon, null) }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(item.label, style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) BrandIndigo else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}
