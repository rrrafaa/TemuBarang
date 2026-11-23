package com.example.temubarang.core.navigation

import com.example.temubarang.R
import com.example.temubarang.ui.component.AppNavigationItem
import com.example.temubarang.ui.component.AppBottomNavigation
import android.app.Application
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
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
import com.example.temubarang.features.lost.ui.screen.LostItemFormScreen
import com.example.temubarang.features.lost.viewmodel.LostItemsViewModel
import com.example.temubarang.features.lost.viewmodel.LostDraftViewModel
import com.example.temubarang.features.lost.ui.screen.LostItemDetailScreen
import com.example.temubarang.ui.theme.BrandBackground

import com.example.temubarang.features.profile.ui.screen.ProfileScreen
import com.example.temubarang.features.profile.viewmodel.ProfileViewModel

object AppRoute {
    const val HOME = "home"
    const val LOST = "lost_items"
    const val LOST_CREATE = "lost_items/new"
    const val LOST_DETAIL = "lost_items/detail"
    const val LOST_EDIT = "lost_items/edit"
    const val FOUND = "found_items"
    const val FOUND_CREATE = "found_items/new"
    const val FOUND_DETAIL = "found_items/detail"
    const val FOUND_EDIT = "found_items/edit"
    const val PROFILE = "profile"
    const val CLAIMS = "claims"
}

private val mainTabs = listOf(
    AppNavigationItem(AppRoute.HOME, "Beranda", R.drawable.ic_house_duotone),
    AppNavigationItem(AppRoute.LOST, "Hilang", R.drawable.ic_search_duotone),
    AppNavigationItem(AppRoute.FOUND, "Ditemukan", R.drawable.ic_package_check_duotone),
    AppNavigationItem(AppRoute.CLAIMS, "Klaim", R.drawable.ic_clipboard_check_duotone),
    AppNavigationItem(AppRoute.PROFILE, "Profil", R.drawable.ic_user_round_duotone)
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
    lostDraftViewModel: LostDraftViewModel,
    onLaunchLostCamera: (String?) -> Unit,
    onLaunchLostGallery: (String?) -> Unit,
    onLaunchCamera: () -> Unit,
    onLaunchGallery: () -> Unit,
    onLogout: () -> Unit
) {
    val application = LocalContext.current.applicationContext as Application
    val foundVm: FoundItemsViewModel = viewModel(key = "found:${authState.userId}", factory = FoundItemsViewModel.factory(application))
    val homeVm: HomeViewModel = viewModel(key = "home:${authState.userId}", factory = HomeViewModel.Factory)
    val profileVm: ProfileViewModel = viewModel(key = "profile:${authState.userId}", factory = ProfileViewModel.Factory)
    val profileState by profileVm.state.collectAsState()
    val foundState by foundVm.uiState.collectAsState()
    LaunchedEffect(foundState.revision) {
        if (foundState.revision > 0) homeVm.refresh()
    }
    val lostVm: LostItemsViewModel = viewModel(key = "lost:${authState.userId}",
        factory = LostItemsViewModel.factory(application))
    val lostState by lostVm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    // Snackbar berada di luar NavHost sehingga tetap terlihat setelah navigasi.
    LaunchedEffect(lostState.notice) {
        lostState.notice?.let { snackbarHostState.showSnackbar(it) }
    }
    LaunchedEffect(foundState.notice) {
        foundState.notice?.let { snackbarHostState.showSnackbar(it) }
    }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val onMainTab = mainTabs.any { it.route == currentRoute }
    // Satu jalur navigasi untuk tombol Beranda dan seluruh tab, tanpa menumpuk destinasi.
    val openMainTab: (String) -> Unit = { route ->
        if (currentRoute != route) {
            navController.navigate(route) {
                popUpTo(AppRoute.HOME) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }
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

    val lostReturnPending by lostDraftViewModel.returnToForm.collectAsState()
    val lostReturnRoute by lostDraftViewModel.returnRoute.collectAsState()
    LaunchedEffect(lostReturnPending, lostReturnRoute, currentRoute) {
        if (lostReturnPending && currentRoute != null) {
            if (currentRoute != lostReturnRoute) {
                navController.navigate(lostReturnRoute) { launchSingleTop = true }
            }
            lostDraftViewModel.acknowledgeReturn()
        }
    }

    Scaffold(
        containerColor = BrandBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (onMainTab) {
                AppBottomNavigation(mainTabs, currentRoute, onSelect = openMainTab)
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
                    userName = profileState.profile?.displayName.orEmpty(),
                    onOpenLost = { openMainTab(AppRoute.LOST) },
                    onOpenFound = { openMainTab(AppRoute.FOUND) },
                    onFoundDetail = { navController.navigate("${AppRoute.FOUND_DETAIL}/$it") },
                    errorMessage = authState.error
                )
            }
            composable(AppRoute.LOST) {
                LostItemsScreen(
                    viewModel = lostVm,
                    onBack = { navController.navigate(AppRoute.HOME) {
                        popUpTo(AppRoute.HOME) { inclusive = false }
                        launchSingleTop = true
                    } },
                    onCreate = { navController.navigate(AppRoute.LOST_CREATE) },
                    onDetail = { id -> navController.navigate("${AppRoute.LOST_DETAIL}/$id") }
                )
            }
            composable(AppRoute.LOST_CREATE) {
                LostItemFormScreen(
                    viewModel = lostVm,
                    draftViewModel = lostDraftViewModel,
                    onLaunchCamera = { onLaunchLostCamera(null) },
                    onLaunchGallery = { onLaunchLostGallery(null) },
                    itemId = null,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.navigate(AppRoute.LOST) {
                        popUpTo(AppRoute.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    } }
                )
            }

            composable("${AppRoute.LOST_DETAIL}/{itemId}") { entry ->
                val id = entry.arguments?.getString("itemId") ?: return@composable
                LostItemDetailScreen(
                    viewModel = lostVm,
                    itemId = id,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate("${AppRoute.LOST_EDIT}/$it") },
                    onStatusUpdated = { navController.navigate(AppRoute.LOST) {
                        popUpTo(AppRoute.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    } }
                )
            }

            composable("${AppRoute.LOST_EDIT}/{itemId}") { entry ->
                val id = entry.arguments?.getString("itemId") ?: return@composable
                LostItemFormScreen(
                    viewModel = lostVm,
                    draftViewModel = lostDraftViewModel,
                    onLaunchCamera = { onLaunchLostCamera(id) },
                    onLaunchGallery = { onLaunchLostGallery(id) },
                    itemId = id,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.navigate(AppRoute.LOST) {
                        popUpTo(AppRoute.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    } }
                )
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
                    onSaved = { navController.navigate(AppRoute.FOUND) {
                        popUpTo(AppRoute.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    } }
                )
            }
            composable("${AppRoute.FOUND_DETAIL}/{itemId}") { entry ->
                val id = entry.arguments?.getString("itemId") ?: return@composable
                FoundItemDetailScreen(
                    viewModel = foundVm, itemId = id,
                    onBack = { navController.popBackStack() },
                    onDeleted = {
                        // Detail juga bisa dibuka langsung dari Beranda.
                        navController.navigate(AppRoute.FOUND) {
                            popUpTo(AppRoute.HOME)
                            launchSingleTop = true
                        }
                    },
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
                    onSaved = { navController.navigate(AppRoute.FOUND) {
                        popUpTo(AppRoute.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    } }
                )
            }
            composable(AppRoute.PROFILE) {
                ProfileScreen(profileVm, onLogout, authState.isSubmitting, authState.error)
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