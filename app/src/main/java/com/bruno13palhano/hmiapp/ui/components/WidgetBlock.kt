package com.bruno13palhano.hmiapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bruno13palhano.core.model.DataSource
import com.bruno13palhano.core.model.Widget
import com.bruno13palhano.core.model.WidgetType
import com.bruno13palhano.hmiapp.R
import com.bruno13palhano.hmiapp.ui.theme.HMIAppTheme
import kotlin.math.roundToInt

@Composable
fun WidgetBlock(
    widget: Widget,
    onMove: (x: Float, y: Float) -> Unit,
    onRemove: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(widget.x) }
    var offsetY by remember { mutableFloatStateOf(widget.y) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                    onMove(offsetX, offsetY)
                }
            }
            .padding(8.dp)
            .size(widget.width.dp, widget.height.dp)
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(8.dp)
            )
            .border(1.dp, MaterialTheme.colorScheme.secondary),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = widget.label,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = when (val ds = widget.dataSource) {
                    is DataSource.MQTT -> "MQTT: ${ds.topic}"
                    is DataSource.HTTP -> "HTTP: ${ds.url}"
                },
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = "${stringResource(id = R.string.value)}: ${widget.value}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(id = R.string.remove)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun WidgetBlockPreview() {
    HMIAppTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            WidgetBlock(
                widget = Widget(
                    type = WidgetType.SWITCH,
                    label = "Switch",
                    dataSource = DataSource.MQTT(topic = "test/topic"),
                    value = "ON"
                ),
                onMove = { _, _ -> },
                onRemove = { }
            )

            WidgetBlock(
                widget = Widget(
                    type = WidgetType.TEXT,
                    label = "Gauss",
                    dataSource = DataSource.HTTP(url = "http://www.test.com/test"),
                    value = "120.2",
                    x = 250f,
                    y = 500f
                ),
                onMove = { _, _ -> },
                onRemove = { }
            )
        }
    }
}