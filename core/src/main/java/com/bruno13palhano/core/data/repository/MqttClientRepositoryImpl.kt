package com.bruno13palhano.core.data.repository

import com.bruno13palhano.core.data.network.MqttClientManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

internal class MqttClientRepositoryImpl @Inject constructor(
    private val mqtt: MqttClientManager
) : MqttClientRepository {

    override suspend fun connectMqtt(
        clientId: String,
        host: String,
        port: Int,
        username: String,
        password: String
    ): Result<Unit> {
        return mqtt.connect(
            clientId = clientId,
            host = host,
            port = port,
            username = username,
            password = password
        )
    }

    override suspend fun subscribeToTopic(topic: String): Result<Unit> {
        return mqtt.subscribe(topic = topic)
    }

    override suspend fun publish(topic: String, message: String): Result<Unit> {
        return mqtt.publish(topic = topic, message = message)
    }

    override fun isConnected(): Flow<Boolean> {
        return mqtt.isConnected()
    }

    override suspend fun disconnect(): Result<Unit> {
        return mqtt.disconnect()
    }

    override fun incomingMessages(): SharedFlow<Pair<String, String>> {
        return mqtt.incomingMessages
    }
}