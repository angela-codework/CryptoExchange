package com.demo.feature_market.domain.usecase

import com.demo.core.common.Resource
import com.demo.feature_market.domain.model.Market
import com.demo.feature_market.domain.repository.MarketRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncMarketsUseCase @Inject constructor(
    private val marketRepository: MarketRepository
) {
    suspend operator fun invoke() : Resource<List<Market>> {
        return marketRepository.getMarkets()
    }
}

