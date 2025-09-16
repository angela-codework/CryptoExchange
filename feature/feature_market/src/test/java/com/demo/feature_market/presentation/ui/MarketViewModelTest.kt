package com.demo.feature_market.presentation.ui

import com.demo.core.common.Resource
import com.demo.feature_market.data.remote.api.WebSocketClient
import com.demo.feature_market.domain.model.Market
import com.demo.feature_market.domain.model.MarketType
import com.demo.feature_market.domain.usecase.*
import com.demo.feature_market.util.MainCoroutineRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class MarketViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var syncMarketsUseCase: SyncMarketsUseCase
    private lateinit var getMarketsWithPriceUseCase: GetMarketsWithPriceUseCase
    private lateinit var getWebSocketConnectStateUseCase: GetWebSocketConnectStateUseCase
    private lateinit var openWebSocketUseCase: OpenWebSocketUseCase
    private lateinit var closeWebSocketUseCase: CloseWebSocketUseCase

    private lateinit var viewModel: MarketViewModel

    @Before
    fun setUp() {
        syncMarketsUseCase = mockk()
        getMarketsWithPriceUseCase = mockk()
        getWebSocketConnectStateUseCase = mockk()
        openWebSocketUseCase = mockk(relaxed = true)
        closeWebSocketUseCase = mockk(relaxed = true)

        // Default mock behaviors
        every { getMarketsWithPriceUseCase() } returns flowOf(emptyList())
        every { getWebSocketConnectStateUseCase() } returns MutableStateFlow(WebSocketClient.ConnectionState.CONNECTED)
        coEvery { syncMarketsUseCase() } returns Resource.Success(emptyList())
    }

    private fun createViewModel() {
        viewModel = MarketViewModel(
            syncMarketsUseCase,
            getMarketsWithPriceUseCase,
            getWebSocketConnectStateUseCase,
            openWebSocketUseCase,
            closeWebSocketUseCase
        )
    }

    @Test
    fun init_initial_state_is_loading() = runTest {
        createViewModel()
        val initialState = viewModel.uiState.first()
        assertTrue(initialState.isLoading)
    }

    @Test
    fun init_calls_syncMarkets_on_init() = runTest {
        createViewModel()
        coVerify { syncMarketsUseCase() }
    }

    @Test
    fun syncMarkets_when_success_isLoading_becomes_false() = runTest {
        createViewModel()
        viewModel.syncMarkets()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun syncMarkets_when_fail_error_event_is_emitted() = runTest {
        val errorMessage = "Sync failed"
        coEvery { syncMarketsUseCase() } returns Resource.Fail(errorMessage)
        createViewModel()

        viewModel.syncMarkets()

        val error = viewModel.errorEvent.first()
        assertEquals(errorMessage, error)
    }

    @Test
    fun uiState_combines_data_sources_correctly() = runTest {
        val markets = listOf(
            Market(symbol = "BTC-USD", isFuture = false, price = 50000.0),
            Market(symbol = "ETH-PERP", isFuture = true, price = 4000.0)
        )
        every { getMarketsWithPriceUseCase() } returns flowOf(markets)

        createViewModel()

        var uiState = viewModel.uiState.value
        assertEquals(MarketType.SPOT, uiState.marketType)
        assertEquals(1, uiState.markets.size)
        assertEquals("BTC-USD", uiState.markets.first().symbol)

        // Change market type
        viewModel.onSelectMarketTypeChange(MarketType.FUTURE)

        uiState = viewModel.uiState.value
        assertEquals(MarketType.FUTURE, uiState.marketType)
        assertEquals(1, uiState.markets.size)
        assertEquals("ETH-PERP", uiState.markets.first().symbol)
    }

    @Test
    fun onCleared_calls_closeWebSocketUseCase() = runTest {
        createViewModel()
        viewModel.onCleared()
        coVerify { closeWebSocketUseCase() }
    }
}
