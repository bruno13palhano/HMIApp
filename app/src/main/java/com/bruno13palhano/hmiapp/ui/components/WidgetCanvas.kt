package com.bruno13palhano.hmiapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import com.bruno13palhano.core.model.DataSource
import com.bruno13palhano.core.model.Widget
import com.bruno13palhano.core.model.WidgetType
import com.bruno13palhano.hmiapp.ui.theme.HMIAppTheme
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce

const val CANVAS_SIZE = 5000f

@OptIn(FlowPreview::class)
@Composable
fun WidgetCanvas(
    widgets: List<Widget>,
    initialScale: Float = 1f,
    initialOffset: Offset = Offset.Zero,
    onDragEnd: (id: String, x: Float, y: Float) -> Unit,
    onTogglePin: (id: String) -> Unit,
    onNotify: (id: String) -> Unit,
    onEdit: (id: String) -> Unit,
    onRemove: (id: String) -> Unit,
    onEvent: (event: WidgetEvent) -> Unit,
    onTransformChange: (scale: Float, offset: Offset) -> Unit
) {
    var scale by remember { mutableFloatStateOf(initialScale) }
    var offset by remember { mutableStateOf(initialOffset) }
    var screenSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(initialScale, initialOffset) {
        scale = initialScale
        offset = initialOffset
    }

    LaunchedEffect(Unit) {
        snapshotFlow { scale to offset }
            .debounce(300)
            .collectLatest { (s, o) ->
                onTransformChange(s, o)
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.onSecondaryContainer)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(0.3f, 3f)
                    val newOffset = offset + pan

                    val screenWidth = screenSize.width.toFloat()
                    val screenHeight = screenSize.height.toFloat()
                    val scaledWidth = CANVAS_SIZE * newScale
                    val scaledHeight = CANVAS_SIZE * newScale

                    val maxTranslationX = (scaledWidth - screenWidth) / 2
                    val maxTranslationY = (scaledHeight - screenHeight) / 2

                    offset = if (maxTranslationX > 0 && maxTranslationY > 0) {
                        Offset(
                            x = newOffset.x.coerceIn(-maxTranslationX, maxTranslationX),
                            y = newOffset.y.coerceIn(-maxTranslationY, maxTranslationY)
                        )
                    } else {
                        Offset.Zero
                    }

                    scale = newScale
                }
            }
            .onSizeChanged { screenSize = it }
    ) {
        Canvas(
            modifier = Modifier
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
                .fillMaxSize()
        ) {
            val step = 100f
            val gridColor = Color.Red.copy(alpha = 0.25f)

            val halfSize = CANVAS_SIZE / 2

            // Drawn vertical lines (from -halfSize to halfSize)
            var x = -halfSize
            while (x <=  halfSize) {
                drawLine(
                    color = gridColor,
                    start = Offset(x, -halfSize),
                    end = Offset(x, halfSize),
                    strokeWidth = 1f
                )
                x += step
            }

            // Draw horizontal lines (from -halfSize to halfSize)
            var y = -halfSize
            while (y <= halfSize) {
                drawLine(
                    color = gridColor,
                    start = Offset(-halfSize, y),
                    end = Offset(halfSize, y),
                    strokeWidth = 1f
                )
                y += step
            }
        }

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
                key(widget.id) {
                    WidgetRenderer(
                        widget = widget,
                        onDragEnd = { x, y -> onDragEnd(widget.id, x, y) },
                        onTogglePin = { onTogglePin(widget.id) },
                        onNotify = { onNotify(widget.id) },
                        onEdit = { onEdit(widget.id) },
                        onRemove = { onRemove(widget.id) },
                        onEvent = onEvent,
                    )
                }
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
                    y = 20f,
                    environmentId = 1L
                ),
                Widget(
                    type = WidgetType.TEXT,
                    label = "T1",
                    dataSource = DataSource.HTTP(url = "https://www.test.com/text/t1"),
                    value = "Hello word",
                    x = 580f,
                    y = 20f,
                    environmentId = 1L
                ),
                Widget(
                    type = WidgetType.SWITCH,
                    label = "Switch for M1",
                    dataSource = DataSource.MQTT(topic = "/switch/s1"),
                    value = "true",
                    x = 20f,
                    y = 500f,
                    environmentId = 1L
                ),
                Widget(
                    type = WidgetType.BUTTON,
                    label = "B1",
                    dataSource = DataSource.MQTT(topic = "/button/b1"),
                    value = "",
                    x = 580f,
                    y = 500f,
                    environmentId = 1L
                ),
                Widget(
                    type = WidgetType.PROGRESS_BAR,
                    label = "P1",
                    dataSource = DataSource.MQTT(topic = "/progress/p1"),
                    value = ".63",
                    x = 20f,
                    y = 1000f,
                    environmentId = 1L
                ),
                Widget(
                    type = WidgetType.SLIDER,
                    label = "L1",
                    dataSource = DataSource.HTTP(url = "http://www.test.com/slider/l1"),
                    value = "40.7",
                    x = 580f,
                    y = 1000f,
                    environmentId = 1L
                ),
                Widget(
                    type = WidgetType.LED_INDICATOR,
                    label = "LED1",
                    dataSource = DataSource.HTTP(url = "https://www.test.com/leds/l1"),
                    value = "WARN",
                    x = 20f,
                    y = 1500f,
                    environmentId = 1L
                ),
                Widget(
                    type = WidgetType.INPUT_FIELD,
                    label = "IP1",
                    dataSource = DataSource.HTTP(url = "https://www.test.com/input/ip1"),
                    value = "ON",
                    x = 580f,
                    y = 1500f,
                    environmentId = 1L
                )
            ),
            onDragEnd = { _, _, _ -> },
            onTogglePin = {},
            onNotify = {},
            onEdit = {},
            onRemove = {},
            onEvent = {},
            onTransformChange = { _, _ -> },
        )
    }
}