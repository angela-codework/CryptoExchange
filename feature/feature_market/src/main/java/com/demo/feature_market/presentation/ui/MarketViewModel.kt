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
import com.demo.feature_market.data.remote.api.WebSocketClient
import com.demo.feature_market.domain.model.MarketType
import com.demo.feature_market.domain.model.MarketUiState
import com.demo.feature_market.domain.usecase.CloseWebSocketUseCase
import com.demo.feature_market.domain.usecase.GetMarketsWithPriceUseCase
import com.demo.feature_market.domain.usecase.GetWebSocketConnectStateUseCase
import com.demo.feature_market.domain.usecase.OpenWebSocketUseCase
import com.demo.feature_market.domain.usecase.SyncMarketsUseCase
import com.demo.logger.AppLogger
import com.demo.logger.AppLogger.tag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
    private val closeWebSocketUseCase: CloseWebSocketUseCase
) : ViewModel() {

    companion object {
        val TAG = tag<MarketViewModel>()
        const val OBSERVE_TIMEOUT = 5000L
    }

    // --- Private State Holders & Event Triggers ---

    // Main UI state, exposed to the UI. It's private and updated by the central `observeUiState` collector.
    private val _uiState = MutableStateFlow(MarketUiState(isLoading = true))

    // A dedicated StateFlow to represent the loading status of a one-shot sync operation.
    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)

    // Represents the market type currently selected by the user (e.g., SPOT or FUTURES).
    private val _selectedType = MutableStateFlow(MarketType.SPOT)

    // A SharedFlow for one-time "fire-and-forget" events, like showing a Toast for a sync failure.
    private val _errorEvent = MutableSharedFlow<String>()


    /**
     * The single source of truth for the UI, observed by the Fragment.
     */
    val uiState: StateFlow<MarketUiState> = _uiState

    /**
     * The stream of one-time error events for the UI to display as transient messages (e.g., Snackbar).
     */
    val errorEvent: SharedFlow<String> = _errorEvent
    //TODO: error snack bar


    // --- Private Data Flows ---

    // A hot flow from the repository that provides a continuous list of all markets with real-time prices.
    private val allMarketsWithPriceFlow = getMarketsWithPriceUseCase()

    // A hot flow from the repository that provides the current WebSocket connection state.
    private val connectionStateFlow = getWebSocketConnectStateUseCase()


    init {
        // When the ViewModel is created, start observing the data sources to build the UI state.
        observeUiState()
        // Trigger an initial sync of the market data.
        syncMarkets()
    }

    // --- Public Actions (called from the UI) ---

    /**
     * Called by the UI to retry the entire process, typically after an error.
     * This will attempt to reconnect the WebSocket and re-sync the market list.
     */
    fun onRetry() {
        AppLogger.d(TAG, "onRetry")
        openWebSocketUseCase() // Attempt to reconnect WebSocket
        syncMarkets()          // Attempt to re-sync market data
    }

    /**
     * Called by the UI when the user selects a different market type tab.
     * Updates the filter for the displayed market list.
     */
    fun onSelectMarketTypeChange(type: MarketType) {
        AppLogger.d(TAG, "onSelectMarketTypeChange to $type")
        _selectedType.value = type
    }

    /**
     * Triggers a one-shot operation to sync the market list from the remote API.
     * Manages the `isLoading` state and emits an error event on failure.
     */
    fun syncMarkets() {
        AppLogger.d(TAG, "syncMarkets")
        viewModelScope.launch {
            _isLoading.value = true
            val result = syncMarketsUseCase()
            if (result is Resource.Fail) {
                _errorEvent.emit(result.msg)
            }
            _isLoading.value = false
        }
    }


    /**
     * The central reactive pipeline.
     * This function launches a coroutine that combines all relevant data and state flows
     * into a single, definitive `MarketUiState` and collects it to update the `_uiState`.
     */
    private fun observeUiState() {
        viewModelScope.launch {
            combine(
                allMarketsWithPriceFlow,
                _selectedType,
                _isLoading,
                connectionStateFlow,
            ) { allMarkets, selectedType, isLoading, connState ->

                // Step 1: Filter the full market list based on the user's selected tab.
                val filteredMarkets = allMarkets.filter { market ->
                    when (selectedType) {
                        MarketType.SPOT -> !market.isFuture
                        MarketType.FUTURE -> market.isFuture
                    }
                }

                // Step 2: Determine if there are any persistent errors to display.
                // Connection errors have the highest priority.
                val persistentError: String? = when {
                    connState == WebSocketClient.ConnectionState.DISCONNECTED -> "Real-time price update cannot update."
                    !isLoading && filteredMarkets.isEmpty() -> "No market data to show"
                    else -> null
                }

                // Step 3: Construct the final UI state object.
                MarketUiState(
                    marketType = selectedType,
                    isLoading = isLoading,
                    markets = filteredMarkets,
                    error = persistentError
                )
            }.collect {
                // Step 4: Update the single StateFlow that the UI observes.
                _uiState.value = it
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up resources when the ViewModel is destroyed.
        closeWebSocketUseCase()
    }
}