package com.demo.cryptoexchange.presentation.navigation

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.demo.core.common.ui.components.CryptoExchangeTheme
import com.demo.cryptoexchange.presentation.navigation.host.MainNavHost

@Composable
fun MainNavigation(
    isPreview: Boolean = false
) {
    // This is the top-level NavController
    val rootNavController = rememberNavController()

    MainNavHost(
        isPreview = isPreview,
        rootNavController = rootNavController
    )
}


@Preview(showBackground = true)
@Composable
fun MainNavigationPreview() {
    CryptoExchangeTheme(darkTheme = true) {
        Surface {
            MainNavigation(isPreview = true)
        }
    }
}