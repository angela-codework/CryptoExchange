/**
 * @file MarketRepositoryImpl.kt
 * @author angela.wang
 *
 * Implementation of the [MarketRepository] interface.
 *
 * This file contains the concrete implementation of the repository layer for the market feature.
 * It serves as the Single Source of Truth (SSoT) for all market-related data, abstracting
 * away the complexities of data fetching, caching, and real-time updates from the rest of
 * the application.
 *
 * ## Core Design:
 * The repository follows a reactive programming paradigm using Kotlin Flows, providing
 * resilient and continuous data streams to the domain layer (UseCases). It orchestrates
 * data from a REST API, a WebSocket, and an in-memory cache.
 *
 * @see MarketRepository for the interface definition.
 * @see MarketViewModel for the consumer of the data provided by this repository (via UseCases).
 */
package com.demo.feature_market.data.repository

import com.demo.core.common.Resource
import com.demo.core.util.logger.AppLogger
import com.demo.core.util.logger.AppLogger.tag
import com.demo.feature_market.data.remote.api.MarketApi
import com.demo.feature_market.data.remote.api.WebSocketClient
import com.demo.feature_market.data.remote.dto.MarketsWithTimeDto
import com.demo.feature_market.data.remote.dto.toMarkets
import com.demo.feature_market.data.remote.dto.toMarketsWithTimeDto
import com.demo.feature_market.domain.model.Market
import com.demo.feature_market.domain.model.MarketType
import com.demo.feature_market.domain.repository.MarketRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementation of the [MarketRepository] interface.
 *
 * This class acts as the Single Source of Truth (SSoT) for all market-related data,
 * orchestrating data from a remote REST API, a real-time WebSocket, and an in-memory cache.
 * It uses a reactive approach with Kotlin Flows to provide continuous data streams to the
 * domain layer.
 *
 * @param marketApi The Retrofit service for fetching market data via REST.
 * @param webSocket The client for receiving real-time price updates.
 */
