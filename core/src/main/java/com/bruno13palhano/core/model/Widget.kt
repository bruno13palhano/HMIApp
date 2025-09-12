package com.bruno13palhano.core.model

import java.util.UUID

data class Widget(
    val id: String = UUID.randomUUID().toString(),
    val type: WidgetType,
    val label: String,
    val dataSource: DataSource,
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float = 160f,
    val height: Float = 160f,
    val value: String = "",
    val extras: List<String>? = null,
    val isPinned: Boolean = false,
    val environmentId: Long
)

enum class WidgetType {
    TEXT,         // Displays text (MQTT: subscribe, HTTP: GET)
    BUTTON,       // Triggers an action (MQTT: publish, HTTP: POST)
    SWITCH,       // Toggles a binary state (MQTT: publish, HTTP: POST)
    SLIDER,       // Adjusts a value in a range (MQTT: publish, HTTP: POST)
    GAUGE,        // Shows a value on a gauge (MQTT: subscribe, HTTP: GET)
    PROGRESS_BAR, // Displays progress/percentage (MQTT: subscribe, HTTP: GET)
    CHART,        // Visualizes data over time (MQTT: subscribe, HTTP: GET)
    TOGGLE_BUTTON,// Maintains a pressed/unpressed state (MQTT: publish, HTTP: POST)
    INPUT_FIELD,  // Allows text/number input (MQTT: publish, HTTP: POST)
    LED_INDICATOR,// Shows status with a colored indicator (MQTT: subscribe, HTTP: GET)
    DROPDOWN,     // Selects from options (MQTT: publish, HTTP: POST)
}
