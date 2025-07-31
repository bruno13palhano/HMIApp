package com.bruno13palhano.core.model

sealed class DataSource {
    data class MQTT(val topic: String) : DataSource()
    data class HTTP(val url: String, val method: String = "GET") : DataSource()
}
