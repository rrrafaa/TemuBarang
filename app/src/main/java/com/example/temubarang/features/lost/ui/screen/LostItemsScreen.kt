package com.example.temubarang.features.lost.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.temubarang.features.lost.ui.component.LostItemCard
import com.example.temubarang.features.lost.ui.component.LostMessage
import com.example.temubarang.features.lost.viewmodel.LostItemsViewModel
import com.example.temubarang.ui.component.AppPage
import com.example.temubarang.ui.component.GradientPrimaryButton
import com.example.temubarang.ui.theme.BrandNavy

@Composable
fun LostItemsScreen(
    viewModel: LostItemsViewModel,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    onDetail: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    AppPage(
        title = "Barang Hilang",
        onBack = onBack,
        onRefresh = { viewModel.refresh() },
        refreshing = state.isLoading
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Laporan barang hilang yang kamu buat.",
                style = MaterialTheme.typography.bodyMedium,
                color = BrandNavy
            )
            GradientPrimaryButton(
                text = "+ Tambah Laporan",
                onClick = {
                    viewModel.clearMessages()
                    onCreate()
                }
            )
            LostMessage(state.error)
            LostMessage(state.notice, isError = false)

            if (state.isLoading && state.items.isEmpty()) {
                CircularProgressIndicator()
            } else if (state.items.isEmpty()) {
                Text("Kamu belum membuat laporan barang hilang. Ketuk tombol Tambah Laporan untuk memulai.")
            } else {
                Text(
                    "${state.items.size} laporan",
                    style = MaterialTheme.typography.labelMedium
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.items, key = { it.id }) { item ->
                        LostItemCard(item, viewModel.photoUrl(item.imagePath)) {
                            viewModel.clearMessages()
                            onDetail(item.id)
                        }
                    }
                }
            }
        }
    }
}
