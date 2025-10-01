package com.bruno13palhano.hmiapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bruno13palhano.core.model.Widget
import com.bruno13palhano.core.model.WidgetType
import com.bruno13palhano.hmiapp.ui.shared.clearFocusOnKeyboardDismiss

@Composable
fun WidgetRenderer(
    widget: Widget,
    onDragEnd: (x: Float, y: Float) -> Unit,
    onTogglePin: () -> Unit,
    onNotify: () -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
    onEvent: (event: WidgetEvent) -> Unit,
) {
    when (widget.type) {
        WidgetType.TEXT -> TextWidget(widget, onDragEnd, onTogglePin, onNotify, onEdit, onRemove)
        WidgetType.BUTTON -> ButtonWidget(widget, onDragEnd, onTogglePin, onNotify, onEdit, onRemove) {
            onEvent(WidgetEvent.ButtonClicked(widget))
        }
        WidgetType.SWITCH -> SwitchWidget(widget, onDragEnd, onTogglePin, onNotify, onEdit, onRemove) { state ->
            onEvent(WidgetEvent.SwitchToggled(widget, state))
        }
        WidgetType.SLIDER -> SliderWidget(widget, onDragEnd, onTogglePin, onNotify, onEdit, onRemove) { value ->
            onEvent(WidgetEvent.SliderChanged(widget, value))
        }
        WidgetType.GAUGE -> GaugeWidget(widget, onDragEnd, onTogglePin, onNotify, onEdit, onRemove)
        WidgetType.PROGRESS_BAR -> ProgressBarWidget(widget, onDragEnd, onTogglePin, onNotify, onEdit, onRemove)
        WidgetType.CHART -> ChartWidget(widget, onDragEnd, onTogglePin, onNotify, onEdit, onRemove)
        WidgetType.TOGGLE_BUTTON -> ToggleButtonWidget(widget,onDragEnd, onTogglePin, onNotify, onEdit, onRemove) { state ->
            onEvent(WidgetEvent.ToggleButtonChanged(widget, state))
        }
        WidgetType.INPUT_FIELD -> InputFieldWidget(widget, onDragEnd, onTogglePin, onNotify, onEdit, onRemove) { text ->
            onEvent(WidgetEvent.InputSubmitted(widget, text))
        }
        WidgetType.LED_INDICATOR -> LedIndicatorWidget(widget, onDragEnd, onTogglePin, onNotify, onEdit, onRemove)
        WidgetType.DROPDOWN -> DropdownWidget(widget, onDragEnd, onTogglePin, onNotify, onEdit, onRemove) { selected ->
            onEvent(WidgetEvent.DropdownSelected(widget, selected))
        }
    }
}
sealed class WidgetEvent {
    data class ButtonClicked(val widget: Widget) : WidgetEvent()
    data class SwitchToggled(val widget: Widget, val state: Boolean) : WidgetEvent()
    data class SliderChanged(val widget: Widget, val value: Float) : WidgetEvent()
    data class ToggleButtonChanged(val widget: Widget, val state: Boolean) : WidgetEvent()
    data class InputSubmitted(val widget: Widget, val text: String) : WidgetEvent()
    data class DropdownSelected(val widget: Widget, val selected: String) : WidgetEvent()
}

