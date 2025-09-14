package com.demo.feature_market.domain.model

data class MarketUiState(
    val marketType: MarketType = MarketType.SPOT,
    val markets: List<Market> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)