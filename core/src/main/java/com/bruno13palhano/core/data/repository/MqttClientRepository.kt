package com.bruno13palhano.core.data.repository

import kotlinx.coroutines.flow.SharedFlow

interface MqttClientRepository {
    suspend fun connectMqtt(
        clientId: String,
        host: String,
        port: Int,
        username: String,
        password: String
    ): Result<Unit>
    suspend fun subscribeToTopic(topic: String): Result<Unit>
    suspend fun publish(topic: String, message: String): Result<Unit>
    suspend fun disconnect(): Result<Unit>
    fun incomingMessages(): SharedFlow<Pair<String, String>>
}