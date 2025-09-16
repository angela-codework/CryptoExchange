package com.demo.cryptoexchange.presentation.navigation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.demo.core.common.ui.components.CryptoExchangeTheme

@Composable
fun DScreen(
    screenName: String,
    modifier: Modifier = Modifier,
    onSettingClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Text(
            text = screenName,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.Center)
        )

        IconButton(
            onClick = onSettingClick,
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings"
            )
        }
    }
}

@Preview
@Composable
fun PreviewDScreen() {
    CryptoExchangeTheme {
        DScreen("D", Modifier.fillMaxSize(), {})
    }
}
