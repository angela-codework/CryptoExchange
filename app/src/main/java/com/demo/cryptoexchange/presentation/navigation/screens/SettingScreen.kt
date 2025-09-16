package com.demo.cryptoexchange.presentation.navigation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.demo.core.common.ui.components.CryptoExchangeTheme

@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.TopStart
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "back"
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewSettingScreen() {
    CryptoExchangeTheme {
        SettingScreen(onBackClick = {})
    }
}