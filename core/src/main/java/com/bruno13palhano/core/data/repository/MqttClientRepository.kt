package com.bruno13palhano.core.data.repository

import com.bruno13palhano.core.data.network.MqttConnectionConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface MqttClientRepository {
    suspend fun connectIfSessionExists()
    suspend fun connectMqtt(mqttConnectionConfig: MqttConnectionConfig): Result<Unit>
    suspend fun subscribeToTopic(topic: String): Result<Unit>
    suspend fun publish(topic: String, message: String): Result<Unit>
    fun isConnected(): Flow<Boolean>
    suspend fun disconnect(): Result<Unit>
    fun incomingMessages(): SharedFlow<Pair<String, String>>
}