package com.demo.feature_market.domain.usecase

import com.demo.feature_market.domain.repository.MarketRepository
import com.demo.logger.AppLogger
import com.demo.logger.AppLogger.tag
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenWebSocketUseCase @Inject constructor(
    private val marketRepository: MarketRepository
) {

    companion object {
        val TAG = tag<OpenWebSocketUseCase>()
    }

    operator fun invoke() {
        AppLogger.d(TAG, "OpenWebSocketUseCase")
        marketRepository.openWebSocket()
    }
}