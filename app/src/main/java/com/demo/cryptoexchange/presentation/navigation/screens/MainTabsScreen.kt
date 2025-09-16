package com.demo.cryptoexchange.presentation.navigation.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.demo.cryptoexchange.presentation.navigation.host.TabsNavHost
import com.demo.cryptoexchange.presentation.navigation.components.AppBottomNavigationBar

@Composable
fun MainTabsScreen(
    isPreview: Boolean,
    onOpenSetting: () -> Unit
) {
    // This is the inner NavController for the bottom bar tabs
    val tabsNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            AppBottomNavigationBar(
                navController = tabsNavController
            )
        }
    ) { innerPadding ->
        TabsNavHost(
            modifier = Modifier.padding(innerPadding),
            navController = tabsNavController,
            isPreview = isPreview,
            onOpenSetting = onOpenSetting
        )
    }
}