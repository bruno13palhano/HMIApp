package com.bruno13palhano.core.data.configuration

import kotlinx.serialization.Serializable

@Serializable
data class LayoutConfig(val environment: EnvironmentConfig, val widgets: List<WidgetConfig>)
