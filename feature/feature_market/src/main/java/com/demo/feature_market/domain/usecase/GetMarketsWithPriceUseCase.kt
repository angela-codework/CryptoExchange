package com.demo.feature_market.domain.usecase

import com.demo.feature_market.domain.model.Market
import com.demo.feature_market.domain.model.MarketType
import com.demo.feature_market.domain.repository.MarketRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetMarketsWithPriceUseCase @Inject constructor(
    private val marketRepository: MarketRepository
) {
    operator fun invoke() : Flow<List<Market>> {
        return marketRepository.getMarketsWithRealTimePrice()
    }
}

