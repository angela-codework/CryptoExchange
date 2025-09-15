package com.demo.feature_market.domain.repository

import com.demo.core.common.Resource
import com.demo.feature_market.data.remote.api.WebSocketClient
import com.demo.feature_market.domain.model.Market
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MarketRepository {
    suspend fun getMarkets() : Resource<List<Market>>
    fun getMarketsWithRealTimePrice() : Flow<List<Market>>
    fun getRealTimePriceMap(): Flow<Map<String, Double>>

    fun getWsRealTimeConnectionState(): StateFlow<WebSocketClient.ConnectionState>

    fun openWebSocket()
    fun closeWebSocket()
}