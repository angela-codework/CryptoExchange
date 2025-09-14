package com.demo.feature_market.domain.model

data class Market(
    val symbol: String,
    val isFuture: Boolean,
    val price: Double = 0.0
)
