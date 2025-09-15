package com.demo.cryptoexchange.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.demo.cryptoexchange.presentation.navigation.components.PlaceholderScreen
import com.demo.cryptoexchange.presentation.navigation.constants.ScreenRoute
import com.demo.feature_market.presentation.ui.MarketScreen
import com.demo.feature_market.presentation.ui.MarketSpotScreenPreview

@Composable
fun AppNavHost(
    navController: NavHostController,
    isPreview: Boolean = false,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = ScreenRoute.Market.name,
        modifier = modifier
    ) {
        // Market Screen
        composable(ScreenRoute.Market.name) {
            if (isPreview) {
                MarketSpotScreenPreview()
            } else {
                MarketScreen()
            }
        }

        // Portfolio Screen
        composable(ScreenRoute.B.name) {
            PlaceholderScreen(ScreenRoute.B.label)
        }

        // Trade Screen
        composable(ScreenRoute.C.name) {
            PlaceholderScreen(ScreenRoute.C.label)
        }

        // Profile Screen
        composable(ScreenRoute.D.name) {
            PlaceholderScreen(ScreenRoute.D.label)
        }
    }
}