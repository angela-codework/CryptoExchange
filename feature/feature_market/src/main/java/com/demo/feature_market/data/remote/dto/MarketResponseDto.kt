package com.demo.feature_market.data.remote.dto

import com.demo.feature_market.domain.model.Market
import com.google.gson.annotations.SerializedName

data class MarketResponseDto(
    @SerializedName("msg") val msg: String,
    @SerializedName("time") val serverTime: Long,
    @SerializedName("data") val marketList: List<MarketDto>
)

data class MarketsWithTimeDto(
    val data: List<MarketDto>,
    val serverTime: Long
)

fun MarketResponseDto.toMarketsWithTimeDto() : MarketsWithTimeDto {
    return MarketsWithTimeDto(
        data = marketList,
        serverTime = serverTime
    )
}

fun MarketsWithTimeDto.toMarkets(): List<Market> {
    return data.map { it.toMarket() }
}

