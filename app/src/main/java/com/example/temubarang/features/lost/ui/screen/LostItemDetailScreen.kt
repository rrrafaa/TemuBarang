package com.example.temubarang.features.lost.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.temubarang.features.lost.model.lostStatusLabel
import com.example.temubarang.features.lost.ui.component.LostMessage
import com.example.temubarang.features.lost.ui.component.LostPhoto
import com.example.temubarang.features.lost.viewmodel.LostItemsViewModel
import com.example.temubarang.ui.component.AppDetailRow
import com.example.temubarang.ui.component.AppPage
import com.example.temubarang.ui.component.AppSectionCard
import com.example.temubarang.ui.component.AppStatusBadge
import com.example.temubarang.ui.component.GradientPrimaryButton
import com.example.temubarang.ui.theme.AppSpacing
import com.example.temubarang.ui.theme.BrandNavy

/** Detail laporan hilang mengikuti struktur detail barang ditemukan. */
@Composable
fun LostItemDetailScreen(
    viewModel: LostItemsViewModel,
    itemId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onStatusUpdated: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val item = state.items.firstOrNull { it.id == itemId }
    var confirmDelete by rememberSaveable(itemId) { mutableStateOf(false) }

    LaunchedEffect(itemId) { if (item == null) viewModel.refresh() }

    AppPage("Detail Barang Hilang", onBack = onBack,
        onRefresh = viewModel::refresh, refreshing = state.isLoading, enabled = !state.isSaving) {
        Column(
            modifier = Modifier.fillMaxSize().navigationBarsPadding()
                .verticalScroll(rememberScrollState()).padding(AppSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            LostMessage(state.error)
            LostMessage(state.notice, isError = false)
            if (item == null) {
                Text(if (state.isLoading) "Memuat laporan..." else "Laporan tidak ditemukan.")
            } else {
                AppSectionCard("Informasi Barang") {
                    LostPhoto(viewModel.photoUrl(item.imagePath),
                        Modifier.fillMaxWidth().height(230.dp))
                    Text(item.title, style = MaterialTheme.typography.headlineSmall, color = BrandNavy)
                    AppStatusBadge(item.status.lostStatusLabel(), positive = item.status == "found")
                    AppDetailRow("Deskripsi", item.description)
                    AppDetailRow("Lokasi terakhir terlihat", item.lastSeenLocation)
                    AppDetailRow("Tanggal hilang", item.lostAt)
                }
                AppSectionCard("Pelapor") {
                    Text("Laporan ini dibuat dari akunmu. Informasi kontak publik tidak tersedia pada data laporan barang hilang.",
                        style = MaterialTheme.typography.bodyMedium)
                }
                if (item.status != "found") {
                    GradientPrimaryButton(
                        text = "Edit laporan", enabled = !state.isSaving,
                        onClick = { viewModel.clearMessages(); onEdit(item.id) }
                    )
                    OutlinedButton(
                        onClick = { viewModel.updateStatus(item.id, "found", onStatusUpdated) },
                        enabled = !state.isSaving, modifier = Modifier.fillMaxWidth()
                    ) { Text("Tandai sudah ditemukan") }
                }
                if (item.status != "closed") {
                    OutlinedButton(
                        onClick = { viewModel.updateStatus(item.id, "closed", onStatusUpdated) },
                        enabled = !state.isSaving, modifier = Modifier.fillMaxWidth()
                    ) { Text("Tutup laporan") }
                }
                if (item.status != "open") {
                    OutlinedButton(
                        onClick = { viewModel.updateStatus(item.id, "open", onStatusUpdated) },
                        enabled = !state.isSaving, modifier = Modifier.fillMaxWidth()
                    ) { Text("Buka kembali (masih dicari)") }
                }
                OutlinedButton(
                    onClick = { confirmDelete = true },
                    enabled = !state.isSaving,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Hapus laporan") }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Hapus laporan?") },
            text = { Text("Laporan yang dihapus tidak dapat dikembalikan.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.delete(itemId, onSuccess = onBack)
                }) { Text("Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Batal") }
            }
        )
    }
}
