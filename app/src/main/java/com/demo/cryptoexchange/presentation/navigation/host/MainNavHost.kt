package com.demo.cryptoexchange.presentation.navigation.host

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.demo.cryptoexchange.presentation.navigation.model.MainRoute
import com.demo.cryptoexchange.presentation.navigation.screens.MainTabsScreen
import com.demo.cryptoexchange.presentation.navigation.screens.SettingScreen

@Composable
fun MainNavHost(
    isPreview: Boolean = false,
    rootNavController: NavHostController
) {

    NavHost(navController = rootNavController, startDestination = MainRoute.Tabs.route) {
        // Main Tabs Graph
        composable(MainRoute.Tabs.route) {
            MainTabsScreen(
                isPreview = isPreview,
                onOpenSetting = { rootNavController.navigate(MainRoute.Setting.route) })
        }

        // Full-screen settings destination
        composable(MainRoute.Setting.route) {
            SettingScreen(onBackClick = { rootNavController.popBackStack() })
        }
    }
}