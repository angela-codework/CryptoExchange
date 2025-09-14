package com.demo.cryptoexchange.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.demo.cryptoexchange.presentation.navigation.constants.NavigationRoutes
import com.demo.cryptoexchange.presentation.navigation.components.PlaceholderScreen
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
        startDestination = NavigationRoutes.A.name,
        modifier = modifier
    ) {
        // Market Screen
        composable(NavigationRoutes.A.name) {
            if (isPreview) {
                MarketSpotScreenPreview()
            } else {
                MarketScreen()
            }
        }

        // Portfolio Screen
        composable(NavigationRoutes.B.name) {
            PlaceholderScreen(NavigationRoutes.B.label)
        }

        // Trade Screen
        composable(NavigationRoutes.C.name) {
            PlaceholderScreen(NavigationRoutes.C.label)
        }

        // Profile Screen
        composable(NavigationRoutes.D.name) {
            PlaceholderScreen(NavigationRoutes.D.label)
        }
    }
}