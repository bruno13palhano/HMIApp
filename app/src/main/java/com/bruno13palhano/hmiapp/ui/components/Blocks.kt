package com.bruno13palhano.hmiapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bruno13palhano.core.model.Widget
import com.bruno13palhano.core.model.WidgetType

@Composable
fun WidgetRenderer(
    widget: Widget,
    onMove: (x: Float, y: Float) -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
    onEvent: (event: WidgetEvent) -> Unit,
) {
    when (widget.type) {
        WidgetType.TEXT -> TextWidget(widget, onMove, onEdit, onRemove)
        WidgetType.BUTTON -> ButtonWidget(widget, onMove, onEdit, onRemove) {
            onEvent(WidgetEvent.ButtonClicked(widget.id))
        }
        WidgetType.SWITCH -> SwitchWidget(widget, onMove, onEdit, onRemove) { state ->
            onEvent(WidgetEvent.SwitchToggled(widget.id, state))
        }
        WidgetType.SLIDER -> SliderWidget(widget, onMove, onEdit, onRemove) { value ->
            onEvent(WidgetEvent.SliderChanged(widget.id, value))
        }
        WidgetType.GAUGE -> GaugeWidget(widget, onMove, onEdit, onRemove)
        WidgetType.PROGRESS_BAR -> ProgressBarWidget(widget, onMove, onEdit, onRemove)
        WidgetType.IMAGE -> ImageWidget(widget, onMove, onEdit, onRemove)
        WidgetType.CHART -> ChartWidget(widget, onMove, onEdit, onRemove)
        WidgetType.TOGGLE_BUTTON -> ToggleButtonWidget(widget, onMove, onEdit, onRemove) { state ->
            onEvent(WidgetEvent.ToggleButtonChanged(widget.id, state))
        }
        WidgetType.INPUT_FIELD -> InputFieldWidget(widget, onMove, onEdit, onRemove) { text ->
            onEvent(WidgetEvent.InputSubmitted(widget.id, text))
        }
        WidgetType.LED_INDICATOR -> LedIndicatorWidget(widget, onMove, onEdit, onRemove)
        WidgetType.DROPDOWN -> DropdownWidget(widget, onMove, onEdit, onRemove) { selected ->
            onEvent(WidgetEvent.DropdownSelected(widget.id, selected))
        }
        WidgetType.COLOR_PICKER -> ColorPickerWidget(widget, onMove, onEdit, onRemove) { color ->
            onEvent(WidgetEvent.ColorPicked(widget.id, color))
        }
    }
}
sealed class WidgetEvent {
    data class ButtonClicked(val widgetId: String) : WidgetEvent()
    data class SwitchToggled(val widgetId: String, val state: Boolean) : WidgetEvent()
    data class SliderChanged(val widgetId: String, val value: Float) : WidgetEvent()
    data class ToggleButtonChanged(val widgetId: String, val state: Boolean) : WidgetEvent()
    data class InputSubmitted(val widgetId: String, val text: String) : WidgetEvent()
    data class DropdownSelected(val widgetId: String, val selected: String) : WidgetEvent()
    data class ColorPicked(val widgetId: String, val color: Color) : WidgetEvent()
}

@Composable
fun TextWidget(
    widget: Widget,
    onMove: (x: Float, y: Float) -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit
) {
    WidgetBlock(
        widget = widget,
        onMove = onMove,
        onEdit = onEdit,
        onRemove = onRemove
    ) {
        Text(
            modifier = Modifier
                .padding(8.dp),
            text = widget.value,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ButtonWidget(
    widget: Widget,
    onMove: (x: Float, y: Float) -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
    onClick: () -> Unit
) {
    WidgetBlock(
        widget = widget,
        onMove = onMove,
        onEdit = onEdit,
        onRemove = onRemove
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .width((widget.width / 2).dp)
                .height((widget.width / 2).dp)
        ) {}
    }
}

@Composable
fun SwitchWidget(
    widget: Widget,
    onMove: (x: Float, y: Float) -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    var checked by remember { mutableStateOf(widget.value.lowercase() == "true") }
    WidgetBlock(
        widget = widget,
        onMove = onMove,
        onEdit = onEdit,
        onRemove = onRemove
    ) {
        Switch(
            modifier = Modifier,
            checked = checked,
            onCheckedChange = {
                checked = it
                onToggle(it)
            }
        )
    }
}

@Composable
fun SliderWidget(
    widget: Widget,
    onMove: (x: Float, y: Float) -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
    onValueChange: (Float) -> Unit
) {
    var sliderValue by remember { mutableFloatStateOf(widget.value.toFloatOrNull() ?: 0f) }
    WidgetBlock(
        widget = widget,
        onMove = onMove,
        onEdit = onEdit,
        onRemove = onRemove
    ) {
        Text(
            text = sliderValue.toString()
        )
        Box {
            Slider(
                value = sliderValue,
                onValueChange = {
                    sliderValue = it
                    onValueChange(it)
                },
                valueRange = 0f..100f,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .width(widget.width.dp)
            )
        }
    }
}

@Composable
fun GaugeWidget(
    widget: Widget,
    onMove: (x: Float, y: Float) -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
) {
    val value = widget.value.toFloatOrNull() ?: 0f
    WidgetBlock(
        widget = widget,
        onMove = { x, y ->
            onMove(x, y)
        },
        onEdit = onEdit,
        onRemove = onRemove
    ) {
        Box {
            Canvas(
                modifier = Modifier
                    .size((widget.width / 1.75).dp)
            ) {
                drawArc(
                    color = Color.Green,
                    startAngle = 180f,
                    sweepAngle = value * 1.8f,
                    useCenter = false,
                    style = Stroke(width = 8f)
                )
            }
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = value.toString()
            )
        }
    }
}

@Composable
fun ProgressBarWidget(
    widget: Widget,
    onMove: (x: Float, y: Float) -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
) {
    val progress = widget.value.toFloatOrNull() ?: 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    )

    WidgetBlock(
        widget = widget,
        onMove = onMove,
        onEdit = onEdit,
        onRemove = onRemove
    ) {
        Box(modifier = Modifier.fillMaxHeight()) {
            Text(
                modifier = Modifier.align(Alignment.TopCenter),
                text = (progress * 100).toString()
            )
            LinearProgressIndicator(
                modifier = Modifier
                    .width(widget.width.dp)
                    .padding(8.dp)
                    .align(Alignment.Center),
                progress = { animatedProgress }
            )
        }
    }
}

