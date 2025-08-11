package com.bruno13palhano.core.data.repository

import com.bruno13palhano.core.data.network.MqttClientManager
import com.bruno13palhano.core.data.database.dao.MqttConnectionConfigDao
import com.bruno13palhano.core.data.database.entity.toEntity
import com.bruno13palhano.core.data.database.entity.toDomain
import com.bruno13palhano.core.data.network.MqttConnectionConfig
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class MqttClientRepositoryImpl @Inject constructor(
    private val mqtt: MqttClientManager,
    private val mqttConnectionConfigDao: MqttConnectionConfigDao
) : MqttClientRepository {

    override suspend fun connectIfSessionExists() {
        mqttConnectionConfigDao.getConfig()?.toDomain()?.let {
            mqtt.connect(mqttConnectionConfig = it)
        }
    }

    override suspend fun connectMqtt(mqttConnectionConfig: MqttConnectionConfig): Result<Unit> {
        return mqtt.connect(mqttConnectionConfig = mqttConnectionConfig).onSuccess {
            mqttConnectionConfigDao.saveConfig(entity = mqttConnectionConfig.toEntity())
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
        return mqtt.disconnect().onSuccess {
            mqttConnectionConfigDao.clear()
        }
    }

    override fun incomingMessages() = mqtt.incomingMessages
}