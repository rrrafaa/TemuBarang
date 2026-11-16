package com.example.temubarang.features.home.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.temubarang.ui.component.AppPage
import com.example.temubarang.ui.component.AppIcon
import com.example.temubarang.R
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.temubarang.features.home.model.HomeFeedItem
import com.example.temubarang.features.home.model.ReportType
import com.example.temubarang.features.home.viewmodel.HomeViewModel
import com.example.temubarang.ui.component.GradientPrimaryButton
import com.example.temubarang.ui.theme.BrandBackground
import com.example.temubarang.ui.theme.BrandMuted
import com.example.temubarang.ui.theme.BrandNavy

/** Feed bersama membaca tabel yang sama dengan masing-masing fitur. */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    userName: String,
    onOpenLost: () -> Unit,
    onOpenFound: () -> Unit,
    onFoundDetail: (String) -> Unit,
    errorMessage: String? = null
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.refresh() }
    val visibleItems = state.visibleItems

    AppPage("Beranda", onRefresh = viewModel::refresh, refreshing = state.isLoading) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Surface(shape = RoundedCornerShape(24.dp), color = com.example.temubarang.ui.theme.BrandSoftBlue) {
                    Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        val hour = java.time.LocalTime.now().hour
                        val greeting = when (hour) { in 5..10 -> "Selamat pagi"; in 11..14 -> "Selamat siang"; in 15..17 -> "Selamat sore"; else -> "Selamat malam" }
                        Text("$greeting,", color = BrandMuted)
                        Text("${userName.trim().ifBlank { "Teman Kampus" }}!", style = MaterialTheme.typography.headlineSmall, color = BrandNavy)
                        Text("Ada barang yang tertinggal? Yuk, bantu temukan pemiliknya dan buat kampus lebih peduli.", color = BrandMuted)
                    }
                }
            }
            item { GradientPrimaryButton("Saya Menemukan Barang", onClick = onOpenFound,
                leadingIcon = { AppIcon(R.drawable.ic_package_check_duotone, null) }) }
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Kabar Barang di Kampus", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, color = BrandNavy)
                    TextButton(onClick = onOpenLost) { Text("Lihat Hilang") }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(null, ReportType.LOST, ReportType.FOUND).forEach { type ->
                        FilterChip(selected = state.filter == type, onClick = { viewModel.setFilter(type) },
                            label = { Text(type?.label ?: "Semua") })
                    }
                }
            }
            if (errorMessage != null) item { FeedbackMessage(errorMessage) }
            state.error?.let { message -> item { FeedbackMessage(message) } }
            if (state.isLoading && state.items.isEmpty()) item { CircularProgressIndicator() }
            else if (visibleItems.isEmpty()) item {
                Surface(shape = RoundedCornerShape(18.dp)) {
                    Text("Belum ada kabar barang di kategori ini. Coba kategori lain atau laporkan barang yang kamu temukan.", Modifier.padding(20.dp), color = BrandMuted)
                }
            } else {
                item { Text("${visibleItems.size} laporan", style = MaterialTheme.typography.labelMedium, color = BrandMuted) }
                items(visibleItems, key = { it.key }) { item ->
                    ReportPreviewCard(item, viewModel.photoUrl(item.imagePath)) {
                        if (item.type == ReportType.FOUND) onFoundDetail(item.id) else onOpenLost()
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedbackMessage(text: String) {
    Surface(color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(14.dp)) {
        Text(text, modifier = Modifier.fillMaxWidth().padding(12.dp),
            color = MaterialTheme.colorScheme.onErrorContainer)
    }
}

@Composable
private fun ReportPreviewCard(item: HomeFeedItem, photoUrl: String?, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            if (photoUrl != null) {
                AsyncImage(
                    model = photoUrl, contentDescription = "Foto ${item.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(78.dp).clip(RoundedCornerShape(12.dp))
                )
            } else {
                Surface(modifier = Modifier.size(78.dp), color = Color(0xFFE7F2FF),
                    shape = RoundedCornerShape(12.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center) {
                        AppIcon(R.drawable.ic_package_duotone, null)
                    }
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.type.label, style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary)
                Text(item.title, style = MaterialTheme.typography.titleMedium, color = BrandNavy)
                Text("${if (item.type == ReportType.FOUND) "Ditemukan di" else "Terakhir terlihat"}: ${item.location}", color = BrandMuted,
                    style = MaterialTheme.typography.bodySmall)
                Text("${item.date} · ${item.statusLabel}",
                    style = MaterialTheme.typography.labelSmall, color = BrandMuted)
            }
        }
    }
}
