package com.demo.feature_market.domain.usecase

import com.demo.feature_market.data.remote.api.WebSocketClient
import com.demo.feature_market.domain.model.Market
import com.demo.feature_market.domain.model.MarketType
import com.demo.feature_market.domain.repository.MarketRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.WebSocket
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetWebSocketConnectStateUseCase @Inject constructor(
    private val marketRepository: MarketRepository
) {
    operator fun invoke() : StateFlow<WebSocketClient.ConnectionState> {
        return marketRepository.getWsRealTimeConnectionState()
    }
}

