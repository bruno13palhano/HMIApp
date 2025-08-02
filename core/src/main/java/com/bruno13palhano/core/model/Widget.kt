package com.bruno13palhano.core.model

import java.util.UUID

data class Widget(
    val id: String = UUID.randomUUID().toString(),
    val type: WidgetType,
    val label: String,
    val dataSource: DataSource,
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float = 120f,
    val height: Float = 120f,
    val value: String = ""
)

enum class WidgetType { TEXT, BUTTON, SWITCH }