@Composable
fun ImageWidget(
    widget: Widget,
    onMove: (x: Float, y: Float) -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
) {
    WidgetBlock(
        widget = widget,
        onMove = onMove,
        onEdit = onEdit,
        onRemove = onRemove
    ) {
//    AsyncImage(
//        model = widget.value,
//        contentDescription = widget.label,
//        modifier = Modifier
//            .width(widget.width.dp)
//            .height(widget.height.dp),
//        contentScale = ContentScale.Crop
//    )
    }
}

@Composable
fun ChartWidget(
    widget: Widget,
    onMove: (x: Float, y: Float) -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
) {
    WidgetBlock(
        widget = widget,
        onMove = onMove,
        onEdit = onEdit,
        onRemove = onRemove
    ) {
        Box(
            modifier = Modifier.background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text("Chart")
        }
    }
}

@Composable
fun ToggleButtonWidget(
    widget: Widget,
    onMove: (x: Float, y: Float) -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    var toggled by remember { mutableStateOf(widget.value == "true") }
    WidgetBlock(
        widget = widget,
        onMove = onMove,
        onEdit = onEdit,
        onRemove = onRemove
    ) {
        Button(
            onClick = {
                toggled = !toggled
                onToggle(toggled)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (toggled) Color.Green else Color.Red
            ),
            modifier = Modifier
                .width((widget.width / 2).dp)
                .height((widget.width / 2).dp)
        ) {
            val text = if (toggled) "ON" else "OFF"
            Text(text = text)
        }
    }
}

@Composable
fun InputFieldWidget(
    widget: Widget,
    onMove: (x: Float, y: Float) -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var text by remember { mutableStateOf(widget.value) }
    WidgetBlock(
        widget = widget,
        onMove = onMove,
        onEdit = onEdit,
        onRemove = onRemove
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .padding(horizontal = 8.dp),
            trailingIcon = {
                IconButton(onClick = { onSubmit(text) }) {
                    Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = "Send")
                }
            }
        )
    }
}

@Composable
fun LedIndicatorWidget(
    widget: Widget,
    onMove: (x: Float, y: Float) -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
) {
    val color = when (widget.value.lowercase()) {
        "on", "true", "1" -> Color.Green
        "warn" -> Color.Yellow
        "error", "off", "false", "0" -> Color.Red
        else -> Color.Gray
    }
    WidgetBlock(
        widget = widget,
        onMove = onMove,
        onEdit = onEdit,
        onRemove = onRemove
    ) {
        Box(modifier = Modifier.fillMaxHeight()) {
            Text(
                modifier = Modifier.align(Alignment.TopCenter),
                text = widget.value
            )
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size((widget.width / 3).dp)
                    .background(color, shape = CircleShape)
                    .align(Alignment.Center)

            )
        }
    }
}

@Composable
fun DropdownWidget(
    widget: Widget,
    onMove: (x: Float, y: Float) -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(widget.value) }
    WidgetBlock(
        widget = widget,
        onMove = onMove,
        onEdit = onEdit,
        onRemove = onRemove
    ) {
        Text(
            text = selected,
            modifier = Modifier
                .clickable { expanded = true }
                .padding(8.dp)
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            listOf("Option 1", "Option 2", "Option 3").forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        selected = option
                        expanded = false
                        onSelected(option)
                    }
                )
            }
        }
    }
}

@Composable
fun ColorPickerWidget(
    widget: Widget,
    onMove: (x: Float, y: Float) -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
    onPick: (Color) -> Unit
) {
    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Magenta)
    WidgetBlock(
        widget = widget,
        onMove = onMove,
        onEdit = onEdit,
        onRemove = onRemove
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            colors.forEach { c ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(c, shape = CircleShape)
                        .clickable { onPick(c) }
                )
            }
        }
    }
}
