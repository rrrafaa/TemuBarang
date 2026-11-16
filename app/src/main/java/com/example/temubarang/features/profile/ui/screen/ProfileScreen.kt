package com.example.temubarang.features.profile.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.temubarang.features.found.ui.component.FoundMessage
import com.example.temubarang.features.profile.viewmodel.ProfileViewModel
import com.example.temubarang.ui.component.GradientPrimaryButton

@Composable
fun ProfileScreen(viewModel: ProfileViewModel, onLogout: () -> Unit, isLoggingOut: Boolean, logoutError: String?) {
    val state by viewModel.state.collectAsState()
    val profile = state.profile
    var name by rememberSaveable(profile?.id) { mutableStateOf(profile?.displayName.orEmpty()) }
    var campus by rememberSaveable(profile?.id) { mutableStateOf(profile?.campus.orEmpty()) }
    var email by rememberSaveable(profile?.id) { mutableStateOf(profile?.contactEmail.orEmpty()) }
    var phone by rememberSaveable(profile?.id) { mutableStateOf(profile?.phone.orEmpty()) }
    var share by rememberSaveable(profile?.id) { mutableStateOf(profile?.shareContact ?: false) }
    val enabled = !state.loading && !state.saving && !isLoggingOut
    LazyColumn(
        Modifier.fillMaxSize().systemBarsPadding().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(vertical = 20.dp)
    ) {
        item { Text("Profil Saya", style = MaterialTheme.typography.headlineMedium) }
        item { Text("Lengkapi profil agar pengambilan barang di kampus lebih mudah dikoordinasikan.") }
        item { FoundMessage(state.error); FoundMessage(state.notice, isError = false) }
        if (state.loading) item { CircularProgressIndicator() }
        if (profile != null) {
            item { OutlinedTextField(name, { name = it }, label = { Text("Nama tampilan *") }, singleLine = true, enabled = enabled, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(campus, { campus = it }, label = { Text("Kampus *") }, singleLine = true, enabled = enabled, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(email, { email = it }, label = { Text("Email kontak (opsional)") }, singleLine = true, enabled = enabled, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(phone, { phone = it }, label = { Text("Telepon (opsional)") }, supportingText = { Text("Gunakan format +628… tanpa spasi.") }, singleLine = true, enabled = enabled, modifier = Modifier.fillMaxWidth()) }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Bagikan kontak pada laporan saya", Modifier.weight(1f))
                    Switch(share, { share = it }, enabled = enabled)
                }
                Text("Nama dan kampus terlihat bagi pengguna yang login melalui detail laporan. Email dan telepon hanya dibagikan jika opsi ini aktif. Email kontak tidak mengubah email login.", style = MaterialTheme.typography.bodySmall)
            }
            item { GradientPrimaryButton("Simpan Profil", enabled = enabled, loading = state.saving, onClick = { viewModel.save(name, campus, email, phone, share) }) }
        } else if (!state.loading) item { OutlinedButton(onClick = viewModel::load) { Text("Coba Muat Profil Lagi") } }
        item { FoundMessage(logoutError) }
        item { OutlinedButton(onClick = onLogout, enabled = !state.saving && !isLoggingOut, modifier = Modifier.fillMaxWidth()) { Text(if (isLoggingOut) "Keluar…" else "Keluar Akun") } }
    }
}
