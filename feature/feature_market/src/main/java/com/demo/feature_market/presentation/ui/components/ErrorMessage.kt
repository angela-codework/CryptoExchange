package com.demo.feature_market.presentation.ui.components

import android.os.Message
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun ErrorMessage(
    message: String?,
    modifier: Modifier = Modifier
) {

    AnimatedVisibility(
        modifier = modifier,
        visible = !message.isNullOrEmpty(),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Text(
            text = message ?: "",
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Normal
        )
    }

}