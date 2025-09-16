package com.demo.cryptoexchange.presentation.navigation.model

sealed class RouteScreen(
    val route: String,
    val title: String
) {
    object Market : RouteScreen("market", "A")
    object B : RouteScreen("screen_b",  "B")
    object C : RouteScreen("screen_c",  "C")
    object D : RouteScreen("screen_d", "D")
    object Setting : RouteScreen("setting", "Setting")

    companion object {
        fun getBottomNavItems() = listOf(Market, B, C, D)
    }
}