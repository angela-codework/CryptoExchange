package com.demo.feature_market.data.remote.api

import com.demo.core.di.Qualifiers
import com.demo.core.util.fromJsonOrNull
import com.demo.core.util.logger.AppLogger
import com.demo.core.util.logger.AppLogger.tag
import com.demo.core.util.toJsonOrNull
import com.demo.feature_market.core.common.Constants.BASE_WS_URL
import com.demo.feature_market.data.remote.dto.WebSocketResponseDto
import com.demo.feature_market.data.remote.dto.WebSocketSubscriptDto
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class WebSocketClient @Inject constructor(
    @property:Named(Qualifiers.API_CLIENT_WS) private val client: OkHttpClient,
    private val gson: Gson
) {

    enum class ConnectionState {
        CONNECTED, DISCONNECTED, CONNECTING
    }

    companion object {
        val TAG = tag<WebSocketClient>()
        const val COIN_INDEX_TOPIC = "coinIndex"
        const val SUBSCRIBE_OP = "subscribe"
        const val CODE_CLOSE = 1000
        const val MAX_RETRY = 3
        const val RETRY_INTERVAL = 2000L
    }

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var retryCount = 0

    private var webSocket: WebSocket? = null
    private val _responseUpdate: MutableSharedFlow<WebSocketResponseDto> = MutableSharedFlow(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val _connectionState: MutableStateFlow<ConnectionState> = MutableStateFlow(ConnectionState.CONNECTING)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    val responseUpdate: SharedFlow<WebSocketResponseDto> = _responseUpdate

    private val request: Request by lazy {
        Request.Builder()
            .url(BASE_WS_URL)
            .build()
    }

    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            _connectionState.value = ConnectionState.CONNECTED
            retryCount = 0
            subscribeTopic(webSocket)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            text.fromJsonOrNull<WebSocketResponseDto>(gson)?.let { dto ->
                _responseUpdate.tryEmit(dto)
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            AppLogger.e(TAG, "${t.message}")
            handleDisconnection()
        }

        override fun onClosed(
            webSocket: WebSocket,
            code: Int,
            reason: String
        ) {
            super.onClosed(webSocket, code, reason)
            handleDisconnection()
        }
    }

    private fun handleDisconnection() {
        AppLogger.d(TAG, "Handle Disconnection")

        if(retryCount < MAX_RETRY) {
            retryCount ++

            val delayMillis = retryCount * RETRY_INTERVAL
            AppLogger.d(TAG, "Will retry in ${delayMillis/1000} s... (Attempt $retryCount/$MAX_RETRY)")

            coroutineScope.launch {
                delay(delayMillis)
                connectInternal()
            }
        } else {
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }

    private fun connectInternal() {
        webSocket = client.newWebSocket(request, webSocketListener)
        _connectionState.value = ConnectionState.CONNECTING
    }

    fun connect() {
        AppLogger.d(TAG, "Do connect WebSocket")
        retryCount = 0
        connectInternal()
    }

    fun disconnect() {
        webSocket?.close(CODE_CLOSE, "Closing")
        webSocket = null
        retryCount = MAX_RETRY
    }

    private fun subscribeTopic(webSocket: WebSocket) {
        val subDto = WebSocketSubscriptDto(
            op = SUBSCRIBE_OP,
            args = listOf(COIN_INDEX_TOPIC)
        )

        subDto.toJsonOrNull(gson)?.let { sub ->
            webSocket.send(sub)
        }
    }
}