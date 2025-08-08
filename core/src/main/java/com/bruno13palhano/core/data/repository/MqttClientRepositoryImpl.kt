package com.bruno13palhano.core.data.repository

import com.bruno13palhano.core.data.network.MqttClientManager
import com.bruno13palhano.core.data.connection.Connection
import com.bruno13palhano.core.data.connection.ConnectionSessionImpl
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class MqttClientRepositoryImpl @Inject constructor(
    private val mqtt: MqttClientManager,
    private val connectionSession: ConnectionSessionImpl
) : MqttClientRepository {

    override suspend fun connectIfSessionExists() {
        connectionSession.get()?.let {
            mqtt.connect(
                clientId = it.clientId,
                host = it.host,
                port = it.port,
                username = it.username,
                password = it.password
            )
        }
    }

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
            .onSuccess {
                connectionSession.save(
                    connection = Connection(
                        clientId = clientId,
                        host = host,
                        port = port,
                        username = username,
                        password = password
                    )
                )
            }
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

    override fun incomingMessages() = mqtt.incomingMessages
}