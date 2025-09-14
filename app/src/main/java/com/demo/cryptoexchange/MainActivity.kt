package com.demo.cryptoexchange

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import com.demo.cryptoexchange.presentation.navigation.MainScreen
import com.demo.cryptoexchange.presentation.navigation.theme.CryptoExchangeTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            CryptoExchangeTheme(darkTheme = true) {
                Surface {
                    MainScreen()
                }
            }
        }

    }


}