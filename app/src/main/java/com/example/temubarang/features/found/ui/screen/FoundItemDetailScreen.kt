package com.example.temubarang.features.found.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.temubarang.features.found.model.foundStatusLabel
import com.example.temubarang.features.found.ui.component.FoundMessage
import com.example.temubarang.features.found.ui.component.FoundPhoto
import com.example.temubarang.features.found.viewmodel.FoundItemsViewModel
import com.example.temubarang.ui.component.GradientPrimaryButton
import com.example.temubarang.ui.theme.BrandBackground
import com.example.temubarang.ui.theme.BrandNavy

@Composable
fun FoundItemDetailScreen(
    viewModel: FoundItemsViewModel,
    itemId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(itemId) { viewModel.load(itemId) }
    val item = state.detail?.takeIf { it.id == itemId }

    Column(
        modifier = Modifier.fillMaxSize().background(BrandBackground)
            .systemBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TextButton(onClick = onBack) { Text("← Daftar barang") }
        Text("Detail Barang Ditemukan", style = MaterialTheme.typography.headlineSmall, color = BrandNavy)
        FoundMessage(state.error)
        FoundMessage(state.notice, isError = false)
        if (state.isLoadingDetail) {
            CircularProgressIndicator()
        } else if (item == null) {
            Text("Detail tidak tersedia atau laporan sudah dihapus.")
        } else {
            FoundPhoto(viewModel.photoUrl(item.imagePath), Modifier.fillMaxWidth().height(230.dp))
            Text(item.title, style = MaterialTheme.typography.headlineSmall, color = BrandNavy)
            DetailRow("Deskripsi", item.description)
            DetailRow("Lokasi penemuan", item.foundLocation)
            DetailRow("Tanggal ditemukan", item.foundAt)
            DetailRow("Status", item.status.foundStatusLabel())
            if (viewModel.userId == item.reporterId) {
                GradientPrimaryButton("Edit Laporan / Ubah Status", onClick = { onEdit(item.id) })
            } else {
                Text("Hanya pelapor yang dapat mengubah laporan ini.",
                    style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
