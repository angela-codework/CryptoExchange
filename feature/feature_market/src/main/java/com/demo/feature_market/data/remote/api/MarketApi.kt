package com.demo.feature_market.data.remote.api

import com.demo.feature_market.core.common.Constants.BASE_MARKET_URL
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MarketApi {
    @GET(BASE_MARKET_URL)
    suspend fun getSpotFromMarkets(
        @Query("future") isFuture: Boolean
    ): Response<MarketResponse>
}