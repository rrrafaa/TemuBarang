package com.example.temubarang.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.temubarang.R
import com.example.temubarang.ui.theme.TemuBarangTheme

/** Preview komponen tanpa NavController, ViewModel, atau koneksi backend. */
@Preview(name = "Header dan navbar", widthDp = 360, showBackground = true)
@Preview(name = "Header dan navbar - teks besar", widthDp = 320, fontScale = 1.3f, showBackground = true)
@Composable
private fun AppChromePreview() {
    TemuBarangTheme {
        Surface {
            Column {
                AppHeader("Barang Ditemukan", onBack = {}, onRefresh = {})
                AppBottomNavigation(
                    items = listOf(
                        AppNavigationItem("home", "Beranda", R.drawable.ic_house_duotone),
                        AppNavigationItem("lost", "Hilang", R.drawable.ic_search_duotone),
                        AppNavigationItem("found", "Ditemukan", R.drawable.ic_package_check_duotone),
                        AppNavigationItem("claims", "Klaim", R.drawable.ic_clipboard_check_duotone),
                        AppNavigationItem("profile", "Profil", R.drawable.ic_user_round_duotone)
                    ),
                    selectedRoute = "found", onSelect = {}
                )
            }
        }
    }
}
