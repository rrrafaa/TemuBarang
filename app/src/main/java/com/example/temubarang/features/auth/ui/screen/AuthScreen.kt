package com.example.temubarang.features.auth.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.temubarang.features.auth.model.AuthUiState
import com.example.temubarang.features.auth.ui.component.EmailField
import com.example.temubarang.features.auth.ui.component.NameField
import com.example.temubarang.features.auth.ui.component.PasswordField
import com.example.temubarang.ui.component.BrandLogo
import com.example.temubarang.ui.component.GradientPrimaryButton
import com.example.temubarang.ui.theme.BrandBackground
import com.example.temubarang.ui.theme.BrandMuted
import com.example.temubarang.ui.theme.BrandNavy
import com.example.temubarang.ui.theme.BrandSoftBlue

@Composable
fun AuthScreen(
    state: AuthUiState,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String, String) -> Unit,
    onClearMessage: () -> Unit,
    onRegistrationHandled: () -> Unit
) {
    var isRegister by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmation by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(state.registrationComplete) {
        if (state.registrationComplete) {
            isRegister = false
            password = ""
            confirmation = ""
            onRegistrationHandled()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .background(BrandBackground)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        BrandLogo(modifier = Modifier.padding(bottom = 8.dp))
        Text(
            text = if (isRegister) "Buat akun TemuBarang" else "Selamat datang kembali",
            style = MaterialTheme.typography.headlineMedium,
            color = BrandNavy,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(7.dp))
        Text(
            if (isRegister) "Daftar agar dapat melaporkan dan mengklaim barang."
            else "Masuk untuk mencari barang hilang dan melaporkan barang temuan.",
            style = MaterialTheme.typography.bodyMedium,
            color = BrandMuted,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(22.dp))
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isRegister) NameField(name, { name = it; onClearMessage() }, !state.isSubmitting)
                EmailField(email, { email = it; onClearMessage() }, !state.isSubmitting)
                PasswordField(password, { password = it; onClearMessage() }, enabled = !state.isSubmitting)
                if (isRegister) PasswordField(
                    confirmation, { confirmation = it; onClearMessage() },
                    label = "Konfirmasi password", enabled = !state.isSubmitting
                )

                // Error atau sukses langsung tampil dalam card tanpa harus membuka Logcat.
                state.error?.let { FeedbackBanner(message = it, isError = true) }
                state.notice?.let { FeedbackBanner(message = it, isError = false) }

                Spacer(Modifier.height(4.dp))
                GradientPrimaryButton(
                    text = if (isRegister) "Daftar akun" else "Masuk",
                    enabled = !state.isSubmitting,
                    loading = state.isSubmitting,
                    onClick = {
                        if (isRegister) onRegister(name, email, password, confirmation)
                        else onLogin(email, password)
                    }
                )
                TextButton(
                    onClick = {
                        isRegister = !isRegister
                        password = ""
                        confirmation = ""
                        onClearMessage()
                    },
                    enabled = !state.isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (isRegister) "Sudah punya akun? Masuk" else "Belum punya akun? Daftar",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("TemuBarang • Lost & Found", style = MaterialTheme.typography.bodyMedium, color = BrandMuted)
    }
}

@Composable
private fun FeedbackBanner(message: String, isError: Boolean) {
    Surface(
        color = if (isError) MaterialTheme.colorScheme.errorContainer else BrandSoftBlue,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            color = if (isError) MaterialTheme.colorScheme.error else BrandNavy,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(12.dp)
        )
    }
}