class MarketRepositoryImpl(
    private val marketApi: MarketApi,
    private val webSocket: WebSocketClient
) : MarketRepository {

    companion object {
        val TAG = tag<MarketRepositoryImpl>()
        const val CACHE_TIME = 30_000L // 30 seconds cache
        const val PRICE_TYPE_FILTER = 1
    }

    private val lastFetchServerTime = ConcurrentHashMap<MarketType, Long>().apply {
        put(MarketType.SPOT, 0L)
        put(MarketType.FUTURE, 0L)
    }

    private var cachedMarketMap = ConcurrentHashMap<MarketType, List<Market>>()

    private val _marketSpotStateFlow = MutableStateFlow<List<Market>>(emptyList())
    private val _marketFutureStateFlow = MutableStateFlow<List<Market>>(emptyList())

    override fun getWsRealTimeConnectionState(): StateFlow<WebSocketClient.ConnectionState> {
        return webSocket.connectionState
    }

    override fun openWebSocket() {
        AppLogger.d(TAG, "Opening WebSocket connection...")
        webSocket.connect()
    }

    override fun closeWebSocket() {
        AppLogger.d(TAG, "Closing WebSocket connection...")
        webSocket.disconnect()
    }

    // get valid market list cache by MarketType
    private fun getValidMarketListFromCache(type: MarketType) : List<Market>? {
        val currentTime = System.currentTimeMillis()

        return cachedMarketMap[type]?.let { cache ->
            val isExpired = currentTime - (lastFetchServerTime[type]?: 0L) > CACHE_TIME
            if(isExpired) {
                null
            } else {
                cache
            }
        }
    }

    // save market list to cache and update last fetch server time by MarketType
    private fun saveToCache(type: MarketType, markets: List<Market>, dataTime: Long) {
        AppLogger.d(TAG, "saveToCache: [type $type, market size ${markets.size}, server time $dataTime]")
        cachedMarketMap[type] = markets
        lastFetchServerTime[type] = dataTime
    }

    override suspend fun getSpotMarkets() : Resource<List<Market>> {
        return getMarketsByType(MarketType.SPOT)
    }

    override suspend fun getFutureMarkets() : Resource<List<Market>> {
        return getMarketsByType(MarketType.FUTURE)
    }

    // Get markets by cache or api wrap in Resource by MarketType
    private suspend fun getMarketsByType(type: MarketType) : Resource<List<Market>> {
        AppLogger.d(TAG, "getMarketList of type $type")
        val cache = getValidMarketListFromCache(type)
        if(!cache.isNullOrEmpty()) {
            AppLogger.d(TAG, "Use cached market list.")
            return Resource.Success(cache)
        }

        AppLogger.d(TAG, "cache is expired or null. Fetch from server.")


        return when(val result = getMarketResponseByType(type)) {
            is Resource.Fail<MarketsWithTimeDto> -> {
                getMarketStateFlowFromType(type).value = emptyList()
                Resource.Fail(result.msg)
            }
            is Resource.Success<MarketsWithTimeDto> -> {
                val markets = result.data.toMarkets()
                val serverTime = result.data.serverTime
                saveToCache(type, markets, serverTime)
                getMarketStateFlowFromType(type).value = markets

                Resource.Success(markets)
            }
        }
    }

    // Fetch MarketsDto and server time wrap in Resource by MarketType
    private suspend fun getMarketResponseByType(type: MarketType) : Resource<MarketsWithTimeDto> =
        withContext(Dispatchers.IO) {
            try {
                val result = marketApi.getSpotFromMarkets(type == MarketType.FUTURE)

                if(result.isSuccessful) {
                    val response = result.body()
                    if(response != null) {
                        Resource.Success(response.toMarketsWithTimeDto())
                    } else {
                        Resource.Fail("Failed to fetch market list with type ${type.name} : body is null")
                    }
                } else {
                    Resource.Fail("Failed to fetch market list with type ${type.name} : ${result.message()}")
                }
            } catch (e: Exception) {
                Resource.Fail("Failed to fetch market list with type ${type.name} : ${e.message}")
            }
        }

    // Return Map of (symbol, price) as flow
    override fun getRealTimePriceMap(): Flow<Map<String, Double>> {
        return webSocket.responseUpdate
            .onEach { wsResponse ->
                AppLogger.d(TAG, "Receive webSocket response with item size ${wsResponse.data.size}")
            }
            .map { wsResponse ->
                try {
                    wsResponse.data
                        .filter { it.type == PRICE_TYPE_FILTER }
                        .associate { it.symbol to it.price }
                } catch (e: Exception) {
                    AppLogger.w(TAG, "WebSocket mapping failed: ${e.message}")
                    emptyMap()
                }
            }
    }

    override fun getSpotMarketsWithRealTimePrice() : Flow<List<Market>> {
        return getMarketsWithRealTimePriceByType(MarketType.SPOT)
    }

    override fun getFutureMarketsWithRealTimePrice() : Flow<List<Market>> {
        return getMarketsWithRealTimePriceByType(MarketType.FUTURE)
    }

    // get updated market list with real-time price
    private fun getMarketsWithRealTimePriceByType(type: MarketType) : Flow<List<Market>> {
        return combine(getMarketStateFlowFromType(type), getRealTimePriceMap()) { markets, priceMap ->
            if (markets.isEmpty()) {
                AppLogger.d(TAG, "market list of type ${type.name} is empty.")
                emptyList()
            } else {
                val marketsWithPrice = markets.map {  market ->
                    market.copy(
                        price = priceMap[market.symbol]?: market.price
                    )
                }.sortedBy { it.symbol }

                marketsWithPrice
            }
        }
    }

    private fun getMarketStateFlowFromType(type: MarketType) : MutableStateFlow<List<Market>> {
        return when(type) {
            MarketType.SPOT -> _marketSpotStateFlow
            MarketType.FUTURE -> _marketFutureStateFlow
        }
    }
}