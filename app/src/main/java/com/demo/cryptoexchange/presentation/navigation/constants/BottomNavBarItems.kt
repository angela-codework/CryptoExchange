package com.demo.cryptoexchange.presentation.navigation.constants

import com.demo.cryptoexchange.presentation.navigation.model.BottomNavItem

object BottomNavBarItems {
    val items = listOf(
        BottomNavItem(ScreenRoute.Market.name, ScreenRoute.Market.label),
        BottomNavItem(ScreenRoute.B.name, ScreenRoute.B.label),
        BottomNavItem(ScreenRoute.C.name,  ScreenRoute.C.label),
        BottomNavItem(ScreenRoute.D.name, ScreenRoute.D.label)
    )
}