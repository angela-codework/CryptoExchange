package com.demo.feature_market.domain.usecase

import com.demo.feature_market.domain.model.Market
import com.demo.feature_market.domain.repository.MarketRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetSpotListUseCase @Inject constructor(
    private val marketRepository: MarketRepository
) {
    operator fun invoke() : List<Market> {
        //fixme
        return emptyList()
    }
}

