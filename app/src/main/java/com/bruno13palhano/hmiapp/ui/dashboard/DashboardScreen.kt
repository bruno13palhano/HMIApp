package com.bruno13palhano.hmiapp.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.bruno13palhano.hmiapp.R
import com.bruno13palhano.hmiapp.ui.components.DrawerMenu
import com.bruno13palhano.hmiapp.ui.components.WidgetCanvas
import com.bruno13palhano.hmiapp.ui.components.WidgetInputDialog
import com.bruno13palhano.hmiapp.ui.components.WidgetToolbox
import com.bruno13palhano.hmiapp.ui.shared.rememberFlowWithLifecycle
import com.bruno13palhano.hmiapp.ui.theme.HMIAppTheme
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    navigateTo: (destination: NavKey) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.container.state.collectAsStateWithLifecycle()
    val sideEffect = rememberFlowWithLifecycle(flow = viewModel.container.sideEffect)

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val disconnectedInfo = stringResource(id = R.string.disconnect_info)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    LaunchedEffect(Unit) {
        viewModel.onEvent(event = DashboardEvent.Init)
    }

    LaunchedEffect(Unit) {
        sideEffect.collect { effect ->
            when (effect) {
                DashboardSideEffect.ToggleMenu -> {
                    scope.launch {
                        if (drawerState.isOpen) drawerState.close()
                        else drawerState.open()
                    }
                }

                is DashboardSideEffect.ShowInfo -> {
                    when (effect.info) {
                        DashboardInfo.DISCONNECTED -> {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = disconnectedInfo,
                                    withDismissAction = true
                                )
                            }
                        }
                    }
                }

                is DashboardSideEffect.NavigateTo -> navigateTo(effect.destination)
            }
        }
    }

    DashboardContent(
        drawerState = drawerState,
        snackbarHostState = snackbarHostState,
        state = state,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    drawerState: DrawerState,
    snackbarHostState: SnackbarHostState,
    state: DashboardState,
    onEvent: (event: DashboardEvent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.dashboard)) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(DashboardEvent.ToggleMenu) }) {
                        Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = stringResource(id = R.string.menu_button)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) {
        DrawerMenu(
            modifier = Modifier.padding(it),
            currentKey = state.currentDestination,
            drawerState = drawerState,
            navigateTo = { key -> onEvent(DashboardEvent.NavigateTo(destination = key)) },
            gesturesEnabled = state.isGestureEnabled,
        ) {
            Box(modifier = Modifier.padding(it).fillMaxSize()) {
                WidgetCanvas(
                    widgets = state.widgets,
                    onMove = { id, x, y ->
                        onEvent(
                            DashboardEvent.MoveWidget(
                                id = id,
                                x = x,
                                y = y
                            )
                        )
                    },
                    onRemove = { id -> onEvent(DashboardEvent.RemoveWidget(id = id)) }
                )
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    WidgetToolbox(
                        expanded = state.isToolboxExpanded,
                        onExpandedClick = { onEvent(DashboardEvent.ToggleIsToolboxExpanded) },
                        onAdd = { type -> onEvent(DashboardEvent.ShowWidgetDialog(type = type)) }
                    )
                }

                AnimatedVisibility(visible = state.isWidgetInputDialogVisible) {
                    WidgetInputDialog(
                        label = state.label,
                        endpoint = state.endpoint,
                        onLabelChange = { label -> onEvent(DashboardEvent.UpdateLabel(label = label)) },
                        onEndpointChange = { endpoint ->
                            onEvent(DashboardEvent.UpdateEndpoint(endpoint = endpoint))
                        },
                        onConfirm = { onEvent(DashboardEvent.AddWidget) },
                        onDismissRequest = { onEvent(DashboardEvent.HideWidgetConfig) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DashboardPreview() {
    HMIAppTheme {
        DashboardContent(
            drawerState = DrawerState(initialValue = DrawerValue.Open),
            snackbarHostState = SnackbarHostState(),
            state = DashboardState(),
            onEvent = {}
        )
    }
}