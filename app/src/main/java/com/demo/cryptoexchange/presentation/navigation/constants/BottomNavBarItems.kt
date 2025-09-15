package com.demo.cryptoexchange.presentation.navigation.constants

import com.demo.cryptoexchange.presentation.navigation.model.BottomNavItem

object BottomNavBarItems {
    val items = listOf(
        BottomNavItem(NavigationRoutes.A.name, NavigationRoutes.A.label),
        BottomNavItem(NavigationRoutes.B.name, NavigationRoutes.B.label),
        BottomNavItem(NavigationRoutes.C.name,  NavigationRoutes.C.label),
        BottomNavItem(NavigationRoutes.D.name, NavigationRoutes.D.label)
    )
}