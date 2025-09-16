package com.demo.cryptoexchange.presentation.navigation.model


sealed class MainRoute(val route: String) {
    object Tabs : MainRoute("tabs")
    object Setting : MainRoute("setting")
}