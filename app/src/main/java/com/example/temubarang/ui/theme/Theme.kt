package com.example.temubarang.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Dynamic color dinonaktifkan agar branding tetap konsisten di setiap perangkat.
private val TemuBarangColors = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = BrandSoftBlue,
    onPrimaryContainer = BrandNavy,
    secondary = BrandCyan,
    onSecondary = BrandNavy,
    secondaryContainer = BrandSoftMint,
    onSecondaryContainer = BrandNavy,
    background = BrandBackground,
    onBackground = BrandText,
    surface = Color.White,
    onSurface = BrandText,
    surfaceVariant = BrandSoftBlue,
    onSurfaceVariant = BrandMuted,
    outline = BrandOutline,
    error = Color(0xFFB42336),
    errorContainer = Color(0xFFFFECEE)
)

@Composable
fun TemuBarangTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TemuBarangColors,
        typography = Typography,
        content = content
    )
}
