package com.example.temubarang.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
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
    Row(modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) IconButton(onClick = onBack, enabled = enabled) {
            AppIcon(R.drawable.ic_circle_arrow_left_duotone, "Kembali")
        } else Spacer(Modifier.width(48.dp))
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center)
        if (onRefresh != null) IconButton(onClick = onRefresh, enabled = enabled && !refreshing) {
            if (refreshing) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
            else AppIcon(R.drawable.ic_refresh_cw, "Muat ulang")
        } else Spacer(Modifier.width(48.dp))
    }
}

@Composable
fun AppIcon(@DrawableRes resource: Int, description: String?, modifier: Modifier = Modifier) {
    Icon(painterResource(resource), description, modifier.size(24.dp))
}
