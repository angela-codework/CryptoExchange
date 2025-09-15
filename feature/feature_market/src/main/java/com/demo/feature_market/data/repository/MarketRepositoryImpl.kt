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
import com.demo.feature_market.data.remote.api.MarketApi
import com.demo.feature_market.data.remote.api.WebSocketClient
import com.demo.feature_market.data.remote.dto.MarketsWithTimeDto
import com.demo.feature_market.data.remote.dto.toMarkets
import com.demo.feature_market.data.remote.dto.toMarketsWithTimeDto
import com.demo.feature_market.domain.model.Market
import com.demo.feature_market.domain.model.MarketType
import com.demo.feature_market.domain.repository.MarketRepository
import com.demo.logger.AppLogger
import com.demo.logger.AppLogger.tag
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

    private var lastFetchServerTime = 0L

    private var cachedMarket: List<Market> = emptyList()

    private val _marketsStateFlow = MutableStateFlow<List<Market>>(emptyList())

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

    // get valid market list cache
    private fun getValidMarketListFromCache() : List<Market>? {
        val currentTime = System.currentTimeMillis()

        return cachedMarket.let { cache ->
            val isExpired = currentTime - lastFetchServerTime > CACHE_TIME
            if(isExpired) {
                null
            } else {
                cache
            }
        }
    }

    // save market list to cache and update last fetch server time
    private fun saveToCache(markets: List<Market>, dataTime: Long) {
        AppLogger.d(TAG, "saveToCache: [market size ${markets.size}, server time $dataTime]")
        cachedMarket = markets
        lastFetchServerTime = dataTime
    }

    // Get markets by cache or api wrap in Resource
    override suspend fun getMarkets() : Resource<List<Market>> {
        AppLogger.d(TAG, "getMarketList")
        val cache = getValidMarketListFromCache()
        if(!cache.isNullOrEmpty()) {
            AppLogger.d(TAG, "Use cached market list.")
            return Resource.Success(cache)
        }

        AppLogger.d(TAG, "cache is expired or null. Fetch from server.")


        return when(val result = getMarketResponse()) {
            is Resource.Fail<MarketsWithTimeDto> -> {
                _marketsStateFlow.value = emptyList()
                Resource.Fail(result.msg)
            }
            is Resource.Success<MarketsWithTimeDto> -> {
                val markets = result.data.toMarkets()
                val serverTime = result.data.serverTime
                AppLogger.d(TAG, "getMarketResponseByType success: ${markets.joinToString("\n") }}")
                saveToCache(markets, serverTime)
                _marketsStateFlow.value = markets

                Resource.Success(markets)
            }
        }
    }

    // Fetch MarketsDto and server time wrap in Resource
    private suspend fun getMarketResponse() : Resource<MarketsWithTimeDto> =
        withContext(Dispatchers.IO) {
            try {
                val result = marketApi.getMarkets()

                if(result.isSuccessful) {
                    val response = result.body()
                    if(response != null) {
                        Resource.Success(response.toMarketsWithTimeDto())
                    } else {
                        Resource.Fail("Failed to fetch market list : body is null")
                    }
                } else {
                    Resource.Fail("Failed to fetch market list : ${result.message()}")
                }
            } catch (e: Exception) {
                Resource.Fail("Failed to fetch market list : ${e.message}")
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
                    // wsResponse.data is now a Map, e.g., Map<String, WsData>
                    wsResponse.data.values
                        .filter { coinData ->
                            // Filter the map entries. The value of the entry contains the type.
                            // entry.key is the symbol, entry.value is the object with price/type
                            coinData.type == PRICE_TYPE_FILTER
                        }
                        .associate { coinData ->
                            // 3. Create a new map. For each object, use its 'id' as the new key and its 'price' as the new value.
                            coinData.id to coinData.price
                        }
                } catch (e: Exception) {
                    AppLogger.w(TAG, "WebSocket mapping failed: ${e.message}")
                    emptyMap()
                }
            }
    }

    // get updated market list with real-time price
    override fun getMarketsWithRealTimePrice() : Flow<List<Market>> {
        return combine(_marketsStateFlow, getRealTimePriceMap()) { markets, priceMap ->
            if (markets.isEmpty()) {
                AppLogger.d(TAG, "market list is empty.")
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
}