package com.demo.feature_market.domain.model

data class Market(
    val symbol: String,
    val future: String,
    val price: Double = 0.0
)
