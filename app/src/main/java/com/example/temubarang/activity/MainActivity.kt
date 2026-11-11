package com.example.temubarang.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.temubarang.core.navigation.AuthenticatedNavigation
import com.example.temubarang.core.supabase.SupabaseProvider
import com.example.temubarang.features.auth.ui.screen.AuthScreen
import com.example.temubarang.features.auth.viewmodel.AuthViewModel
import com.example.temubarang.features.found.ui.camera.CameraPhotoUri
import com.example.temubarang.features.found.viewmodel.FoundDraftViewModel
import com.example.temubarang.ui.theme.TemuBarangTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TemuBarangTheme { TemuBarangRoot() }
        }
    }
}

@Composable
private fun TemuBarangRoot() {
    if (!SupabaseProvider.isConfigured) {
        SetupRequiredScreen()
        return
    }

    // Kesalahan inisialisasi client juga harus tampil di UI, bukan menutup aplikasi.
    val initializationError = remember { runCatching { SupabaseProvider.client }.exceptionOrNull() }
    if (initializationError != null) {
        CenteredMessage("Tidak dapat menginisialisasi Supabase. Periksa URL, publishable key, dan konfigurasi dependency, lalu jalankan ulang aplikasi.")
        return
    }

    // Tetap dibuat di root, bukan dalam cabang auth. Karena itu NavController dan
    // callback hasil kamera dapat dipulihkan meskipun Activity direkreasi.
    val navController = rememberNavController()
    val context = LocalContext.current
    val draftViewModel: FoundDraftViewModel = viewModel()

    // Registrasi ActivityResult berada DI LUAR cabang isInitializing/isAuthenticated.
    // Wajib selalu tersedia ketika Activity dibuat ulang sewaktu kamera aktif.
    val cameraContract = remember { object : ActivityResultContracts.TakePicture() {
            override fun createIntent(context: Context, input: Uri): Intent =
                super.createIntent(context, input).apply {
                    // Beberapa aplikasi kamera memerlukan izin URI eksplisit untuk
                    // menulis ke FileProvider milik TemuBarang.
                    clipData = ClipData.newRawUri("Foto TemuBarang", input)
                    addFlags(
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
        } }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = cameraContract
    ) { success ->
        val uri = draftViewModel.pendingCameraUri.value?.let(Uri::parse)
        // Sejumlah aplikasi kamera mengembalikan RESULT_CANCELED meski telah menulis file.
        // Pastikan ukuran file sungguhan sebelum menyatakan gagal.
        val photoReady = uri != null && CameraPhotoUri.hasPhoto(context, uri)
        if (!success || !photoReady) {
            android.util.Log.w(
                "TemuBarangCamera",
                "Kamera mengembalikan success=$success, foto terisi=$photoReady"
            )
        }
        draftViewModel.finishCameraCapture(photoReady)
    }
    val galleryContract = remember { ActivityResultContracts.GetContent() }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = galleryContract
    ) { uri ->
        draftViewModel.finishGallerySelection(uri?.toString())
    }

    val openCamera: () -> Unit = {
        try {
            val destination = CameraPhotoUri.create(context)
            draftViewModel.beginCameraCapture(destination.toString())
            cameraLauncher.launch(destination)
        } catch (e: ActivityNotFoundException) {
            draftViewModel.cameraLaunchFailed("Tidak ada aplikasi kamera pada perangkat ini.")
        } catch (e: Exception) {
            android.util.Log.e("TemuBarangCamera", "Gagal membuka kamera", e)
            draftViewModel.cameraLaunchFailed(
                "Kamera tidak dapat dibuka. Coba lagi atau gunakan Galeri."
            )
        }
    }
    val openGallery: () -> Unit = {
        try {
            draftViewModel.beginGallerySelection()
            galleryLauncher.launch("image/*")
        } catch (e: Exception) {
            android.util.Log.e("TemuBarangCamera", "Gagal membuka galeri", e)
            draftViewModel.cameraLaunchFailed("Galeri tidak dapat dibuka. Silakan coba lagi.")
        }
    }

    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    when {
        authState.isInitializing -> CenteredMessage("Memeriksa sesi pengguna...", loading = true)
        authState.isAuthenticated -> AuthenticatedNavigation(
            authState = authState,
            navController = navController,
            foundDraftViewModel = draftViewModel,
            onLaunchCamera = openCamera,
            onLaunchGallery = openGallery,
            onLogout = authViewModel::logout
        )
        else -> AuthScreen(
            state = authState,
            onLogin = authViewModel::login,
            onRegister = authViewModel::register,
            onClearMessage = authViewModel::clearMessage,
            onRegistrationHandled = authViewModel::acknowledgeRegistration
        )
    }
}

@Composable
private fun SetupRequiredScreen() {
    CenteredMessage("Supabase belum dikonfigurasi. Tambahkan SUPABASE_URL dan SUPABASE_PUBLISHABLE_KEY ke local.properties, lalu Sync Gradle dan jalankan ulang aplikasi.")
}

@Composable
private fun CenteredMessage(message: String, loading: Boolean = false) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (loading) CircularProgressIndicator()
        Text(message, style = MaterialTheme.typography.bodyLarge)
    }
}
