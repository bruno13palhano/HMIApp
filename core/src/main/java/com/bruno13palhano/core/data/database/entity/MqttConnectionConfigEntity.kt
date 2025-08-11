package com.bruno13palhano.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bruno13palhano.core.data.network.MqttConnectionConfig
import com.bruno13palhano.core.data.secure.EncryptedBytes
import com.bruno13palhano.core.data.secure.EncryptedString

@Entity(tableName = "mqtt_connection")
internal data class MqttConnectionConfigEntity(
    @PrimaryKey val id: Int = 0,
    val clientId: String = "",
    val host: String = "",
    val port: Int = 8883,
    val username: EncryptedString? = null,
    val password: EncryptedString? = null,
    val caBytes: EncryptedBytes? = null,
    val clientP12Bytes: EncryptedBytes? = null,
    val p12Password: EncryptedString? = null
)

internal fun MqttConnectionConfigEntity.toDomain(): MqttConnectionConfig = MqttConnectionConfig(
    clientId = clientId,
    host = host,
    port = port,
    username = username?.value,
    password = password?.value,
    caBytes = caBytes?.value,
    clientP12Bytes = clientP12Bytes?.value,
    p12Password = p12Password?.value
)

internal fun MqttConnectionConfig.toEntity(): MqttConnectionConfigEntity =
    MqttConnectionConfigEntity(
        id = 0,
        clientId = clientId,
        host = host,
        port = port,
        username = EncryptedString(value = username),
        password = EncryptedString(value = password),
        caBytes = EncryptedBytes(value = caBytes),
        clientP12Bytes = EncryptedBytes(value = clientP12Bytes),
        p12Password = EncryptedString(value = p12Password)
    )