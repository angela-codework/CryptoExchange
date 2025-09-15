package com.demo.cryptoexchange.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.demo.core.common.ui.components.CryptoExchangeTheme
import com.demo.cryptoexchange.presentation.navigation.components.AppBottomNavigationBar


@Composable
fun MainScreen(
    isPreview: Boolean = false
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            AppBottomNavigationBar(
                navController = navController
            )
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            isPreview = isPreview,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainNavigationPreview() {
    // 包一層 Theme 或 Surface，讓顏色、樣式正常
    CryptoExchangeTheme(darkTheme = true) {
        Surface {
            MainScreen(isPreview = true)
        }
    }
}