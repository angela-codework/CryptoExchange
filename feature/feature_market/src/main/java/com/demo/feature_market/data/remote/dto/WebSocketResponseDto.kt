package com.demo.feature_market.data.remote.dto

import com.google.gson.annotations.SerializedName

data class WebSocketResponseDto(
    @SerializedName("data") val data: Map<String, CoinIndexDataDto> = emptyMap()
)

data class CoinIndexDataDto(
    @SerializedName("id") val id: String,
    @SerializedName("price") val price: Double,
    @SerializedName("type") val type: Int
)

data class WebSocketSubscriptDto(
    @SerializedName("op") val op: String,
    @SerializedName("args") val args: List<String>
)