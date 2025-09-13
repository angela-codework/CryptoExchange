package com.demo.feature_market.data.repository

import com.demo.core.common.Resource
import com.demo.core.util.logger.AppLogger
import com.demo.feature_market.data.remote.api.MarketApi
import com.demo.feature_market.data.remote.api.MarketsDtoWithTime
import com.demo.feature_market.data.remote.api.toMarkets
import com.demo.feature_market.data.remote.api.toMarketsDtoWithTime
import com.demo.feature_market.domain.model.Market
import com.demo.feature_market.domain.model.MarketType
import com.demo.feature_market.domain.repository.MarketRepository

class MarketRepositoryImpl(
    private val marketApi: MarketApi
) : MarketRepository {

    companion object {
        val TAG = MarketRepositoryImpl::class.simpleName
        const val CACHE_TIME = 30_000L // 30 seconds cache
    }

    private var lastFetchServerTime = mutableMapOf(
        MarketType.SPOT to 0L,
        MarketType.FUTURE to 0L
    )
    private var cachedMarketMap = mutableMapOf<MarketType, List<Market>>()


    // get valid cache by MarketType
    private fun getValidInitialMarketListFromCache(type: MarketType) : List<Market>? {
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

    // get initial markets by cache or api wrap in Resource by MarketType
    override suspend fun getInitialMarketList(type: MarketType) : Resource<List<Market>> {
        AppLogger.d(TAG, "getInitialMarketList of $type")
        val cache = getValidInitialMarketListFromCache(type)
        if(!cache.isNullOrEmpty()) {
            AppLogger.d(TAG, "Use cached market list.")
            return Resource.Success(cache)
        }

        AppLogger.d(TAG, "cache is expired or null. Fetch from server.")


        return when(val result = getInitialMarketResponse(type)) {
            is Resource.Fail<MarketsDtoWithTime> -> {
               Resource.Fail(result.msg)
            }
            is Resource.Success<MarketsDtoWithTime> -> {
                val markets = result.data.toMarkets()
                val serverTime = result.data.serverTime
                saveToCache(type, markets, serverTime)

                Resource.Success(markets)
            }
        }
    }

    // fetch Markets and server time wrap in Resource by MarketType
    suspend fun getInitialMarketResponse(type: MarketType) : Resource<MarketsDtoWithTime> {
        val result = marketApi.getSpotFromMarkets(type == MarketType.FUTURE)

        return if(result.isSuccessful) {
            val response = result.body()
            if(response != null) {
                Resource.Success(response.toMarketsDtoWithTime())
            } else {
                Resource.Fail("Failed to fetch market list with type ${type.name} : body is null")
            }
        } else {
            Resource.Fail("Failed to fetch market list with type ${type.name} : ${result.message()}")
        }
    }
}