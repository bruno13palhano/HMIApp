package com.bruno13palhano.hmiapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import com.bruno13palhano.core.data.repository.MqttClientRepository
import com.bruno13palhano.hmiapp.ui.shared.Container
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val mqttClientRepository: MqttClientRepository
) : ViewModel() {
    val container: Container<SettingsState, SettingsSideEffect> = Container(
        initialSTATE = SettingsState(),
        scope = viewModelScope
    )

    fun onEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.ToggleMenu -> toggleMenu()
            SettingsEvent.HideKeyboardAndClearFocus -> hideKeyboardAndClearFocus()
            is SettingsEvent.UpdateClientId -> updateClientId(clientId = event.clientId)
            is SettingsEvent.UpdateHost -> updateHost(host = event.host)
            is SettingsEvent.UpdatePort -> updatePort(port = event.port)
            is SettingsEvent.UpdateUsername -> updateUsername(username = event.username)
            is SettingsEvent.UpdatePassword -> updatePassword(password = event.password)
            SettingsEvent.CheckConnection -> checkConnection()
            SettingsEvent.TogglePasswordVisibility -> togglePasswordVisibility()
            SettingsEvent.ConnectMqtt -> connectMqtt()
            SettingsEvent.DisconnectMqtt -> disconnectMqtt()
            is SettingsEvent.NavigateTo -> navigateTo(destination = event.destination)
        }
    }

    private fun navigateTo(destination: NavKey) = container.intent {
        postSideEffect(effect = SettingsSideEffect.NavigateTo(destination = destination))
    }

    private fun toggleMenu() = container.intent {
        postSideEffect(effect = SettingsSideEffect.ToggleMenu)
    }

    private fun hideKeyboardAndClearFocus() = container.intent {
        postSideEffect(effect = SettingsSideEffect.HideKeyboardAndClearFocus)
    }

    private fun updateClientId(clientId: String) = container.intent {
        val isInvalid = clientId.isBlank()
        reduce { copy(clientId = clientId, isClientIdInvalid = isInvalid) }
    }

    private fun updateHost(host: String) = container.intent {
        val isInvalid = host.isBlank()
        reduce { copy(host = host, isHostInvalid = isInvalid) }
    }

    private fun updatePort(port: String) = container.intent {
        val isInvalid = port.isBlank()
        reduce { copy(port = port, isPortInvalid = isInvalid) }
    }

    private fun updateUsername(username: String) = container.intent {
        val isInvalid = username.isBlank()
        reduce { copy(username = username, isUsernameInvalid = isInvalid) }
    }

    private fun updatePassword(password: String) = container.intent {
        val isInvalid = password.isBlank()
        reduce { copy(password = password, isPasswordInvalid = isInvalid) }
    }

    private fun togglePasswordVisibility() = container.intent {
        reduce { copy(passwordVisibility = !passwordVisibility) }
    }

    private fun checkConnection() = container.intent {
        mqttClientRepository.isConnected().collect {
            reduce { copy(isConnected = it) }
        }
    }

    private fun connectMqtt() = container.intent {
        val state = state.value
        if (isMqttPropertiesEmpty(state = state) || state.isConnected) {
            return@intent
        }

        reduce { copy(isLoading = true) }

        mqttClientRepository.connectMqtt(
            clientId = state.clientId,
            host = state.host,
            port = state.port.toInt(),
            username = state.username,
            password = state.password
        )
            .onSuccess {
                reduce { copy(isLoading = false) }
                postSideEffect(
                    effect = SettingsSideEffect.ShowSettingsInfo(
                        info = SettingsInfo.CONNECT_SUCCESS
                    )
                )
            }
            .onFailure {
                reduce { copy(isLoading = false) }
                postSideEffect(
                    effect = SettingsSideEffect.ShowSettingsInfo(
                        info = SettingsInfo.CONNECT_FAILURE
                    )
                )
            }
    }

    private fun disconnectMqtt() = container.intent {
        mqttClientRepository.disconnect()
            .onSuccess {
                postSideEffect(
                    effect = SettingsSideEffect.ShowSettingsInfo(
                        info = SettingsInfo.DISCONNECT_SUCCESS
                    )
                )
            }
            .onFailure {
                postSideEffect(
                    effect = SettingsSideEffect.ShowSettingsInfo(
                        info = SettingsInfo.DISCONNECT_FAILURE
                    )
                )
            }
    }

    private fun isMqttPropertiesEmpty(state: SettingsState): Boolean {
        return state.clientId.isBlank() || state.host.isBlank() || state.port.isBlank() ||
                state.username.isBlank() || state.password.isBlank()
    }
}