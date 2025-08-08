package com.bruno13palhano.core.data.connection

import kotlinx.serialization.Serializable

@Serializable
data class Connection(
    val clientId: String,
    val host: String,
    val port: Int,
    val username: String,
    val password: String
)
