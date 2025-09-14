package com.demo.feature_market.domain.usecase

import com.demo.core.common.Resource
import com.demo.feature_market.domain.model.Market
import com.demo.feature_market.domain.model.MarketType
import com.demo.feature_market.domain.repository.MarketRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetMarketsByTypeUseCase @Inject constructor(
    private val marketRepository: MarketRepository
) {
    suspend operator fun invoke(type: MarketType) : Resource<List<Market>> {
        return when(type) {
            MarketType.SPOT -> marketRepository.getSpotMarkets()
            MarketType.FUTURE -> marketRepository.getFutureMarkets()
        }
    }
}

