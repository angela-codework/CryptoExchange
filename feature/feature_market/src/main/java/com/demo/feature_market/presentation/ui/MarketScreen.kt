package com.demo.feature_market.presentation.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.demo.core.common.ui.components.CryptoExchangeTheme
import com.demo.feature_market.domain.model.Market
import com.demo.feature_market.domain.model.MarketType
import com.demo.feature_market.domain.model.MarketUiState
import com.demo.feature_market.presentation.ui.components.ErrorMessage
import com.demo.feature_market.presentation.ui.components.MarketItem
import com.demo.feature_market.presentation.ui.components.MarketTabRow
import com.google.gson.annotations.Until

@Composable
fun MarketScreen(
    viewModel: MarketViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(MarketUiState(isLoading = true))

    MarketScreenContent(
        uiState = uiState,
        onTypeSelected = { type -> viewModel.onSelectMarketTypeChange(type) },
        onRetry = { viewModel.onRetry() }
    )
}

@Composable
fun MarketScreenContent(
    uiState: MarketUiState,
    onTypeSelected: (MarketType) -> Unit,
    onRetry: () -> Unit
) {
    val listState = rememberLazyListState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        // Market Type Tabs
        MarketTabRow(
            selectedType = uiState.marketType,
            onTypeSelected = onTypeSelected,
            modifier = Modifier.fillMaxWidth()
        )

        // Content Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.error != null -> {
                    ErrorMessage(
                        message = uiState.error,
                        modifier = Modifier.align(Alignment.Center),
                        onRetry = onRetry
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        // Market items
                        items(
                            items = uiState.markets,
                            key = { market -> "${market.symbol}-${market.isFuture}" }
                        ) { market ->
                            MarketItem(
                                market = market,
                                modifier = Modifier.animateItem()
                            )
                        }

                        // Bottom spacing
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MarketFutureScreenPreview() {
    CryptoExchangeTheme {
        MarketScreenContent(
            uiState = MarketUiState(
                marketType = MarketType.FUTURE,
                markets = listOf(
                    Market(
                        symbol = "FUTURE_1",
                        isFuture = true,
                        price = 32.8
                    ),
                    Market(
                        symbol = "FUTURE_2",
                        isFuture = true,
                        price = 32.9
                    ),
                    Market(
                        symbol = "FUTURE_3",
                        isFuture = true,
                        price = 37.9
                    )
                )
            ),
            onTypeSelected = { },
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MarketSpotScreenPreview() {
    CryptoExchangeTheme {
        MarketScreenContent(
            uiState = MarketUiState(
                marketType = MarketType.SPOT,
                markets = listOf(
                    Market(
                        symbol = "SPOT_1",
                        isFuture = false,
                        price = 32.8
                    ),
                    Market(
                        symbol = "SPOT_2",
                        isFuture = false,
                        price = 32.9
                    ),
                    Market(
                        symbol = "SPOT_3",
                        isFuture = false,
                        price = 37.9
                    )
                )
            ),
            onTypeSelected = { },
            onRetry = {}
        )
    }
}

@Preview(name = "Loading State", showBackground = true)
@Composable
fun MarketScreenLoadingPreview() {
    CryptoExchangeTheme {
        MarketScreenContent(
            uiState = MarketUiState(isLoading = true),
            onTypeSelected = { },
            onRetry = {}
        )
    }
}

@Preview(
    name = "Loading State Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MarketScreenLoadingDarkPreview() {
    CryptoExchangeTheme {
        MarketScreenContent(
            uiState = MarketUiState(isLoading = true),
            onTypeSelected = { },
            onRetry = {}
        )
    }
}

@Preview(name = "Error State", showBackground = true)
@Composable
fun MarketScreenErrorPreview() {
    CryptoExchangeTheme {
        MarketScreenContent(
            uiState = MarketUiState(
                isLoading = false,
                error = "網路連接失敗"
            ),
            onTypeSelected = { },
            onRetry = {}
        )
    }
}

@Preview(
    name = "Error State Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MarketScreenErrorDarkPreview() {
    CryptoExchangeTheme {
        MarketScreenContent(
            uiState = MarketUiState(
                isLoading = false,
                error = "網路連接失敗"
            ),
            onTypeSelected = { },
            onRetry = {}
        )
    }
}

@Preview(name = "Dark Theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MarketFutureScreenDarkPreview() {
    CryptoExchangeTheme {
        MarketScreenContent(
            uiState = MarketUiState(
                marketType = MarketType.FUTURE,
                markets = listOf(
                    Market(
                        symbol = "FUTURE_1",
                        isFuture = true,
                        price = 32.8
                    ),
                    Market(
                        symbol = "FUTURE_2",
                        isFuture = true,
                        price = 32.9
                    ),
                    Market(
                        symbol = "FUTURE_3",
                        isFuture = true,
                        price = 37.9
                    )
                )
            ),
            onTypeSelected = { },
            onRetry = {}
        )
    }
}

@Preview(name = "Dark Theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MarketSpotScreenDarkPreview() {
    CryptoExchangeTheme {
        MarketScreenContent(
            uiState = MarketUiState(
                marketType = MarketType.SPOT,
                markets = listOf(
                    Market(
                        symbol = "SPOT_1",
                        isFuture = false,
                        price = 32.8
                    ),
                    Market(
                        symbol = "SPOT_2",
                        isFuture = false,
                        price = 32.9
                    ),
                    Market(
                        symbol = "SPOT_3",
                        isFuture = false,
                        price = 37.9
                    )
                )
            ),
            onTypeSelected = { },
            onRetry = {}
        )
    }
}