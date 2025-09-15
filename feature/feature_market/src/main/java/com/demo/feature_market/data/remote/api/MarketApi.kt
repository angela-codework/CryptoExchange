package com.demo.feature_market.data.remote.api

import com.demo.feature_market.core.common.Constants.SERVER_MARKET_URL
import com.demo.feature_market.data.remote.dto.MarketResponseDto
import retrofit2.Response
import retrofit2.http.GET

interface MarketApi {
    @GET(SERVER_MARKET_URL)
    suspend fun getMarkets(): Response<MarketResponseDto>
}