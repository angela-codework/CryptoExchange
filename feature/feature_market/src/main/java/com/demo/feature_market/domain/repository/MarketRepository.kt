package com.demo.feature_market.domain.repository

import com.demo.core.common.Resource
import com.demo.feature_market.domain.model.Market
import com.demo.feature_market.domain.model.MarketType

interface MarketRepository {
    suspend fun getInitialMarketList(type: MarketType): Resource<List<Market>>

}