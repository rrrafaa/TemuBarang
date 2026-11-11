package com.example.temubarang.features.home.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.temubarang.features.home.model.HomeLostItem
import com.example.temubarang.features.home.viewmodel.HomeViewModel
import com.example.temubarang.ui.component.GradientPrimaryButton
import com.example.temubarang.ui.theme.BrandBackground
import com.example.temubarang.ui.theme.BrandMuted
import com.example.temubarang.ui.theme.BrandNavy

/** Home milik bersama; menampilkan data lost_items tanpa mengambil alih CRUD anggota 1. */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenLost: () -> Unit,
    onOpenFound: () -> Unit,
    onLogout: () -> Unit,
    isLoggingOut: Boolean,
    errorMessage: String? = null
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.refresh() }

    Column(
        modifier = Modifier.fillMaxSize().background(BrandBackground)
            .statusBarsPadding().padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("TemuBarang", style = MaterialTheme.typography.headlineMedium, color = BrandNavy)
                Text("Bantu temukan barang yang hilang", color = BrandMuted)
            }
            TextButton(onClick = viewModel::refresh, enabled = !state.isLoading) { Text("Muat ulang") }
        }
        GradientPrimaryButton(text = "Saya Menemukan Barang", onClick = onOpenFound)
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Laporan Barang Hilang", style = MaterialTheme.typography.titleLarge, color = BrandNavy,
                modifier = Modifier.weight(1f))
            TextButton(onClick = onOpenLost) { Text("Kelola ›") }
        }
        if (errorMessage != null) {
            FeedbackMessage(errorMessage)
        }
        if (state.error != null) FeedbackMessage(state.error!!)

        if (state.isLoading && state.lostItems.isEmpty()) {
            CircularProgressIndicator()
            Spacer(Modifier.weight(1f))
        } else if (state.lostItems.isEmpty()) {
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface) {
                Text(
                    "Belum ada laporan barang hilang yang dapat ditampilkan. Data akan muncul ketika anggota 1 membuat laporan.",
                    modifier = Modifier.padding(20.dp), color = BrandMuted
                )
            }
            Spacer(Modifier.weight(1f))
        } else {
            Text("${state.lostItems.size} laporan tersedia", color = BrandMuted,
                style = MaterialTheme.typography.labelMedium)
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.lostItems, key = { it.id }) { item ->
                    LostPreviewCard(item, viewModel.photoUrl(item.imagePath))
                }
            }
        }
        OutlinedButton(onClick = onLogout, enabled = !isLoggingOut,
            modifier = Modifier.fillMaxWidth()) {
            Text(if (isLoggingOut) "Sedang keluar..." else "Keluar akun")
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
private fun LostPreviewCard(item: HomeLostItem, photoUrl: String?) {
    Surface(
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
                        Text("⌕", style = MaterialTheme.typography.headlineMedium, color = BrandNavy)
                    }
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium, color = BrandNavy)
                Text("Terakhir terlihat: ${item.lastSeenLocation}", color = BrandMuted,
                    style = MaterialTheme.typography.bodySmall)
                Text("${item.lostAt} · ${item.status}",
                    style = MaterialTheme.typography.labelSmall, color = BrandMuted)
            }
        }
    }
}
