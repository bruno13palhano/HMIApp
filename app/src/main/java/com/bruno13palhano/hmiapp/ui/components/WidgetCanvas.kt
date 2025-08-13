package com.bruno13palhano.hmiapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.bruno13palhano.core.model.DataSource
import com.bruno13palhano.core.model.Widget
import com.bruno13palhano.core.model.WidgetType
import com.bruno13palhano.hmiapp.ui.theme.HMIAppTheme

@Composable
fun WidgetCanvas(
    widgets: List<Widget>,
    onMove: (id: String, x: Float, y: Float) -> Unit,
    onEdit: (id: String) -> Unit,
    onRemove: (id: String) -> Unit,
    onEvent: (event: WidgetEvent) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.onSecondaryContainer)
    ) {
        widgets.forEach { widget ->
            WidgetRenderer(
                widget = widget,
                onMove = { x, y -> onMove(widget.id, x, y) },
                onEdit = { onEdit(widget.id) },
                onRemove = { onRemove(widget.id) },
                onEvent = onEvent,
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun WidgetCanvasPreview() {
    HMIAppTheme {
        WidgetCanvas(
            widgets = listOf(
                Widget(
                    type = WidgetType.GAUGE,
                    label = "V1",
                    dataSource = DataSource.MQTT(topic = "gauge/v1"),
                    value = "200",
                    x = 20f,
                    y = 20f
                ),
                Widget(
                    type = WidgetType.TOGGLE_BUTTON,
                    label = "T1",
                    dataSource = DataSource.MQTT(topic = "toggle/t1"),
                    value = "true",
                    x = 580f,
                    y = 20f
                ),
                Widget(
                    type = WidgetType.SWITCH,
                    label = "S1",
                    dataSource = DataSource.MQTT(topic = "switch/s1"),
                    value = "true",
                    x = 20f,
                    y = 500f
                ),
                Widget(
                    type = WidgetType.BUTTON,
                    label = "B1",
                    dataSource = DataSource.MQTT(topic = "button/b1"),
                    value = "",
                    x = 580f,
                    y = 500f
                ),
                Widget(
                    type = WidgetType.PROGRESS_BAR,
                    label = "P1",
                    dataSource = DataSource.MQTT(topic = "progress/p1"),
                    value = ".63",
                    x = 20f,
                    y = 1000f
                ),
                Widget(
                    type = WidgetType.SLIDER,
                    label = "L1",
                    dataSource = DataSource.MQTT(topic = "test/topic"),
                    value = "40.7",
                    x = 580f,
                    y = 1000f
                ),
                Widget(
                    type = WidgetType.LED_INDICATOR,
                    label = "LED1",
                    dataSource = DataSource.MQTT(topic = "test/topic"),
                    value = "WARN",
                    x = 20f,
                    y = 1500f
                ),
                Widget(
                    type = WidgetType.INPUT_FIELD,
                    label = "M1",
                    dataSource = DataSource.MQTT(topic = "test/topic"),
                    value = "ON",
                    x = 580f,
                    y = 1500f
                )
            ),
            { _, _, _ -> },
            {},
            {},
            onEvent = {}
        )
    }
}