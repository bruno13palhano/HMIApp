package com.bruno13palhano.hmiapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.bruno13palhano.core.model.DataSource
import com.bruno13palhano.core.model.Widget
import com.bruno13palhano.core.model.WidgetType
import com.bruno13palhano.hmiapp.R
import com.bruno13palhano.hmiapp.ui.theme.HMIAppTheme
import java.net.URI
import kotlin.math.roundToInt

@Composable
fun WidgetBlock(
    widget: Widget,
    onMove: (x: Float, y: Float) -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(widget.x) }
    var offsetY by remember { mutableFloatStateOf(widget.y) }

    var expandMenu by remember { mutableStateOf(false) }
    val items = mapOf(
        MenuOptions.EDIT to stringResource(id = R.string.edit),
        MenuOptions.REMOVE to stringResource(id = R.string.remove)
    )

    Column(
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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row (
            modifier = Modifier.width(widget.width.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 8.dp).weight(2f),
                text = widget.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(
                modifier = Modifier.weight(1f),
                onClick = { expandMenu = !expandMenu },
            ) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = null
                )

                VertMenu(
                    items = items,
                    expanded = expandMenu,
                    onDismissRequest = { expandMenu = !expandMenu},
                    onItemClick = { item ->
                        when (item) {
                            MenuOptions.EDIT -> onEdit()
                            MenuOptions.REMOVE -> onRemove()
                        }
                    }
                )
            }
        }
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
        Text(
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                .fillMaxWidth(),
            text = when (val dataSource = widget.dataSource) {
                is DataSource.HTTP -> extractEndpoint(url = dataSource.url)
                is DataSource.MQTT -> dataSource.topic
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            fontStyle = FontStyle.Italic
        )
    }
}

private fun extractEndpoint(url: String): String {
    try {
        val uri = URI(url)
        val path = uri.path ?: "/"
        val query = uri.query?.let {"?$it" } ?: ""
        val  fragment = uri.fragment?.let { "#it" } ?: ""
        return "$path$query$fragment"
    } catch (e: Exception) {
        e.printStackTrace()
        return "/"
    }
}

private enum class MenuOptions {
    EDIT,
    REMOVE
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun WidgetBlockPreview() {
    HMIAppTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            WidgetBlock(
                widget = Widget(
                    type = WidgetType.SWITCH,
                    label = "M1",
                    dataSource = DataSource.MQTT(topic = "test/topic"),
                    value = "ON"
                ),
                onMove = { _, _ -> },
                onEdit = { },
                onRemove = { },
                content = {}
            )
        }
    }
}