@Composable
fun TextWidget(
    widget: Widget,
    onDragEnd: (x: Float, y: Float) -> Unit,
    onTogglePin: () -> Unit,
    onNotify: () -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit
) {
    WidgetBlock(
        widget = widget,
        onDragEnd = onDragEnd,
        onTogglePin = onTogglePin,
        onNotify = onNotify,
        onEdit = onEdit,
        onRemove = onRemove
    ) {
        Text(
            modifier = Modifier
                .padding(8.dp),
            text = widget.value,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ButtonWidget(
    widget: Widget,
    onDragEnd: (x: Float, y: Float) -> Unit,
    onTogglePin: () -> Unit,
    onNotify: () -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
    onClick: () -> Unit
) {
    WidgetBlock(
        widget = widget,
        onDragEnd = onDragEnd,
        onTogglePin = onTogglePin,
        onNotify = onNotify,
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
    onDragEnd: (x: Float, y: Float) -> Unit,
    onTogglePin: () -> Unit,
    onNotify: () -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    val initialValue = widget.extras?.firstOrNull() ?: "true"
    var checked by remember { mutableStateOf(widget.value.lowercase() == initialValue) }
    WidgetBlock(
        widget = widget,
        onDragEnd = onDragEnd,
        onTogglePin = onTogglePin,
        onNotify = onNotify,
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
    onDragEnd: (x: Float, y: Float) -> Unit,
    onTogglePin: () -> Unit,
    onNotify: () -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
    onValueChange: (Float) -> Unit
) {
    var sliderValue by remember { mutableFloatStateOf(widget.value.toFloatOrNull() ?: 0f) }
    WidgetBlock(
        widget = widget,
        onDragEnd = onDragEnd,
        onTogglePin = onTogglePin,
        onNotify = onNotify,
        onEdit = onEdit,
        onRemove = onRemove
    ) {
        Text(
            text = sliderValue.toString(),
            style = MaterialTheme.typography.titleMedium
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
    onDragEnd: (x: Float, y: Float) -> Unit,
    onTogglePin: () -> Unit,
    onNotify: () -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
) {
    val value = widget.value.toFloatOrNull() ?: 0f
    WidgetBlock(
        widget = widget,
        onDragEnd = onDragEnd,
        onTogglePin = onTogglePin,
        onNotify = onNotify,
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
                text = value.toString(),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun ProgressBarWidget(
    widget: Widget,
    onDragEnd: (x: Float, y: Float) -> Unit,
    onTogglePin: () -> Unit,
    onNotify: () -> Unit,
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
        onDragEnd = onDragEnd,
        onTogglePin = onTogglePin,
        onNotify = onNotify,
        onEdit = onEdit,
        onRemove = onRemove
    ) {
        Box(modifier = Modifier.fillMaxHeight()) {
            Text(
                modifier = Modifier.align(Alignment.TopCenter),
                text = (progress * 100).toString(),
                style = MaterialTheme.typography.titleMedium
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
fun ChartWidget(
    widget: Widget,
    onDragEnd: (x: Float, y: Float) -> Unit,
    onTogglePin: () -> Unit,
    onNotify: () -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
) {
    WidgetBlock(
        widget = widget,
        onDragEnd = onDragEnd,
        onTogglePin = onTogglePin,
        onNotify = onNotify,
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
    onDragEnd: (x: Float, y: Float) -> Unit,
    onTogglePin: () -> Unit,
    onNotify: () -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    val initialValue = widget.extras?.firstOrNull() ?: "true"
    var toggled by remember { mutableStateOf(widget.value == initialValue) }
    WidgetBlock(
        widget = widget,
        onDragEnd = onDragEnd,
        onTogglePin = onTogglePin,
        onNotify = onNotify,
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
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun InputFieldWidget(
    widget: Widget,
    onDragEnd: (x: Float, y: Float) -> Unit,
    onTogglePin: () -> Unit,
    onNotify: () -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var text by remember { mutableStateOf(widget.value) }
    WidgetBlock(
        widget = widget,
        onDragEnd = onDragEnd,
        onTogglePin = onTogglePin,
        onNotify = onNotify,
        onEdit = onEdit,
        onRemove = onRemove
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .clearFocusOnKeyboardDismiss(),
            trailingIcon = {
                IconButton(onClick = { onSubmit(text) }) {
                    Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = "Send")
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { defaultKeyboardAction(ImeAction.Done) })
        )
    }
}

@Composable
fun LedIndicatorWidget(
    widget: Widget,
    onDragEnd: (x: Float, y: Float) -> Unit,
    onTogglePin: () -> Unit,
    onNotify: () -> Unit,
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
        onDragEnd = onDragEnd,
        onTogglePin = onTogglePin,
        onNotify = onNotify,
        onEdit = onEdit,
        onRemove = onRemove
    ) {
        Box(modifier = Modifier.fillMaxHeight()) {
            Text(
                modifier = Modifier.align(Alignment.TopCenter),
                text = widget.value,
                style = MaterialTheme.typography.titleMedium
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
    onDragEnd: (x: Float, y: Float) -> Unit,
    onTogglePin: () -> Unit,
    onNotify: () -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(widget.value) }
    WidgetBlock(
        widget = widget,
        onDragEnd = onDragEnd,
        onTogglePin = onTogglePin,
        onNotify = onNotify,
        onEdit = onEdit,
        onRemove = onRemove
    ) {
        Text(
            text = selected,
            modifier = Modifier
                .clickable { expanded = true }
                .padding(8.dp),
            style = MaterialTheme.typography.titleMedium
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            widget.extras?.let {
                it.forEach { option ->
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
}
