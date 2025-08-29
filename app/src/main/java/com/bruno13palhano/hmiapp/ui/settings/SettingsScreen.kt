package com.bruno13palhano.hmiapp.ui.settings

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Button
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.bruno13palhano.hmiapp.R
import com.bruno13palhano.hmiapp.ui.components.CircularProgress
import com.bruno13palhano.hmiapp.ui.components.CustomIntegerField
import com.bruno13palhano.hmiapp.ui.components.CustomPasswordTextField
import com.bruno13palhano.hmiapp.ui.components.CustomTextField
import com.bruno13palhano.hmiapp.ui.components.DrawerMenu
import com.bruno13palhano.hmiapp.ui.components.VertMenu
import com.bruno13palhano.hmiapp.ui.factory.ViewModelFactoryEntryPoint
import com.bruno13palhano.hmiapp.ui.factory.assistedViewModel
import com.bruno13palhano.hmiapp.ui.shared.clickableWithoutRipple
import com.bruno13palhano.hmiapp.ui.shared.rememberFlowWithLifecycle
import com.bruno13palhano.hmiapp.ui.theme.HMIAppTheme
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    navigateTo: (destination: NavKey) -> Unit,
    viewModel: SettingsViewModel = assistedViewModel(
        state = SettingsState(),
        entryPoint = ViewModelFactoryEntryPoint::class.java,
        factorySelector = { entryPoint, state ->
            entryPoint.settingsViewModelFactory().create(state = state)
        }
    )
) {
    val state by viewModel.container.state.collectAsStateWithLifecycle()
    val sideEffect = rememberFlowWithLifecycle(flow = viewModel.container.sideEffect)

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val messagesInfo = getSettingInfo()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    LaunchedEffect(Unit) {
        viewModel.onEvent(event = SettingsEvent.CheckConnection)
    }

    LaunchedEffect(Unit) {
        sideEffect.collect { effect ->
            when (effect) {
                SettingsSideEffect.ToggleMenu -> {
                    scope.launch {
                        if (drawerState.isOpen) drawerState.close()
                        else drawerState.open()
                    }
                }

                SettingsSideEffect.HideKeyboardAndClearFocus -> {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
                is SettingsSideEffect.ShowSettingsInfo -> {
                    val currentInfo = messagesInfo[effect.info] ?: ""

                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = currentInfo,
                            withDismissAction = true
                        )
                    }
                }

                is SettingsSideEffect.NavigateTo -> navigateTo(effect.destination)
            }
        }
    }

    SettingsContent(
        drawerState = drawerState,
        snackbarHostState = snackbarHostState,
        state = state,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    drawerState: DrawerState,
    snackbarHostState: SnackbarHostState,
    state: SettingsState,
    onEvent: (event: SettingsEvent) -> Unit
) {
    val content = LocalContext.current

    val pickCaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            onEvent(SettingsEvent.LoadCA(caCert = content.uriToByteArray(uri = it), caUri = it))
        }
    }

    val pickClientLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            onEvent(
                SettingsEvent.LoadClientCert(
                    clientCert = content.uriToByteArray(uri = it),
                    clientP12Uri = it
                )
            )
        }
    }

    val pickClientKeyLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            onEvent(
                SettingsEvent.LoadClientKey(clientKey = content.uriToByteArray(uri = it))
            )
        }
    }

    val items = mapOf(
        "CA" to "Load CA",
        "CL" to "Load clientP12",
    )

    Scaffold(
        modifier = Modifier
            .clickableWithoutRipple { onEvent(SettingsEvent.HideKeyboardAndClearFocus) },
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(SettingsEvent.ToggleMenu) }) {
                        Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = stringResource(id = R.string.menu_button)
                        )
                    }
                },
                actions = {
                    var s by remember { mutableStateOf(false) }

                    IconButton(onClick = { s = true }) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = null
                        )

                        VertMenu(
                            items = items,
                            expanded = s,
                            onDismissRequest = { s = false },
                            onItemClick = { item ->
                                when (item) {
                                    "CA" -> pickCaLauncher.launch(arrayOf("*/*"))
                                    "CL" -> pickClientLauncher.launch(arrayOf("*/*"))
                                    "CK" -> pickClientKeyLauncher.launch(arrayOf("*/*"))
                                    else -> ""
                                }
                            }
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
            navigateTo = { key -> onEvent(SettingsEvent.NavigateTo(destination = key)) },
            gesturesEnabled = state.isGestureEnabled,
        ) {
            if (state.isLoading) {
                CircularProgress(
                    modifier = Modifier
                        .padding(it)
                        .fillMaxSize()
                )
            } else {
                Column(
                    modifier = Modifier
                        .padding(it)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    CustomTextField(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth(),
                        value = state.clientId,
                        onValueChange = { clientId ->
                            onEvent(SettingsEvent.UpdateClientId(clientId = clientId))
                        },
                        enabled = !state.isConnected,
                        label = stringResource(id = R.string.client_id),
                        placeholder = stringResource(id = R.string.client_id_placeholder),
                        isError = state.isClientIdInvalid
                    )

                    CustomTextField(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth(),
                        value = state.host,
                        onValueChange = { host ->
                            onEvent(SettingsEvent.UpdateHost(host = host))
                        },
                        enabled = !state.isConnected,
                        label = stringResource(id = R.string.host),
                        placeholder = stringResource(id = R.string.host_placeholder),
                        isError = state.isHostInvalid
                    )

                    CustomIntegerField(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth(),
                        value = state.port,
                        onValueChange = { port ->
                            onEvent(SettingsEvent.UpdatePort(port = port))
                        },
                        enabled = !state.isConnected,
                        label = stringResource(id = R.string.port),
                        placeholder = stringResource(id = R.string.port_placeholder),
                        isError = state.isPortInvalid
                    )

                    CustomTextField(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth(),
                        value = state.username,
                        onValueChange = { username ->
                            onEvent(SettingsEvent.UpdateUsername(username = username))
                        },
                        enabled = !state.isConnected,
                        label = stringResource(id = R.string.username),
                        placeholder = stringResource(id = R.string.username_placeholder),
                        isError = state.isUsernameInvalid
                    )

                    CustomPasswordTextField(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth(),
                        value = state.password,
                        visibility = state.passwordVisibility,
                        onValueChange = { password ->
                            onEvent(SettingsEvent.UpdatePassword(password = password))
                        },
                        togglePasswordVisibility = { onEvent(SettingsEvent.TogglePasswordVisibility) },
                        enabled = !state.isConnected,
                        label = stringResource(id = R.string.password),
                        placeholder = stringResource(id = R.string.password_placeholder),
                        isError = state.isPasswordInvalid
                    )

                    Column(
                        modifier = Modifier
                            .padding(vertical = 32.dp)
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Button(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .fillMaxWidth(),
                            onClick = { onEvent(SettingsEvent.ConnectMqtt) },
                            enabled = !state.isConnected
                        ) {
                            Text(text = stringResource(id = R.string.connect))
                        }

                        Button(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .fillMaxWidth(),
                            onClick = { onEvent(SettingsEvent.DisconnectMqtt) },
                            enabled = state.isConnected
                        ) {
                            Text(text = stringResource(id = R.string.disconnect))
                        }
                    }
                }
            }
        }
    }
}

