package com.example.temubarang.features.found.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.temubarang.features.found.ui.component.FoundItemCard
import com.example.temubarang.features.found.ui.component.FoundMessage
import com.example.temubarang.features.found.viewmodel.FoundItemsViewModel
import com.example.temubarang.ui.component.GradientPrimaryButton
import com.example.temubarang.ui.theme.BrandBackground
import com.example.temubarang.ui.theme.BrandNavy

@Composable
fun FoundItemsScreen(
    viewModel: FoundItemsViewModel,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    onDetail: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.refresh() }

    Column(
        modifier = Modifier.fillMaxSize().background(BrandBackground).statusBarsPadding().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onBack) { Text("← Beranda") }
            TextButton(onClick = viewModel::refresh, enabled = !state.isLoading) { Text("Muat ulang") }
        }
        Text("Barang Ditemukan", style = MaterialTheme.typography.headlineMedium, color = BrandNavy)
        Text("Temukan laporan barang yang diserahkan oleh warga.")
        GradientPrimaryButton(text = "+ Tambah Laporan", onClick = {
            viewModel.clearMessage()
            onCreate()
        })
        FoundMessage(state.error)
        FoundMessage(state.notice, isError = false)
        if (state.isLoading && state.items.isEmpty()) {
            CircularProgressIndicator()
        } else if (state.items.isEmpty()) {
            Text("Belum ada laporan barang ditemukan. Kamu dapat membuat laporan pertama.")
        } else {
            Text("${state.items.size} laporan", style = MaterialTheme.typography.labelMedium)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.items, key = { it.id }) { item ->
                    FoundItemCard(item, viewModel.photoUrl(item.imagePath)) {
                        viewModel.clearMessage()
                        onDetail(item.id)
                    }
                }
            }
        }
    }
}
