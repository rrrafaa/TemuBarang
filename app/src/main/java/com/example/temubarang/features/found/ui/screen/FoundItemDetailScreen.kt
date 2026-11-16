package com.example.temubarang.features.found.ui.screen

import com.example.temubarang.R
import com.example.temubarang.ui.component.AppHeader
import com.example.temubarang.ui.component.AppIcon
import com.example.temubarang.features.found.ui.component.FoundSectionCard
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalUriHandler
import android.net.Uri
import com.example.temubarang.features.found.model.foundCategories
import com.example.temubarang.features.found.model.custodyTypes
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
    onDeleted: () -> Unit,
    onEdit: (String) -> Unit
) {
    val uriHandler = LocalUriHandler.current
    var contactError by rememberSaveable(itemId) { mutableStateOf<String?>(null) }
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(itemId) { viewModel.load(itemId) }
    var confirmDelete by rememberSaveable(itemId) { mutableStateOf(false) }
    val item = state.detail?.takeIf { it.id == itemId }

    if (confirmDelete && item != null && viewModel.userId == item.reporterId) {
        AlertDialog(
            onDismissRequest = { if (!state.isSaving) confirmDelete = false },
            title = { Text("Hapus laporan?") },
            text = { Text("Laporan \"${item.title}\" dan fotonya akan dihapus. Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                TextButton(enabled = !state.isSaving, onClick = {
                    confirmDelete = false
                    viewModel.delete(itemId, onDeleted)
                }) { Text("Hapus", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(enabled = !state.isSaving, onClick = { confirmDelete = false }) { Text("Batal") }
            }
        )
    }
    Column(
        modifier = Modifier.fillMaxSize().background(BrandBackground)
            .systemBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        AppHeader("Detail Barang Ditemukan", onBack = onBack, onRefresh = { viewModel.load(itemId) }, refreshing = state.isLoadingDetail || state.reporterLoading, enabled = !state.isSaving)
        FoundMessage(state.error)
        FoundMessage(state.notice, isError = false)
        if (state.isLoadingDetail) {
            CircularProgressIndicator()
        } else if (item == null) {
            Text("Detail tidak tersedia atau laporan sudah dihapus.")
        } else {
            FoundSectionCard("Informasi Barang") {
                FoundPhoto(viewModel.photoUrl(item.imagePath), Modifier.fillMaxWidth().height(230.dp))
                Text(item.title, style = MaterialTheme.typography.headlineSmall, color = BrandNavy)
                DetailRow("Deskripsi", item.description)
                DetailRow("Lokasi penemuan", item.foundLocation)
                DetailRow("Tanggal ditemukan", item.foundAt)
                DetailRow("Kampus", item.campus.ifBlank { "Belum diisi" })
                DetailRow("Kategori", foundCategories[item.category] ?: item.category)
                DetailRow("Penyimpan barang", custodyTypes[item.custodyType] ?: item.custodyType)
                DetailRow("Lokasi pengambilan", item.pickupLocation.ifBlank { "Belum diisi oleh pelapor" })
                if (item.pickupNotes.isNotBlank()) DetailRow("Petunjuk pengambilan", item.pickupNotes)
                DetailRow("Status", item.status.foundStatusLabel())
            }
            FoundSectionCard("Pelapor") {
                if (state.reporterLoading) CircularProgressIndicator()
                FoundMessage(state.reporterError)
                if (state.reporterError != null) TextButton(onClick = { viewModel.load(itemId) }) { AppIcon(R.drawable.ic_refresh_cw, null); Text(" Muat ulang pelapor") }
                state.reporter?.let { reporter ->
                    DetailRow("Nama", reporter.displayName)
                    DetailRow("Kampus pelapor", reporter.campus.ifBlank { "Belum diisi" })
                    if (reporter.contactEmail.isNullOrBlank() && reporter.phone.isNullOrBlank()) {
                        Text("Pelapor tidak membagikan kontak. Ikuti petunjuk pengambilan yang tersedia.")
                    }
                    reporter.contactEmail?.takeIf { it.isNotBlank() }?.let { email ->
                        TextButton(onClick = {
                            try { uriHandler.openUri("mailto:" + Uri.encode(email, "@")) }
                            catch (_: Exception) { contactError = "Aplikasi email tidak tersedia. Gunakan alamat kontak yang ditampilkan." }
                        }) { AppIcon(R.drawable.ic_mail_duotone, null); Text(" Email: $email") }
                    }
                    reporter.phone?.takeIf { it.isNotBlank() }?.let { phone ->
                        TextButton(onClick = {
                            try { uriHandler.openUri("tel:" + Uri.encode(phone, "+")) }
                            catch (_: Exception) { contactError = "Aplikasi telepon tidak tersedia. Gunakan nomor yang ditampilkan." }
                        }) { AppIcon(R.drawable.ic_phone_duotone, null); Text(" Telepon: $phone") }
                    }
                    Text("Hubungi pelapor untuk koordinasi. Kontak langsung bukan persetujuan klaim; siapkan bukti kepemilikan.", style = MaterialTheme.typography.bodySmall)
                }
                if (!state.reporterLoading && state.reporter == null && state.reporterError == null) {
                    Text("Pelapor belum melengkapi profil.")
                }
                FoundMessage(contactError)
            }
            if (viewModel.userId == item.reporterId) {
                if (!state.isSaving) {
                    GradientPrimaryButton("Edit Laporan / Ubah Status", onClick = { onEdit(item.id) })
                }
                OutlinedButton(
                    onClick = { confirmDelete = true }, enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AppIcon(R.drawable.ic_trash_duotone, null)
                    Text(if (state.isSaving) " Menghapus..." else " Hapus Laporan",
                        color = MaterialTheme.colorScheme.error)
                }
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
