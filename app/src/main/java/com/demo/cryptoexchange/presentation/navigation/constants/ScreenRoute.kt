package com.demo.cryptoexchange.presentation.navigation.constants

sealed class ScreenRoute(val name: String, val label: String) {
    object Market : ScreenRoute("A", "A")
    object B : ScreenRoute("B", "B")
    object C : ScreenRoute("C", "C")
    object D : ScreenRoute("D", "D")
}