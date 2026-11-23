package com.example.temubarang.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Token bersama; gunakan di komponen agar perubahan tampilan tidak tersebar. */
object AppSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
}

object AppRadius {
    val field = 16.dp
    val search = 28.dp
    val card = 20.dp
    val button = 16.dp
    val badge = 10.dp
}

object AppSizes {
    val minTouch = 48.dp
    val search = 56.dp
    val button = 54.dp
    val previewPhoto = 96.dp
}

object AppStatusColors {
    val successBackground = BrandSoftMint
    val successText = Color(0xFF116B5E)
    val warningBackground = Color(0xFFFFF4DD)
    val warningText = Color(0xFF875E18)
}
