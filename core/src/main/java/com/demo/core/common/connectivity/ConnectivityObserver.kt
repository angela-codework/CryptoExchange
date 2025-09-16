package com.demo.core.common.connectivity

import kotlinx.coroutines.flow.Flow

/**
 * An observer for monitoring network connectivity status.
 * This interface abstracts the platform-specific implementation of network monitoring.
 */
interface ConnectivityObserver {

    /**
     * Observes the network connectivity status.
     * @return A [Flow] that emits the current [Status].
     */
    fun observe(): Flow<Status>

    /**
     * Represents the various states of network connectivity.
     */
    enum class Status {
        Available,      // The network is available.
        Unavailable,    // The network is currently unavailable.
    }
}
