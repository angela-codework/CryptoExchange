package com.demo.core.common.connectivity

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

/**
 * A concrete implementation of [ConnectivityObserver] that uses Android's [ConnectivityManager].
 */
class NetworkConnectivityObserver @Inject constructor(
    @ApplicationContext private val context: Context
) : ConnectivityObserver {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @SuppressLint("MissingPermission")
    override fun observe(): Flow<ConnectivityObserver.Status> {
        return callbackFlow {
            // Define the callback that will receive network status updates.
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    // When network becomes available, send the Available status.
                    connectivityManager.getNetworkCapabilities(network)?.let {
                        if (it.hasCapability(NET_CAPABILITY_INTERNET)) {
                            trySend(ConnectivityObserver.Status.Available)
                        }
                    }
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    // When network is lost, send the Unavailable status.
                    trySend(ConnectivityObserver.Status.Unavailable)
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    // When network is unavailable, send the Unavailable status.
                    trySend(ConnectivityObserver.Status.Unavailable)
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    capabilities: NetworkCapabilities
                ) {
                    if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                        trySend(ConnectivityObserver.Status.Available)
                    } else {
                        trySend(ConnectivityObserver.Status.Unavailable)
                    }
                }
            }

            // Emit the current status right away on collection
            val currentNetwork = connectivityManager.activeNetwork
            if (currentNetwork == null) {
                trySend(ConnectivityObserver.Status.Unavailable)
            } else {
                trySend(ConnectivityObserver.Status.Available)
            }

            // Register the callback with the system.
            @SuppressLint("MissingPermission")
            connectivityManager.registerDefaultNetworkCallback(callback)

            // `awaitClose` is a suspend function that runs when the Flow's collector is cancelled.
            // This is the perfect place to unregister the callback to avoid memory leaks.
            awaitClose {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }.distinctUntilChanged() // Only emit when the status actually changes.
    }
}
