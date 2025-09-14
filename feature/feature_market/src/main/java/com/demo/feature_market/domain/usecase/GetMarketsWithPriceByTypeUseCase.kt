package com.demo.feature_market.domain.usecase

import com.demo.feature_market.domain.model.Market
import com.demo.feature_market.domain.model.MarketType
import com.demo.feature_market.domain.repository.MarketRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetMarketsWithPriceByTypeUseCase @Inject constructor(
    private val marketRepository: MarketRepository
) {
    operator fun invoke(type: MarketType) : Flow<List<Market>> {
        return when(type) {
            MarketType.SPOT -> marketRepository.getSpotMarketsWithRealTimePrice()
            MarketType.FUTURE -> marketRepository.getFutureMarketsWithRealTimePrice()
        }
    }
}

