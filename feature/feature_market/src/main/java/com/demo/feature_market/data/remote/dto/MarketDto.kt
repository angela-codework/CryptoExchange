package com.demo.feature_market.data.remote.dto

import com.demo.feature_market.domain.model.Market
import com.google.gson.annotations.SerializedName

data class MarketDto(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("future")  val isFuture: Boolean
)

fun MarketDto.toMarket() : Market {
    return Market(
        symbol = symbol,
        isFuture = isFuture
    )
}