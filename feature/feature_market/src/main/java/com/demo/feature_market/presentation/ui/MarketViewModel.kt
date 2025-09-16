/**
 * @file MarketViewModel.kt
 * @author angela.wang
 *
 * ViewModel for the Market screen, responsible for preparing and managing all data for the UI.
 *
 * This ViewModel follows the principles of a reactive, state-driven architecture using Kotlin Flows.
 * Its primary responsibilities include:
 *
 * 1.  **Orchestrating UseCases:** It coordinates various use cases to fetch, sync, and observe market data.
 *
 * 2.  **Managing UI State:** It constructs and exposes a single `uiState` as a [StateFlow]. This `UiState`
 *     is the single source of truth for the UI and contains all necessary information, including
 *     loading status, error messages, and the list of markets.
 *
 * 3.  **Handling User Actions:** It provides public methods like `onSelectMarketTypeChange` and `onRetry`
 *     that the UI can call to signal user intent.
 *
 * ## Core Design:
 * The `uiState` is built using a sophisticated reactive stream that combines multiple triggers and data sources:
 * - **`actionTrigger`:** A flow that combines user selections (`_selectedType`) and manual retry events (`_retryTrigger`)
 *   to drive the main data logic.
 * - **`flatMapLatest`:** Used to create a state machine for each action. This machine handles the full
 *   lifecycle: emitting a `Loading` state, performing a sync, handling sync failure with an `Error` state,
 *   and finally, collecting the continuous data stream on success.
 * - **`combine`:** Used to merge the main logic's state with other global states, such as the real-time
 *   WebSocket connection status, to produce the final, definitive `UiState`.
 *
 * This design ensures that the UI is always a simple reflection of the state and that complex logic
 * is handled reactively and robustly within the ViewModel.
 *
 * @see MarketUiState for the definition of the UI state.
 * @see com.demo.feature_market.domain.repository.MarketRepository for the underlying data provider.
 */
package com.demo.feature_market.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.core.common.Resource
import com.demo.core.common.connectivity.ConnectivityObserver
import com.demo.feature_market.data.remote.api.WebSocketClient
import com.demo.feature_market.domain.model.MarketType
import com.demo.feature_market.domain.model.MarketUiState
import com.demo.feature_market.domain.usecase.CloseWebSocketUseCase
import com.demo.feature_market.domain.usecase.GetMarketsWithPriceUseCase
import com.demo.feature_market.domain.usecase.GetWebSocketConnectStateUseCase
import com.demo.feature_market.domain.usecase.ObserveNetworkStatusUseCase
import com.demo.feature_market.domain.usecase.OpenWebSocketUseCase
import com.demo.feature_market.domain.usecase.SyncMarketsUseCase
import com.demo.logger.AppLogger
import com.demo.logger.AppLogger.tag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Market screen.
 * This ViewModel is responsible for orchestrating data flows from use cases,
 * handling user actions, and providing a single source of truth (UiState) for the UI.
 */
@HiltViewModel
class MarketViewModel @Inject constructor(
    private val syncMarketsUseCase: SyncMarketsUseCase,
    private val getMarketsWithPriceUseCase: GetMarketsWithPriceUseCase,
    private val getWebSocketConnectStateUseCase: GetWebSocketConnectStateUseCase,
    private val openWebSocketUseCase: OpenWebSocketUseCase,
    private val closeWebSocketUseCase: CloseWebSocketUseCase,
    private val observeNetworkStatusUseCase: ObserveNetworkStatusUseCase // Inject the new UseCase
) : ViewModel() {

    companion object {
        val TAG = tag<MarketViewModel>()
        const val OBSERVE_TIMEOUT = 5000L
        const val LOADING_DELAY = 1000L
    }

    // --- Private State Holders & Event Triggers ---

//    private val _uiState = MutableStateFlow(MarketUiState(isLoading = true))
    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(true)
    private val _selectedType = MutableStateFlow(MarketType.SPOT)

    private val allMarketsWithPriceFlow = getMarketsWithPriceUseCase().onStart { emit(emptyList()) }
    private val connectionStateFlow = getWebSocketConnectStateUseCase()
    // Add the new network status flow
    private val networkStatusFlow = observeNetworkStatusUseCase()


    val uiState: Flow<MarketUiState> = combine(
        allMarketsWithPriceFlow,
        _selectedType,
        _isLoading,
        connectionStateFlow,
        networkStatusFlow // Add the new flow to the combine
    ) { allMarkets, selectedType, isLoading, wsConnState, networkStatus ->

        val filteredMarkets = allMarkets.filter { market ->
            when (selectedType) {
                MarketType.SPOT -> !market.isFuture
                MarketType.FUTURE -> market.isFuture
            }
        }

        // Update the error logic to prioritize network status
        val persistentError: String? = when {
            networkStatus == ConnectivityObserver.Status.Unavailable ->
                "Network unavailable."
            wsConnState == WebSocketClient.ConnectionState.DISCONNECTED ->
                "Real-time price update cannot update."
            filteredMarkets.isEmpty() ->
                "No market data to show."
            else -> null
        }

        MarketUiState(
            marketType = selectedType,
            isLoading = isLoading,
            markets = filteredMarkets,
            error = persistentError
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(OBSERVE_TIMEOUT),
        initialValue = MarketUiState(isLoading = true)
    )



    init {
        syncMarkets()
    }

    // --- Public Actions (called from the UI) ---

    fun onRetry() {
        AppLogger.d(TAG, "onRetry")
        openWebSocketUseCase()
        syncMarkets()
    }

    fun onSelectMarketTypeChange(type: MarketType) {
        AppLogger.d(TAG, "onSelectMarketTypeChange to $type")
        _selectedType.value = type
    }

    fun syncMarkets() {
        AppLogger.d(TAG, "syncMarkets")
        viewModelScope.launch {
            _isLoading.value = true
            val result = syncMarketsUseCase()
            AppLogger.d(TAG,"syncMarkets complete! Success? ${result is Resource.Success}")
            delay(LOADING_DELAY)
            _isLoading.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        closeWebSocketUseCase()
    }
}