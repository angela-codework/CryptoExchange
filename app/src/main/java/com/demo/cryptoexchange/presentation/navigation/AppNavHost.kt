package com.demo.cryptoexchange.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.demo.cryptoexchange.presentation.navigation.components.PlaceholderScreen
import com.demo.cryptoexchange.presentation.navigation.model.RouteScreen
import com.demo.cryptoexchange.presentation.navigation.screens.DScreen
import com.demo.cryptoexchange.presentation.navigation.screens.SettingScreen
import com.demo.feature_market.presentation.ui.MarketScreen
import com.demo.feature_market.presentation.ui.MarketSpotScreenPreview

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    isPreview: Boolean = false
) {
    NavHost(
        navController = navController,
        startDestination = RouteScreen.Market.route,
        modifier = modifier
    ) {
        // Market Screen
        composable(RouteScreen.Market.route) {
            if (isPreview) {
                MarketSpotScreenPreview()
            } else {
                MarketScreen()
            }
        }

        // B Screen
        composable(RouteScreen.B.route) {
            PlaceholderScreen(RouteScreen.B.title)
        }

        // C Screen
        composable(RouteScreen.C.route) {
            PlaceholderScreen(RouteScreen.C.title)
        }

        // D Screen
        composable(RouteScreen.D.route) {
            DScreen(
                screenName = RouteScreen.D.title,
                onSettingClick = { navController.navigate(RouteScreen.Setting.route) },
            )
        }

        // Settings Screen
        composable(RouteScreen.Setting.route) {
            SettingScreen(onBackClick = { navController.popBackStack() })
        }
    }
}