package com.bruno13palhano.hmiapp.ui.dashboard

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.bruno13palhano.core.model.Environment
import com.bruno13palhano.hmiapp.R
import com.bruno13palhano.hmiapp.ui.components.CircularProgress
import com.bruno13palhano.hmiapp.ui.components.DrawerMenu
import com.bruno13palhano.hmiapp.ui.components.EnvironmentInputDialog
import com.bruno13palhano.hmiapp.ui.components.ExpandableFab
import com.bruno13palhano.hmiapp.ui.components.ExpandableFabButtons
import com.bruno13palhano.hmiapp.ui.components.VertMenu
import com.bruno13palhano.hmiapp.ui.components.WidgetCanvas
import com.bruno13palhano.hmiapp.ui.components.WidgetInputDialog
import com.bruno13palhano.hmiapp.ui.components.WidgetToolbox
import com.bruno13palhano.hmiapp.ui.factory.ViewModelFactoryEntryPoint
import com.bruno13palhano.hmiapp.ui.factory.assistedViewModel
import com.bruno13palhano.hmiapp.ui.shared.rememberFlowWithLifecycle
import com.bruno13palhano.hmiapp.ui.theme.HMIAppTheme
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    navigateTo: (destination: NavKey) -> Unit,
    viewModel: DashboardViewModel = assistedViewModel(
        state = DashboardState(),
        entryPoint = ViewModelFactoryEntryPoint::class.java,
        factorySelector = { entryPoint, state ->
            entryPoint.dashboardViewModelFactory().create(state = state)
        }
    )
) {
    val state by viewModel.container.state.collectAsStateWithLifecycle()
    val sideEffect = rememberFlowWithLifecycle(flow = viewModel.container.sideEffect)

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val messagesInfo = getDashboardInfo()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.openOutputStream(it)?.let { stream ->
                viewModel.onEvent(event = DashboardEvent.ExportWidgetsConfig(stream = stream))
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.let { stream ->
                viewModel.onEvent(event = DashboardEvent.ImportWidgetsConfig(stream = stream))
            }
        }
    }

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
                    val currentInfo = messagesInfo[effect.info] ?: ""

                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = currentInfo,
                            withDismissAction = true
                        )
                    }
                }

                is DashboardSideEffect.NavigateTo -> navigateTo(effect.destination)

                DashboardSideEffect.LaunchExportWidgetsConfig -> {
                    exportLauncher.launch("layout_config.json")
                }

                DashboardSideEffect.LaunchImportWidgetsConfig -> {
                    importLauncher.launch(arrayOf("application/json"))
                }
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
    val items = mapOf(
        ConfigurationOptions.EXPORT to stringResource(id = R.string.export_config),
        ConfigurationOptions.IMPORT to stringResource(id = R.string.import_config)
    )

    val dashboardOptions = mapOf(
        DashboardOptions.ADD_ENVIRONMENT to ExpandableFabButtons(
            icon = Icons.Outlined.Add,
            label = stringResource(id = R.string.environment),
            contentDescription = stringResource(id = R.string.add_environment_button)
        ),
        DashboardOptions.WIDGETS to ExpandableFabButtons(
            icon = Icons.Outlined.Add,
            label = stringResource(id = R.string.widgets),
            contentDescription  = stringResource(id = R.string.widgets_options_button)
        )
    )

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
                },
                actions = {
                    IconButton(onClick = { onEvent(DashboardEvent.ToggleVertMenu)}) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = null
                        )

                        VertMenu(
                            items = items,
                            expanded = state.isVertMenuVisible,
                            onDismissRequest = { onEvent(DashboardEvent.ToggleVertMenu) },
                            onItemClick = { onEvent(DashboardEvent.OnVertMenuItemClick(item = it)) }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.environment.id != 0L && !state.loading) {
                ExpandableFab(
                    expanded = state.isDashboardOptionsExpanded,
                    items = dashboardOptions,
                    onClick = { onEvent(DashboardEvent.ToggleDashboardOptions) },
                    onOptionSelected = { item ->
                        when (item) {
                            DashboardOptions.ADD_ENVIRONMENT -> {
                                onEvent(DashboardEvent.OpenEnvironmentInputDialog(isEdit = false))
                            }
                            DashboardOptions.WIDGETS -> {
                                onEvent(DashboardEvent.ToggleIsToolboxExpanded)
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (state.environments.isNotEmpty()) {
                BottomAppBar {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(items = state.environments, key = { env -> env.id }) { env ->
                            Button(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .width(112.dp),
                                enabled = env.id != state.environment.id,
                                onClick = { onEvent(DashboardEvent.ChangeEnvironment(id = env.id)) }
                            ) {
                                Text(
                                    text = env.name,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
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
            Box(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
            ) {
                if (state.loading) {
                    CircularProgress(
                        modifier = Modifier
                            .padding(it)
                            .fillMaxSize()
                    )
                } else {
                    if (state.environment.id != 0L) {
                        WidgetCanvas(
                            widgets = state.widgets,
                            initialScale = state.environment.scale,
                            initialOffset = Offset(
                                x = state.environment.offsetX,
                                y = state.environment.offsetY
                            ),
                            onDragEnd = { id, x, y ->
                                onEvent(
                                    DashboardEvent.OnWidgetDragEnd(
                                        id = id,
                                        x = x,
                                        y = y
                                    )
                                )
                            },
                            onTogglePin = { id -> onEvent(DashboardEvent.OnToggleWidgetPin(id = id)) },
                            onEdit = { id -> onEvent(DashboardEvent.OpenEditWidgetDialog(id = id)) },
                            onRemove = { id -> onEvent(DashboardEvent.RemoveWidget(id = id)) },
                            onEvent = { widgetEvent ->
                                onEvent(DashboardEvent.OnWidgetEvent(widgetEvent = widgetEvent))
                            },
                            onTransformChange = { scale, offset ->
                                onEvent(
                                    DashboardEvent.OnUpdateCanvasState(
                                        scale = scale,
                                        offsetX = offset.x,
                                        offsetY = offset.y
                                    )
                                )
                            }
                        )
                    } else {
                        ExtendedFloatingActionButton(
                            modifier = Modifier.align(Alignment.Center),
                            onClick = { onEvent(DashboardEvent.OpenEnvironmentInputDialog(isEdit = false)) }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = stringResource(id = R.string.add_environment_button)
                            )
                            Text(text = stringResource(id = R.string.environment))
                        }
                    }

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
                            hasExtras = state.hasExtras,
                            extras = state.extras,
                            onLabelChange = { label ->
                                onEvent(DashboardEvent.UpdateLabel(label = label))
                            },
                            onEndpointChange = { endpoint ->
                                onEvent(DashboardEvent.UpdateEndpoint(endpoint = endpoint))
                            },
                            onExtraChange = { index, value ->
                                onEvent(DashboardEvent.UpdateExtra(index = index, value = value))
                            },
                            onAddExtra = { onEvent(DashboardEvent.AddExtra) },
                            onConfirm = { onEvent(DashboardEvent.ConfirmWidget) },
                            onDismissRequest = { onEvent(DashboardEvent.HideWidgetConfig) }
                        )
                    }

                    AnimatedVisibility(visible = state.isEnvironmentDialogVisible) {
                        EnvironmentInputDialog(
                            name = state.environment.name,
                            onNameChange = { name ->
                                onEvent(DashboardEvent.UpdateEnvironmentName(name = name))
                            },
                            onConfirm = {
                                if (state.isEditEnvironmentName) {
                                    onEvent(DashboardEvent.EditEnvironment)
                                } else {
                                    onEvent(DashboardEvent.AddEnvironment)
                                }
                            },
                            onDismissRequest = { onEvent(DashboardEvent.CloseEnvironmentInputDialog) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun getDashboardInfo(): Map<DashboardInfo, String> {
    return mapOf(
        DashboardInfo.DISCONNECTED to stringResource(id = R.string.disconnect_info),
        DashboardInfo.EXPORT_SUCCESS to stringResource(id = R.string.export_config_success),
        DashboardInfo.EXPORT_FAILURE to stringResource(id = R.string.export_config_error),
        DashboardInfo.IMPORT_SUCCESS to stringResource(id = R.string.import_config_success),
        DashboardInfo.IMPORT_FAILURE to stringResource(id = R.string.import_config_error)
    )
}

private enum class DashboardOptions {
    ADD_ENVIRONMENT,
    WIDGETS
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DashboardClosedPreview() {
    HMIAppTheme {
        DashboardContent(
            drawerState = DrawerState(initialValue = DrawerValue.Closed),
            snackbarHostState = SnackbarHostState(),
            state = DashboardState(loading = false),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DashboardClosedLoadingPreview() {
    HMIAppTheme {
        DashboardContent(
            drawerState = DrawerState(initialValue = DrawerValue.Closed),
            snackbarHostState = SnackbarHostState(),
            state = DashboardState(),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DashboardBottomMenuPreview() {
    HMIAppTheme {
        DashboardContent(
            drawerState = DrawerState(initialValue = DrawerValue.Closed),
            snackbarHostState = SnackbarHostState(),
            state = DashboardState(
                loading = false,
                environment = Environment(1L, "Home", 0f, 0f, 0f),
                environments = listOf(
                    Environment(1L, "Home", 0f, 0f, 0f),
                    Environment(2L, "Garden", 0f, 0f, 0f),
                    Environment(3L, "Farm", 0f, 0f, 0f)
                )
            ),
            onEvent = {}
        )
    }
}