package com.demo.cryptoexchange.presentation.navigation.host

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.demo.cryptoexchange.presentation.navigation.components.PlaceholderScreen
import com.demo.cryptoexchange.presentation.navigation.model.BottomBarRoute
import com.demo.cryptoexchange.presentation.navigation.screens.DScreen
import com.demo.feature_market.presentation.ui.MarketScreen
import com.demo.feature_market.presentation.ui.MarketSpotScreenPreview

@Composable
fun TabsNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    isPreview: Boolean = false,
    onOpenSetting: () -> Unit
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = BottomBarRoute.Market.route,
    ) {
        // Market Screen
        composable(BottomBarRoute.Market.route) {
            if (isPreview) {
                MarketSpotScreenPreview()
            } else {
                MarketScreen()
            }
        }

        // Portfolio Screen
        composable(BottomBarRoute.B.route) {
            PlaceholderScreen(BottomBarRoute.B.title)
        }

        // Trade Screen
        composable(BottomBarRoute.C.route) {
            PlaceholderScreen(BottomBarRoute.C.title)
        }

        // Profile Screen
        composable(BottomBarRoute.D.route) {
            DScreen(
                screenName = BottomBarRoute.D.title,
                onSettingClick = onOpenSetting,
            )
        }
    }
}