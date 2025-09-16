package com.demo.feature_market.domain.usecase

import com.demo.core.common.connectivity.ConnectivityObserver
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A use case that observes the network connectivity status.
 * It abstracts the data layer ([ConnectivityObserver]) from the presentation layer (ViewModel).
 */
@Singleton
class ObserveNetworkStatusUseCase @Inject constructor(
    private val connectivityObserver: ConnectivityObserver
) {
    operator fun invoke(): Flow<ConnectivityObserver.Status> {
        return connectivityObserver.observe()
    }
}
