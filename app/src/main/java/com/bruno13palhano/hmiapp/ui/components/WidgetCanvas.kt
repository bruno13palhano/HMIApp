package com.bruno13palhano.hmiapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.bruno13palhano.core.model.Widget

@Composable
fun WidgetCanvas(
    widgets: List<Widget>,
    onMove: (id: String, x: Float, y: Float) -> Unit,
    onRemove: (String) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize().background(Color.LightGray)) {
        widgets.forEach { widget ->
            WidgetBlock(
                widget = widget,
                onMove = { x, y -> onMove(widget.id, x, y) },
                onRemove = { onRemove(widget.id) }
            )
        }
    }
}