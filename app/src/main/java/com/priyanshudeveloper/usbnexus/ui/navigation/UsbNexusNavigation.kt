package com.priyanshudeveloper.usbnexus.ui.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.priyanshudeveloper.usbnexus.phoenixengine.viewmodel.StartupViewModel
import com.priyanshudeveloper.usbnexus.ui.dashboard.DashboardScreen
import com.priyanshudeveloper.usbnexus.ui.settings.SavedUsbHistoryScreen
import com.priyanshudeveloper.usbnexus.ui.settings.SettingsScreen
import com.priyanshudeveloper.usbnexus.ui.startup.StartupScreen

object UsbNexusRoutes {
    const val STARTUP = "startup"
    const val DASHBOARD = "dashboard"
    const val SETTINGS = "settings"
    const val SAVED_HISTORY = "saved_history"
}

@Composable
fun UsbNexusNavigation(
    viewModel: StartupViewModel,
    navController: NavHostController = rememberNavController()
) {
    val state by viewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = UsbNexusRoutes.STARTUP
    ) {
        composable(UsbNexusRoutes.STARTUP) {
            StartupScreen(
                viewModel = viewModel,
                onStartupFinished = {
                    navController.navigate(UsbNexusRoutes.DASHBOARD) {
                        popUpTo(UsbNexusRoutes.STARTUP) { inclusive = true }
                    }
                }
            )
        }

        composable(UsbNexusRoutes.DASHBOARD) {
            DashboardScreen(
                deviceProfile = state.deviceProfile,
                manufacturer = state.manufacturer,
                model = state.model,
                portType = state.portType,
                onNavigateToSettings = {
                    navController.navigate(UsbNexusRoutes.SETTINGS)
                }
            )
        }

        composable(
            route = UsbNexusRoutes.SETTINGS,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(1000, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(1000))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(1000, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(1000))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(1000, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(1000))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(1000, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(1000))
            }
        ) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToHistory = {
                    navController.navigate(UsbNexusRoutes.SAVED_HISTORY)
                }
            )
        }

        composable(
            route = UsbNexusRoutes.SAVED_HISTORY,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(1000, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(1000))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(1000, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(1000))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(1000, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(1000))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(1000, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(1000))
            }
        ) {
            SavedUsbHistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
