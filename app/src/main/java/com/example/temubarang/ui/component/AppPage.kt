package com.example.temubarang.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.temubarang.ui.theme.BrandBackground

/** Header outside the scrolling content. Parent navigation owns the bottom inset. */
@Composable
fun AppPage(
    title: String,
    onBack: (() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null,
    refreshing: Boolean = false,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    Column(Modifier.fillMaxSize().background(BrandBackground)) {
        Surface(shadowElevation = 1.dp) {
            AppHeader(title, Modifier.statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
                onBack, onRefresh, refreshing, enabled)
        }
        Box(Modifier.weight(1f).fillMaxWidth()) { content() }
    }
}
