package com.demo.feature_market.presentation.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.demo.feature_market.domain.model.Market
import kotlinx.coroutines.delay
import java.text.DecimalFormat

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MarketItem(
    market: Market,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val priceFormatter = remember { DecimalFormat("#,##0.00####") }
    var previousPrice by remember { mutableDoubleStateOf(market.price) }

    val priceChangeColor = remember(market.price, previousPrice) {
        when {
            market.price > previousPrice -> Color(0xFF4CAF50) // Green
            market.price < previousPrice -> Color(0xFFF44336) // Red
            else -> Color.Transparent
        }
    }

    // Update previous price after animation
    LaunchedEffect(market.price) {
        if (market.price != 0.0) {
            delay(500) // Wait for animation
            previousPrice = market.price
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (priceChangeColor != Color.Transparent) {
                priceChangeColor.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Symbol with future indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = market.symbol,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (market.isFuture) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "F",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Price with trend indicator
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Trend indicator
                if (priceChangeColor != Color.Transparent) {
                    Icon(
                        imageVector = if (market.price > previousPrice) {
                            Icons.Default.KeyboardArrowUp
                        } else {
                            Icons.Default.KeyboardArrowDown
                        },
                        contentDescription = null,
                        tint = priceChangeColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                AnimatedContent(
                    targetState = market.price,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInVertically { it } + fadeIn() with
                                    slideOutVertically { -it } + fadeOut()
                        } else {
                            slideInVertically { -it } + fadeIn() with
                                    slideOutVertically { it } + fadeOut()
                        }
                    },
                    label = "price_animation"
                ) { price ->
                    Text(
                        text = if (price > 0) {
                            "$${priceFormatter.format(price)}"
                        } else {
                            "--"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = when {
                            price > previousPrice -> Color(0xFF4CAF50)
                            price < previousPrice -> Color(0xFFF44336)
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        fontWeight = if (priceChangeColor != Color.Transparent) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Normal
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewMarketItem() {
    MarketItem(
        market = Market(
            symbol = "SPOT_1",
            isFuture = false,
            price = 30.4
        ),
        onClick = {}
    )
}