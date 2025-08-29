package com.bruno13palhano.core.data.configuration

import com.bruno13palhano.core.model.Environment
import kotlinx.serialization.Serializable

@Serializable
data class EnvironmentConfig(
    val id: Long,
    val name: String,
    val scale: Float,
    val offsetX: Float,
    val offsetY: Float
)

fun Environment.toEnvironmentConfig(): EnvironmentConfig {
    return EnvironmentConfig(
        id = id,
        name = name,
        scale = scale,
        offsetX = offsetX,
        offsetY = offsetY
    )
}

fun EnvironmentConfig.toEnvironment(): Environment {
    return Environment(
        id = id,
        name = name,
        scale = scale,
        offsetX = offsetX,
        offsetY = offsetY
    )
}