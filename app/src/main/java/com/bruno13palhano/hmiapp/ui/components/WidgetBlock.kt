package com.bruno13palhano.hmiapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bruno13palhano.core.model.DataSource
import com.bruno13palhano.core.model.Widget
import kotlin.math.roundToInt

@Composable
fun WidgetBlock(
    widget: Widget,
    onMove: (x: Float, y: Float) -> Unit,
    onRemove: () -> Unit
) {
    var offsetX by remember { mutableStateOf(widget.x) }
    var offsetY by remember { mutableStateOf(widget.y) }

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
//            .padding(8.dp)
            .size(widget.width.dp, widget.height.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = widget.label, fontWeight = FontWeight.Bold)
            Text(text = when (val ds = widget.dataSource) {
                is DataSource.MQTT -> "MQTT: ${ds.topic}"
                is DataSource.HTTP -> "HTTP: ${ds.url}"
            }, fontSize = 12.sp)
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove"
                )
            }
        }
    }
}