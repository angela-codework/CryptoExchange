package com.demo.feature_market.domain.repository

import com.demo.core.common.Resource
import com.demo.feature_market.data.remote.api.WebSocketClient
import com.demo.feature_market.domain.model.Market
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MarketRepository {
    suspend fun getSpotMarkets() : Resource<List<Market>>
    suspend fun getFutureMarkets() : Resource<List<Market>>

    fun getSpotMarketsWithRealTimePrice() : Flow<List<Market>>
    fun getFutureMarketsWithRealTimePrice() : Flow<List<Market>>
    fun getRealTimePriceMap(): Flow<Map<String, Double>>

    fun getWsRealTimeConnectionState(): StateFlow<WebSocketClient.ConnectionState>

    fun openWebSocket()
    fun closeWebSocket()
}