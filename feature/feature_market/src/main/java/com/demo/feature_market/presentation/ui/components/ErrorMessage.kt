package com.demo.feature_market.presentation.ui.components

import android.os.Message
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ErrorMessage(
    message: String?,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit
) {

    AnimatedVisibility(
        modifier = modifier,
        visible = !message.isNullOrEmpty(),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message ?: "",
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Normal
            )

            Button(
                onClick = onRetry,
                modifier = modifier
                    .padding(top = 10.dp)
            ) {
                Text(
                    text = "Retry",
                    color = MaterialTheme.colorScheme.background,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Normal
                )
            }

        }

    }

}