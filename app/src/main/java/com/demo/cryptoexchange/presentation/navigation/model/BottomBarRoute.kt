package com.demo.cryptoexchange.presentation.navigation.model

sealed class BottomBarRoute(
    val route: String,
    val title: String
) {
    object Market : BottomBarRoute("market", "A")
    object B : BottomBarRoute("screen_b",  "B")
    object C : BottomBarRoute("screen_c",  "C")
    object D : BottomBarRoute("screen_d", "D")

    companion object {
        fun getBottomNavItems() = listOf(Market, B, C, D)
    }
}