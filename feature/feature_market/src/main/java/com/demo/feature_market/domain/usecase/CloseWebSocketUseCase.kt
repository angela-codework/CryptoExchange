package com.demo.feature_market.domain.usecase

import com.demo.feature_market.domain.repository.MarketRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloseWebSocketUseCase @Inject constructor(
    private val marketRepository: MarketRepository
) {
    operator fun invoke() {
        return marketRepository.closeWebSocket()
    }
}