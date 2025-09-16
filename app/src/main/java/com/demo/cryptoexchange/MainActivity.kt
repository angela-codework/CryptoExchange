package com.demo.cryptoexchange

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import com.demo.core.common.ui.components.CryptoExchangeTheme
import com.demo.cryptoexchange.presentation.navigation.MainNavigation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            CryptoExchangeTheme(darkTheme = true) {
                Surface {
                    MainNavigation()
                }
            }
        }

    }


}