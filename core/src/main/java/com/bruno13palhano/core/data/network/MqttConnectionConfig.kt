package com.bruno13palhano.core.data.network

import kotlinx.serialization.Serializable

@Serializable
data class MqttConnectionConfig(
    val clientId: String = "",
    val host: String = "",
    val port: Int = 1883,
    val username: String? = null,
    val password: String? = null,
    val caBytes: ByteArray? = null,
    val clientP12Bytes: ByteArray? = null,
    val p12Password: String? = "",
)
