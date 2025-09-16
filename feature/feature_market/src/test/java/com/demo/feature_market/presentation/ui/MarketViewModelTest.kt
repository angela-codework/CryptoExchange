package com.demo.feature_market.presentation.ui

import app.cash.turbine.test
import com.demo.core.common.Resource
import com.demo.core.common.connectivity.ConnectivityObserver
import com.demo.feature_market.data.remote.api.WebSocketClient
import com.demo.feature_market.domain.model.Market
import com.demo.feature_market.domain.model.MarketType
import com.demo.feature_market.domain.usecase.*
import com.demo.feature_market.util.MainCoroutineRule
import com.demo.logger.AppLogger
import com.demo.logger.Logger
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class MarketViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    // Mocks for all dependencies of the ViewModel.
    private lateinit var syncMarketsUseCase: SyncMarketsUseCase
    private lateinit var getMarketsWithPriceUseCase: GetMarketsWithPriceUseCase
    private lateinit var getWebSocketConnectStateUseCase: GetWebSocketConnectStateUseCase
    private lateinit var openWebSocketUseCase: OpenWebSocketUseCase
    private lateinit var closeWebSocketUseCase: CloseWebSocketUseCase
    private lateinit var observeNetworkStatusUseCase: ObserveNetworkStatusUseCase

    private lateinit var viewModel: MarketViewModel

    @Before
    fun setUp() {
        // Set a test logger to prevent crashes on a local JVM.
        AppLogger.setLogger(object : Logger {
            override fun d(tag: String, message: String) {}
            override fun i(tag: String, message: String) {}
            override fun w(tag: String, message: String) {}
            override fun e(tag: String, message: String) {}
        })

        // Instantiate mocks
        syncMarketsUseCase = mockk()
        getMarketsWithPriceUseCase = mockk()
        getWebSocketConnectStateUseCase = mockk()
        openWebSocketUseCase = mockk(relaxed = true)
        closeWebSocketUseCase = mockk(relaxed = true)
        observeNetworkStatusUseCase = mockk()

        // Provide default successful behavior for all mocks.
        coEvery { syncMarketsUseCase() } returns Resource.Success(emptyList())
        every { getMarketsWithPriceUseCase() } returns flowOf(emptyList())
        every { getWebSocketConnectStateUseCase() } returns MutableStateFlow(WebSocketClient.ConnectionState.CONNECTED)
        every { observeNetworkStatusUseCase() } returns flowOf(ConnectivityObserver.Status.Available)
    }

    // Helper function to create the ViewModel instance with all mocks.
    private fun createViewModel() {
        viewModel = MarketViewModel(
            syncMarketsUseCase,
            getMarketsWithPriceUseCase,
            getWebSocketConnectStateUseCase,
            openWebSocketUseCase,
            closeWebSocketUseCase,
            observeNetworkStatusUseCase
        )
    }

    @Test
    fun init_triggers_sync() = runTest {
        createViewModel()
        coVerify(exactly = 1) { syncMarketsUseCase() }
    }

    @Test
    fun uiState_networkUnavailable_shows_error() = runTest {
        every { observeNetworkStatusUseCase() } returns flowOf(ConnectivityObserver.Status.Unavailable)
        createViewModel()

        viewModel.uiState.test { 
            val firstState = awaitItem()
            assertTrue(firstState.error?.contains("Network unavailable") ?: false)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_marketTypeChange_updates_list() = runTest {
        val markets = listOf(
            Market(symbol = "BTC-USD", isFuture = false, price = 50000.0),
            Market(symbol = "ETH-PERP", isFuture = true, price = 4000.0)
        )
        every { getMarketsWithPriceUseCase() } returns flowOf(markets)
        createViewModel()

        viewModel.uiState.test { 
            var state = awaitItem()
            assertEquals(1, state.markets.size)
            assertEquals("BTC-USD", state.markets.first().symbol)

            viewModel.onSelectMarketTypeChange(MarketType.FUTURE)

            state = awaitItem()
            assertEquals(1, state.markets.size)
            assertEquals("ETH-PERP", state.markets.first().symbol)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onCleared_closes_webSocket() {
        createViewModel()
        viewModel.onCleared()
        coVerify(exactly = 1) { closeWebSocketUseCase() }
    }
}