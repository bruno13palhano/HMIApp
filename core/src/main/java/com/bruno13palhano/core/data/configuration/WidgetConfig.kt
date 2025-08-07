package com.bruno13palhano.core.data.configuration

import com.bruno13palhano.core.model.DataSource
import com.bruno13palhano.core.model.Widget
import com.bruno13palhano.core.model.WidgetType
import kotlinx.serialization.Serializable

@Serializable
data class WidgetConfig(
    val id: String,
    val type: String,
    val label: String,
    val dataSource: DataSourceConfig,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val value: String
)

@Serializable
sealed class DataSourceConfig {
    @Serializable
    data class MQTT(val topic: String) : DataSourceConfig()
    @Serializable
    data class HTTP(val url: String, val method: String = "GET") : DataSourceConfig()
}

@Serializable
data class LayoutConfig(val widgets: List<WidgetConfig>)

fun Widget.toWidgetConfig(): WidgetConfig = WidgetConfig(
    id = id,
    type = type.name,
    label = label,
    dataSource = when (dataSource) {
        is DataSource.MQTT -> DataSourceConfig.MQTT(dataSource.topic)
        is DataSource.HTTP -> DataSourceConfig.HTTP(dataSource.url, dataSource.method)
    },
    x = x,
    y = y,
    width = width,
    height = height,
    value = value
)

fun WidgetConfig.toWidget(): Widget = Widget(
    id = id,
    type = WidgetType.valueOf(type),
    label = label,
    dataSource = when (dataSource) {
        is DataSourceConfig.MQTT -> DataSource.MQTT(dataSource.topic)
        is DataSourceConfig.HTTP -> DataSource.HTTP(dataSource.url, dataSource.method)
    },
    x = x,
    y = y,
    width = width,
    height = height,
    value = value
)