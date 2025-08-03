package com.bruno13palhano.hmiapp.ui.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bruno13palhano.core.model.DataSource
import com.bruno13palhano.core.model.Widget
import com.bruno13palhano.hmiapp.R
import com.bruno13palhano.hmiapp.ui.components.WidgetCanvas
import com.bruno13palhano.hmiapp.ui.components.WidgetToolbox
import com.bruno13palhano.hmiapp.ui.shared.rememberFlowWithLifecycle
import com.bruno13palhano.hmiapp.ui.theme.HMIAppTheme

@Composable
fun DashboardScreen(
    onMenuIconClick: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.container.state.collectAsStateWithLifecycle()
    val sideEffect = rememberFlowWithLifecycle(flow = viewModel.container.sideEffect)

    LaunchedEffect(Unit) {
        sideEffect.collect { effect ->
            when (effect) {
                DashboardSideEffect.ToggleMenu -> onMenuIconClick()
            }
        }
    }

    DashboardContent(state = state, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    state: DashboardState,
    onEvent: (event: DashboardEvent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.dashboard)) },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = stringResource(id = R.string.menu_button)
                        )
                    }
                }
            )
        }
    ) {
        Box(modifier = Modifier.padding(it).fillMaxSize()) {
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
                            dataSource = DataSource.MQTT(topic = "test/topic")
                        )
                        onEvent(DashboardEvent.AddWidget(widget = newWidget))
                    }
                )
            }
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