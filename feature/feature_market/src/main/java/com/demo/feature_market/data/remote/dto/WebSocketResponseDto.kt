package com.demo.feature_market.data.remote.dto

import com.google.gson.annotations.SerializedName

data class WebSocketResponseDto(
    @SerializedName("topic") val topic: String,
    @SerializedName("data") val data: List<CoinIndexDataDto>
)

data class CoinIndexDataDto(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("price") val price: Double,
    @SerializedName("type") val type: Int
)

data class WebSocketSubscriptDto(
    @SerializedName("op") val op: String,
    @SerializedName("args") val args: List<String>
)