package com.example.temubarang.features.lost.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import com.example.temubarang.ui.theme.BrandBackground
import com.example.temubarang.ui.theme.BrandMuted
import com.example.temubarang.ui.theme.BrandNavy
import com.example.temubarang.ui.theme.BrandSoftMint
import com.example.temubarang.ui.component.GradientPrimaryButton

@Composable
fun LostItemDetailScreen(
    viewModel: LostItemsViewModel,
    itemId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val item = state.items.firstOrNull { it.id == itemId }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    // Bila state kosong (misalnya proses dipulihkan), muat ulang daftar.
    LaunchedEffect(itemId) { if (item == null) viewModel.refresh() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack, enabled = !state.isSaving) { Text("← Kembali") }

        if (item == null) {
            Text(if (state.isLoading) "Memuat laporan..." else "Laporan tidak ditemukan.")
            LostMessage(state.error)
            return@Column
        }

        LostPhoto(viewModel.photoUrl(item.imagePath), Modifier.fillMaxWidth().height(200.dp))
        Text(item.title, style = MaterialTheme.typography.headlineMedium, color = BrandNavy)
        Surface(shape = RoundedCornerShape(8.dp), color = BrandSoftMint) {
            Text(
                item.status.lostStatusLabel(),
                color = BrandNavy,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(8.dp)
            )
        }

        LostMessage(state.error)
        LostMessage(state.notice, isError = false)

        DetailRow("Deskripsi", item.description)
        DetailRow("Lokasi terakhir terlihat", item.lastSeenLocation)
        DetailRow("Tanggal hilang", item.lostAt)


        if (item.status != "found") {
            GradientPrimaryButton(
                text = "Edit laporan",
                enabled = !state.isSaving,
                onClick = {
                    viewModel.clearMessages()
                    onEdit(item.id)
                }
            )
            OutlinedButton(
                onClick = { viewModel.updateStatus(item.id, "found") },
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Tandai sudah ditemukan") }
        }
        if (item.status != "closed") {
            OutlinedButton(
                onClick = { viewModel.updateStatus(item.id, "closed") },
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Tutup laporan") }
        }
        if (item.status != "open") {
            OutlinedButton(
                onClick = { viewModel.updateStatus(item.id, "open") },
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Buka kembali (masih dicari)") }
        }
        OutlinedButton(
            onClick = { confirmDelete = true },
            enabled = !state.isSaving,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Hapus laporan") }
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

@Composable
private fun DetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = BrandMuted)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}