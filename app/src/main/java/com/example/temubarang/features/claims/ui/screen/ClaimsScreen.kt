package com.example.temubarang.features.claims.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.temubarang.ui.component.AppPage

@Composable
fun ClaimsScreen(onBack: () -> Unit) {
    AppPage("Klaim Kepemilikan", onBack = onBack) {
        Text("Fitur klaim kepemilikan sedang dikembangkan.", Modifier.padding(24.dp))
    }
}
