package com.bruno13palhano.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bruno13palhano.core.model.DataSource
import com.bruno13palhano.core.model.Widget
import com.bruno13palhano.core.model.WidgetType

@Entity(tableName = "widgets")
internal data class WidgetEntity(
    @PrimaryKey val id: String,
    val type: String,
    val label: String,
    val dataSourceType: String,
    val data: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val limit: String? = null,
    val extras: List<String>?,
    val isPinned: Boolean,
    val environmentId: Long,
)

internal fun WidgetEntity.toDomain(): Widget {
    val dataSource = when (dataSourceType) {
        "MQTT" -> DataSource.MQTT(data)
        "HTTP" -> DataSource.HTTP(data)
        else -> DataSource.MQTT("")
    }
    return Widget(
        id = id,
        type = WidgetType.valueOf(type),
        label = label,
        dataSource = dataSource,
        x = x,
        y = y,
        width = width,
        height = height,
        limit = limit,
        extras = extras,
        isPinned = isPinned,
        environmentId = environmentId,
    )
}

internal fun Widget.toEntity(): WidgetEntity {
    val (typeStr, dataStr) = when (dataSource) {
        is DataSource.MQTT -> "MQTT" to dataSource.topic
        is DataSource.HTTP -> "HTTP" to dataSource.url
    }
    return WidgetEntity(
        id = id,
        type = type.name,
        label = label,
        dataSourceType = typeStr,
        data = dataStr,
        x = x,
        y = y,
        width = width,
        height = height,
        limit = limit,
        extras = extras,
        isPinned = isPinned,
        environmentId = environmentId,
    )
}
