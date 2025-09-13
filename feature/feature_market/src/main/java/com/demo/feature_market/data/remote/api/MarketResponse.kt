package com.demo.feature_market.data.remote.api

import com.demo.feature_market.data.remote.dto.MarketDto
import com.demo.feature_market.data.remote.dto.toMarket
import com.demo.feature_market.domain.model.Market
import com.google.gson.annotations.SerializedName

data class MarketResponse(
    @SerializedName("msg") val msg: String,
    @SerializedName("time") val serverTime: Long,
    @SerializedName("data") val marketList: List<MarketDto>
)

data class MarketsDtoWithTime(
    val data: List<MarketDto>,
    val serverTime: Long
)

fun MarketResponse.toMarketsDtoWithTime() : MarketsDtoWithTime {
    return MarketsDtoWithTime(
        data = marketList,
        serverTime = serverTime
    )
}

fun MarketsDtoWithTime.toMarkets(): List<Market> {
    return data.map { it.toMarket() }
}

