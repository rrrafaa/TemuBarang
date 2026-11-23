package com.example.temubarang.features.lost.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.temubarang.R
import com.example.temubarang.features.lost.ui.component.LostItemCard
import com.example.temubarang.features.lost.ui.component.LostMessage
import com.example.temubarang.features.lost.viewmodel.LostItemsViewModel
import com.example.temubarang.ui.component.AppIcon
import com.example.temubarang.ui.component.AppPage
import com.example.temubarang.ui.component.AppSearchBar
import com.example.temubarang.ui.component.GradientPrimaryButton

/** Tombol tambah laporan berada di bawah daftar, tepat di atas bottom navigation. */
@Composable
fun LostItemsScreen(
    viewModel: LostItemsViewModel,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    onDetail: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    val visibleItems = state.items.filter { item ->
        query.isBlank() || listOf(item.title, item.description, item.lastSeenLocation)
            .any { it.contains(query.trim(), ignoreCase = true) }
    }
    LaunchedEffect(Unit) { viewModel.refresh() }

    AppPage("Barang Hilang", onBack = onBack,
        onRefresh = viewModel::refresh, refreshing = state.isLoading) {
        Column(Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    AppSearchBar(value = query, onValueChange = { query = it },
                        placeholder = "Cari nama atau lokasi barang hilang")
                }
                item {
                    LostMessage(state.error)
                    LostMessage(state.notice, isError = false)
                }
                if (state.isLoading && state.items.isEmpty()) {
                    item { CircularProgressIndicator() }
                } else if (visibleItems.isEmpty()) {
                    item {
                        Text(if (query.isNotBlank()) "Tidak ada barang hilang yang cocok dengan pencarian."
                            else "Belum ada laporan barang hilang. Ketuk Tambah Laporan untuk memulai.")
                    }
                } else {
                    item { Text("${visibleItems.size} laporan", style = MaterialTheme.typography.labelMedium) }
                    items(visibleItems, key = { it.id }) { item ->
                        LostItemCard(item, viewModel.photoUrl(item.imagePath)) {
                            viewModel.clearMessages()
                            onDetail(item.id)
                        }
                    }
                }
            }
            Surface(color = Color.Transparent) {
                GradientPrimaryButton(
                    text = "Tambah Laporan",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    leadingIcon = { AppIcon(R.drawable.ic_circle_plus_duotone, null) },
                    onClick = { viewModel.clearMessages(); onCreate() }
                )
            }
        }
    }
}
