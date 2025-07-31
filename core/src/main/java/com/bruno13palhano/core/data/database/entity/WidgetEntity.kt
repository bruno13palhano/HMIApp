package com.bruno13palhano.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bruno13palhano.core.model.DataSource
import com.bruno13palhano.core.model.Widget
import com.bruno13palhano.core.model.WidgetType

@Entity(tableName = "widgets")
data class WidgetEntity(
    @PrimaryKey val id: String,
    val type: String,
    val label: String,
    val dataSourceType: String,
    val data: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)

fun WidgetEntity.toDomain(): Widget {
    val dataSource = when (dataSourceType) {
        "MQTT" -> DataSource.MQTT(data)
        "HTTP" -> DataSource.HTTP(data)
        else -> DataSource.MQTT("")
    }
    return Widget(id, WidgetType.valueOf(type), label, dataSource, x, y, width, height)
}

fun Widget.toEntity(): WidgetEntity {
    val (typeStr, dataStr) = when (dataSource) {
        is DataSource.MQTT -> "MQTT" to dataSource.topic
        is DataSource.HTTP -> "HTTP" to dataSource.url
    }
    return WidgetEntity(id, type.name, label, typeStr, dataStr, x, y, width, height)
}
