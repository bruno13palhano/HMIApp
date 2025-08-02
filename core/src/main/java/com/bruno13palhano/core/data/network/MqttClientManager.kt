package com.bruno13palhano.core.data.network

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import java.util.concurrent.CompletableFuture
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class MqttClientManager @Inject constructor() {
    private lateinit var client: Mqtt5AsyncClient

    private val _incomingMessages = MutableSharedFlow<Pair<String, String>>(
        replay = 0,
        extraBufferCapacity = 12,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val incomingMessages: SharedFlow<Pair<String, String>> = _incomingMessages.asSharedFlow()

    suspend fun connect(
        clientId: String,
        host: String,
        port: Int,
        username: String,
        password: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        client = MqttClient.builder()
            .identifier(clientId)
            .serverHost(host)
            .serverPort(port)
            .useMqttVersion5()
            .buildAsync()

        try {
            client.connectWith()
                .simpleAuth()
                .username(username)
                .password(password.toByteArray())
                .applySimpleAuth()
                .send()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun subscribe(topic: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            client.subscribeWith()
                .topicFilter(topic)
                .callback { publish ->
                    val message = String(publish.payloadAsBytes)
                        _incomingMessages.tryEmit(topic to message)
                }
                .send()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun publish(topic: String, message: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            client.publishWith()
                .topic(topic)
                .payload(message.toByteArray())
                .send()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun disconnect(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            client.disconnect().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun <T> CompletableFuture<T>.await() = withContext(Dispatchers.IO) {
        try {
            get()
        } catch (e: Exception) {
            throw e
        }
    }
}