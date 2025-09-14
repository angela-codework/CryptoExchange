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
 * 3.  **Handling User Actions:** It provides public methods like `onMarketTypeSelected` and `onRetry`
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
 * @see MarketRepository for the underlying data provider.
 */
package com.demo.feature_market.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.core.common.Resource
import com.demo.feature_market.data.remote.api.WebSocketClient
import com.demo.feature_market.domain.model.Market
import com.demo.feature_market.domain.model.MarketType
import com.demo.feature_market.domain.model.MarketUiState
import com.demo.feature_market.domain.usecase.CloseWebSocketUseCase
import com.demo.feature_market.domain.usecase.GetMarketsByTypeUseCase
import com.demo.feature_market.domain.usecase.GetMarketsWithPriceByTypeUseCase
import com.demo.feature_market.domain.usecase.GetWebSocketConnectStateUseCase
import com.demo.feature_market.domain.usecase.OpenWebSocketUseCase
import com.demo.logger.AppLogger.tag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel for the Market screen.
 * This ViewModel is responsible for orchestrating data flows from use cases,
 * handling user actions, and providing a single source of truth (UiState) for the UI.
 */
@HiltViewModel
class MarketViewModel @Inject constructor(
    private val getMarketsWithPriceByTypeUseCase: GetMarketsWithPriceByTypeUseCase,
    private val openWebSocketUseCase: OpenWebSocketUseCase,
    private val closeWebSocketUseCase: CloseWebSocketUseCase,
    private val getWebSocketConnectStateUseCase: GetWebSocketConnectStateUseCase,
    private val getMarketsByTypeUseCase: GetMarketsByTypeUseCase
) : ViewModel() {

    companion object {
        val TAG = tag<MarketViewModel>()
        const val OBSERVE_TIMEOUT = 5000L
    }

    // Represents the market type currently selected by the user (ex. SPOT or FUTURES).
    private val _selectedType = MutableStateFlow(MarketType.SPOT)

    // An event trigger to manually restart the data fetching process, ex. on user "retry" click.
    private val _retryTrigger = MutableSharedFlow<Unit>()

    // A combined trigger that emits the latest marketType whenever the type changes or a retry is requested.
    private val actionTrigger = combine(_selectedType, _retryTrigger.onStart { emit(Unit) }) { type, _ -> type }

    /**
     * The single source of truth for the UI.
     * It is constructed by `createUiStateFlow` and converted into a hot StateFlow,
     * ensuring it survives configuration changes and is shared among all collectors.
     */
    val uiState: StateFlow<MarketUiState> = createUiStateFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(OBSERVE_TIMEOUT),
            initialValue = MarketUiState(isLoading = false)
        )

    init {
        openWebSocketUseCase()
    }

    /**
     * * Called by the UI when the user selects a different market type tab.
     */
    fun onSelectMarketTypeChange(type: MarketType) {
        _selectedType.value = type
    }

    /**
     * Called by the UI when the user clicks a "retry" button after a failure.
     */
    fun onRetry() {
        _retryTrigger.tryEmit(Unit)
    }

    /**
     * Top-level function to create the final UI state by combining various data sources.
     * Its sole responsibility is to orchestrate the combination of different state flows.
     */
    private fun createUiStateFlow() : Flow<MarketUiState> {
        // 1. Get the flow representing the core business(markets) logic.
        val marketsUiFlow = createMarketsUiFlow()
        // 2. Get the flow representing the global WebSocket connection state.
        val wsConnectionFlow = getWebSocketConnectStateUseCase()

        // 3. Combine them to produce the final, definitive UI state.
        return combine(marketsUiFlow, wsConnectionFlow) { marketState, weConnectState ->
            if(weConnectState == WebSocketClient.ConnectionState.DISCONNECTED) {
                // The DISCONNECTED state has the highest priority.
                // It overrides any other state to display a connection error to the user.
                marketState.copy(isLoading = false, error = "Connection is lost. Prices stop updating!")
            } else {
                marketState
            }
        }
    }

    /**
     * Creates the core business logic flow.
     * This flow acts as a state machine that responds to the actionTrigger (user changing type or retrying).
     * It describes the entire process: Loading -> Sync -> Success/Failure -> Listening for data.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun createMarketsUiFlow() : Flow<MarketUiState> {
        return actionTrigger.flatMapLatest { marketType ->
            flow {
                emit(MarketUiState(isLoading = true))

                when(val result = getMarketsByTypeUseCase(marketType)) {
                    is Resource.Success<List<Market>> -> {
                        getMarketsWithPriceByTypeUseCase(marketType).collect { markets ->
                            emit(MarketUiState(isLoading = false, markets = markets))
                        }
                    }
                    is Resource.Fail<*> -> {
                        emit(MarketUiState(isLoading = false, error = result.msg))
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        closeWebSocketUseCase()
    }

}