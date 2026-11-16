package com.example.temubarang.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.temubarang.R

/** Header ringan untuk halaman utama maupun subhalaman; inset ditangani screen. */
@Composable
fun AppHeader(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null,
    refreshing: Boolean = false,
    enabled: Boolean = true
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) IconButton(onClick = onBack, enabled = enabled) {
            AppIcon(R.drawable.ic_circle_arrow_left_duotone, "Kembali")
        }
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
        if (onRefresh != null) IconButton(onClick = onRefresh, enabled = enabled && !refreshing) {
            if (refreshing) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
            else AppIcon(R.drawable.ic_refresh_cw, "Muat ulang")
        }
    }
}

@Composable
fun AppIcon(@DrawableRes resource: Int, description: String?, modifier: Modifier = Modifier) {
    Icon(painterResource(resource), description, modifier.size(24.dp))
}
