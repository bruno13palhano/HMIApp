package com.bruno13palhano.hmiapp.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bruno13palhano.hmiapp.R
import com.bruno13palhano.hmiapp.ui.shared.clickableWithoutRipple
import com.bruno13palhano.hmiapp.ui.shared.rememberFlowWithLifecycle

@Composable
fun SettingsScreen(
    onMenuIconClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.container.state.collectAsStateWithLifecycle()
    val sideEffect = rememberFlowWithLifecycle(flow = viewModel.container.sideEffect)

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        sideEffect.collect { effect ->
            when (effect) {
                SettingsSideEffect.ToggleMenu -> onMenuIconClick()
                SettingsSideEffect.HideKeyboardAndClearFocus -> {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
                is SettingsSideEffect.ShowSettingsInfo -> {
                    when (effect.connectionInfo) {
                        SettingsInfo.CLIENT_ID -> TODO()
                        SettingsInfo.HOST -> TODO()
                        SettingsInfo.PORT -> TODO()
                        SettingsInfo.USERNAME -> TODO()
                        SettingsInfo.PASSWORD -> TODO()
                        SettingsInfo.CONNECT_SUCCESS -> TODO()
                        SettingsInfo.CONNECT_FAILURE -> TODO()
                        SettingsInfo.DISCONNECT_SUCCESS -> TODO()
                        SettingsInfo.DISCONNECT_FAILURE -> TODO()
                    }
                }
            }
        }
    }

    SettingsContent(
        snackbarHostState = snackbarHostState,
        state = state,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    snackbarHostState: SnackbarHostState,
    state: SettingsState,
    onEvent: (event: SettingsEvent) -> Unit
) {
    Scaffold(
        modifier = Modifier
            .consumeWindowInsets(WindowInsets.safeDrawing)
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
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) {
        Column(modifier = Modifier.padding(it)) {

        }
    }
}