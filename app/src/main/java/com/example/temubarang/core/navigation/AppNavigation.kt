package com.example.temubarang.core.navigation

import android.app.Application
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavHostController
import com.example.temubarang.features.auth.model.AuthUiState
import com.example.temubarang.features.claims.ui.screen.ClaimsScreen
import com.example.temubarang.features.found.ui.screen.FoundItemDetailScreen
import com.example.temubarang.features.found.ui.screen.FoundItemFormScreen
import com.example.temubarang.features.found.ui.screen.FoundItemsScreen
import com.example.temubarang.features.found.viewmodel.FoundItemsViewModel
import com.example.temubarang.features.found.viewmodel.FoundDraftViewModel
import com.example.temubarang.features.home.ui.screen.HomeScreen
import com.example.temubarang.features.home.viewmodel.HomeViewModel
import com.example.temubarang.features.lost.ui.screen.LostItemsScreen
import com.example.temubarang.ui.theme.BrandBackground

object AppRoute {
    const val HOME = "home"
    const val LOST = "lost_items"
    const val FOUND = "found_items"
    const val FOUND_CREATE = "found_items/new"
    const val FOUND_DETAIL = "found_items/detail"
    const val FOUND_EDIT = "found_items/edit"
    const val CLAIMS = "claims"
}

private data class MainTab(val route: String, val label: String, val symbol: String)
private val mainTabs = listOf(
    MainTab(AppRoute.HOME, "Beranda", "⌂"),
    MainTab(AppRoute.LOST, "Hilang", "⌕"),
    MainTab(AppRoute.FOUND, "Ditemukan", "▣"),
    MainTab(AppRoute.CLAIMS, "Klaim", "✓")
)

/**
 * Home, Barang Hilang, Ditemukan, dan Klaim merupakan destinasi utama.
 * Form/detail adalah subhalaman sehingga bottom navigation disembunyikan.
 * Semua ViewModel di-scope pada AuthenticatedNavigation, bukan dibuat ulang per route.
 */
@Composable
fun AuthenticatedNavigation(
    authState: AuthUiState,
    navController: NavHostController,
    foundDraftViewModel: FoundDraftViewModel,
    onLaunchCamera: () -> Unit,
    onLaunchGallery: () -> Unit,
    onLogout: () -> Unit
) {
    val application = LocalContext.current.applicationContext as Application
    val foundVm: FoundItemsViewModel = viewModel(factory = FoundItemsViewModel.factory(application))
    val homeVm: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val onMainTab = mainTabs.any { it.route == currentRoute }
    val cameraReturnPending by foundDraftViewModel.returnToForm.collectAsState()

    // Setelah kamera menutup, kembalikan screen form. Jika Activity/Process mati saat
    // kamera terbuka, NavController dapat dipulihkan dari saved state dan draft tetap
    // berada pada SavedStateHandle yang hidup di scope MainActivity.
    LaunchedEffect(cameraReturnPending, currentRoute) {
        if (cameraReturnPending && currentRoute != null) {
            if (currentRoute != AppRoute.FOUND_CREATE) {
                navController.navigate(AppRoute.FOUND_CREATE) { launchSingleTop = true }
            }
            foundDraftViewModel.acknowledgeReturnToForm()
        }
    }

    Scaffold(
        containerColor = BrandBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (onMainTab) {
                NavigationBar {
                    mainTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                if (currentRoute != tab.route) {
                                    navController.navigate(tab.route) {
                                        popUpTo(AppRoute.HOME) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Text(tab.symbol) },
                            label = { Text(tab.label) },
                            alwaysShowLabel = true
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppRoute.HOME) {
                HomeScreen(
                    viewModel = homeVm,
                    onOpenLost = { navController.navigate(AppRoute.LOST) },
                    onOpenFound = { navController.navigate(AppRoute.FOUND) },
                    onLogout = onLogout,
                    isLoggingOut = authState.isSubmitting,
                    errorMessage = authState.error
                )
            }
            composable(AppRoute.LOST) {
                LostItemsScreen(onBack = { navController.navigate(AppRoute.HOME) {
                    popUpTo(AppRoute.HOME) { inclusive = false }
                    launchSingleTop = true
                } })
            }
            composable(AppRoute.FOUND) {
                FoundItemsScreen(
                    viewModel = foundVm,
                    onBack = { navController.navigate(AppRoute.HOME) {
                        popUpTo(AppRoute.HOME) { inclusive = false }
                        launchSingleTop = true
                    } },
                    onCreate = { navController.navigate(AppRoute.FOUND_CREATE) },
                    onDetail = { id -> navController.navigate("${AppRoute.FOUND_DETAIL}/$id") }
                )
            }
            composable(AppRoute.FOUND_CREATE) {
                FoundItemFormScreen(
                    viewModel = foundVm, itemId = null,
                    draftViewModel = foundDraftViewModel,
                    onLaunchCamera = onLaunchCamera,
                    onLaunchGallery = onLaunchGallery,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }
            composable("${AppRoute.FOUND_DETAIL}/{itemId}") { entry ->
                val id = entry.arguments?.getString("itemId") ?: return@composable
                FoundItemDetailScreen(
                    viewModel = foundVm, itemId = id,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate("${AppRoute.FOUND_EDIT}/$it") }
                )
            }
            composable("${AppRoute.FOUND_EDIT}/{itemId}") { entry ->
                val id = entry.arguments?.getString("itemId") ?: return@composable
                FoundItemFormScreen(
                    viewModel = foundVm, itemId = id,
                    draftViewModel = foundDraftViewModel,
                    onLaunchCamera = onLaunchCamera,
                    onLaunchGallery = onLaunchGallery,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }
            composable(AppRoute.CLAIMS) {
                ClaimsScreen(onBack = { navController.navigate(AppRoute.HOME) {
                    popUpTo(AppRoute.HOME) { inclusive = false }
                    launchSingleTop = true
                } })
            }
        }
    }
}
