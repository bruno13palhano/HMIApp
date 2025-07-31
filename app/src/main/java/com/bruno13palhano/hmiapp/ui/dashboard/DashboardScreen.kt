package com.bruno13palhano.hmiapp.ui.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bruno13palhano.core.model.DataSource
import com.bruno13palhano.core.model.Widget
import com.bruno13palhano.hmiapp.ui.components.WidgetCanvas
import com.bruno13palhano.hmiapp.ui.components.WidgetToolbox
import com.bruno13palhano.hmiapp.ui.theme.HMIAppTheme

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val state by viewModel.container.state.collectAsStateWithLifecycle()

    DashboardContent(state = state, onEvent = viewModel::onEvent)
}

@Composable
fun DashboardContent(
    state: DashboardState,
    onEvent: (event: DashboardEvent) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        WidgetCanvas(
            widgets = state.widgets,
            onMove = { id, x, y -> onEvent(DashboardEvent.MoveWidget(id = id, x = x, y = y)) },
            onRemove = { id -> onEvent(DashboardEvent.RemoveWidget(id = id)) }
        )
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            WidgetToolbox(
                onAdd = { type ->
                    val newWidget = Widget(
                        type = type,
                        label = type.name,
                        dataSource = DataSource.MQTT(topic = "demo/topic")
                    )
                    onEvent(DashboardEvent.AddWidget(widget = newWidget))
                }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DashboardPreview() {
    HMIAppTheme {
        DashboardContent(state = DashboardState(), onEvent = {})
    }
}