fun Context.uriToByteArray(uri: Uri): ByteArray? {
    return contentResolver.openInputStream(uri)?.use { inputStream ->
        inputStream.readBytes()
    }
}

@Composable
private fun getSettingInfo(): Map<SettingsInfo, String> {
    return mapOf(
        SettingsInfo.CLIENT_ID to stringResource(R.string.client_id_error),
        SettingsInfo.HOST to stringResource(R.string.host_error),
        SettingsInfo.PORT to stringResource(R.string.port_error),
        SettingsInfo.USERNAME to stringResource(R.string.username_error),
        SettingsInfo.PASSWORD to stringResource(R.string.password_error),
        SettingsInfo.CONNECT_SUCCESS to stringResource(R.string.connect_success),
        SettingsInfo.CONNECT_FAILURE to stringResource(R.string.connect_error),
        SettingsInfo.DISCONNECT_SUCCESS to stringResource(R.string.disconnect_success),
        SettingsInfo.DISCONNECT_FAILURE to stringResource(R.string.disconnect_error),
    )
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SettingsPreview() {
    HMIAppTheme {
        SettingsContent(
            drawerState = DrawerState(initialValue = DrawerValue.Open),
            snackbarHostState = SnackbarHostState(),
            state = SettingsState(),
            onEvent = {}
        )
    }
}
