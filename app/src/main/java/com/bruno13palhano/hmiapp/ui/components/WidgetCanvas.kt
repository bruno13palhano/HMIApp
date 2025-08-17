package com.bruno13palhano.hmiapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import com.bruno13palhano.core.model.DataSource
import com.bruno13palhano.core.model.Widget
import com.bruno13palhano.core.model.WidgetType
import com.bruno13palhano.hmiapp.ui.theme.HMIAppTheme

@Composable
fun WidgetCanvas(
    widgets: List<Widget>,
    initialScale: Float = 1f,
    initialOffset: Offset = Offset.Zero,
    onMove: (id: String, x: Float, y: Float) -> Unit,
    onEdit: (id: String) -> Unit,
    onRemove: (id: String) -> Unit,
    onEvent: (event: WidgetEvent) -> Unit,
    onTransformChange: (scale: Float, offset: Offset) -> Unit
) {
    var scale by remember { mutableFloatStateOf(initialScale) }
    var offset by remember { mutableStateOf(initialOffset) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.onSecondaryContainer)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(0.3f, 3f)
                    val newOffset = offset + pan

                    scale = newScale
                    offset = newOffset

                    onTransformChange(newScale, newOffset)
                }
            }
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
                .fillMaxSize()
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
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun WidgetCanvasPreview() {
    HMIAppTheme {
        WidgetCanvas(
            widgets = listOf(
                Widget(
                    type = WidgetType.GAUGE,
                    label = "Tension for M1",
                    dataSource = DataSource.MQTT(topic = "/gauge/v1"),
                    value = "200",
                    x = 20f,
                    y = 20f
                ),
                Widget(
                    type = WidgetType.TEXT,
                    label = "T1",
                    dataSource = DataSource.HTTP(url = "https://www.test.com/text/t1"),
                    value = "Hello word",
                    x = 580f,
                    y = 20f
                ),
                Widget(
                    type = WidgetType.SWITCH,
                    label = "Switch for M1",
                    dataSource = DataSource.MQTT(topic = "/switch/s1"),
                    value = "true",
                    x = 20f,
                    y = 500f
                ),
                Widget(
                    type = WidgetType.BUTTON,
                    label = "B1",
                    dataSource = DataSource.MQTT(topic = "/button/b1"),
                    value = "",
                    x = 580f,
                    y = 500f
                ),
                Widget(
                    type = WidgetType.PROGRESS_BAR,
                    label = "P1",
                    dataSource = DataSource.MQTT(topic = "/progress/p1"),
                    value = ".63",
                    x = 20f,
                    y = 1000f
                ),
                Widget(
                    type = WidgetType.SLIDER,
                    label = "L1",
                    dataSource = DataSource.HTTP(url = "http://www.test.com/slider/l1"),
                    value = "40.7",
                    x = 580f,
                    y = 1000f
                ),
                Widget(
                    type = WidgetType.LED_INDICATOR,
                    label = "LED1",
                    dataSource = DataSource.HTTP(url = "https://www.test.com/leds/l1"),
                    value = "WARN",
                    x = 20f,
                    y = 1500f
                ),
                Widget(
                    type = WidgetType.INPUT_FIELD,
                    label = "IP1",
                    dataSource = DataSource.HTTP(url = "https://www.test.com/input/ip1"),
                    value = "ON",
                    x = 580f,
                    y = 1500f
                )
            ),
            onMove = { _, _, _ -> },
            onEdit = {},
            onRemove = {},
            onEvent = {},
            onTransformChange = { _, _ -> }
        )
    }
}