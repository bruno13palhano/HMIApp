package com.bruno13palhano.hmiapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bruno13palhano.core.model.WidgetType
import com.bruno13palhano.hmiapp.R
import com.bruno13palhano.hmiapp.ui.theme.HMIAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetToolbox(
    expanded: Boolean,
    onAdd: (WidgetType) -> Unit,
    onExpandedClick: () -> Unit
) {
    if (expanded) {
        Column {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onExpandedClick,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .align(Alignment.Center)
                ) {
                    var contentDescription = stringResource(id = R.string.hide_widget_toolbox)
                    var icon = Icons.Outlined.ExpandLess

                    if (expanded) {
                        icon = Icons.Outlined.ExpandMore
                        contentDescription = stringResource(id = R.string.hide_widget_toolbox)
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = contentDescription,
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
            Column(
                modifier = Modifier
                    .offset(y = (-32).dp)
                    .padding(16.dp)
                    .background(
                        MaterialTheme.colorScheme.background,
                        RoundedCornerShape(5)
                    )
                    .border(1.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(5))
                    .padding(8.dp)
                    .sizeIn(maxHeight = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                WidgetType.entries.forEach { type ->
                    Button(
                        onClick = { onAdd(type) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text(text = "+ ${getWidgetTypeName(widgetType = type)}")
                    }
                }
            }
        }
    }
}

@Composable
fun getWidgetTypeName(widgetType: WidgetType): String {
    return when(widgetType) {
        WidgetType.TEXT -> stringResource(id = R.string.widget_text)
        WidgetType.BUTTON -> stringResource(id = R.string.widget_button)
        WidgetType.SWITCH -> stringResource(id = R.string.widget_switch)
        WidgetType.SLIDER -> stringResource(id = R.string.widget_slider)
        WidgetType.GAUGE -> stringResource(id = R.string.widget_gauge)
        WidgetType.PROGRESS_BAR -> stringResource(id = R.string.widget_progress_bar)
        WidgetType.CHART -> stringResource(id = R.string.widget_chart)
        WidgetType.TOGGLE_BUTTON -> stringResource(id = R.string.widget_toggle_button)
        WidgetType.INPUT_FIELD -> stringResource(id = R.string.widget_input_field)
        WidgetType.LED_INDICATOR -> stringResource(id = R.string.widget_led_indicator)
        WidgetType.DROPDOWN -> stringResource(id = R.string.widget_dropdown)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun WidgetToolboxPreview() {
    HMIAppTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
            WidgetToolbox(
                expanded = true,
                onAdd = {
                    listOf(WidgetType.TEXT, WidgetType.BUTTON, WidgetType.SWITCH)
                },
                onExpandedClick = {}
            )
        }
    }
}
