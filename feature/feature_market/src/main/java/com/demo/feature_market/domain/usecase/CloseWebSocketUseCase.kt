package com.demo.feature_market.domain.usecase

import com.demo.feature_market.domain.repository.MarketRepository
import com.demo.logger.AppLogger
import com.demo.logger.AppLogger.tag
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloseWebSocketUseCase @Inject constructor(
    private val marketRepository: MarketRepository
) {

    companion object {
        val TAG = tag<CloseWebSocketUseCase>()
    }

    operator fun invoke() {
        AppLogger.d(TAG, "CloseWebSocketUseCase")
        marketRepository.closeWebSocket()
    }
}