package com.bruno13palhano.hmiapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
            SettingsEvent.ConnectMqtt -> connectMqtt()
            SettingsEvent.DisconnectMqtt -> disconnectMqtt()
        }
    }

    private fun toggleMenu() = container.intent {
        postSideEffect(effect = SettingsSideEffect.ToggleMenu)
    }

    private fun hideKeyboardAndClearFocus() = container.intent {
        postSideEffect(effect = SettingsSideEffect.HideKeyboardAndClearFocus)
    }

    private fun updateClientId(clientId: String) = container.intent {
        reduce { copy(clientId = clientId) }
    }

    private fun updateHost(host: String) = container.intent {
        reduce { copy(host = host) }
    }

    private fun updatePort(port: Int) = container.intent {
        reduce { copy(port = port) }
    }

    private fun updateUsername(username: String) = container.intent {
        reduce { copy(username = username) }
    }

    private fun updatePassword(password: String) = container.intent {
        reduce { copy(password = password) }
    }

    private fun connectMqtt() = container.intent {
        val state = state.value

        mqttClientRepository.connectMqtt(
            clientId = state.clientId,
            host = state.host,
            port = state.port,
            username = state.username,
            password = state.password
        )
            .onSuccess {
                postSideEffect(
                    effect = SettingsSideEffect.ShowSettingsInfo(
                        connectionInfo = SettingsInfo.CONNECT_SUCCESS
                    )
                )
            }
            .onFailure {
                postSideEffect(
                    effect = SettingsSideEffect.ShowSettingsInfo(
                        connectionInfo = SettingsInfo.CONNECT_FAILURE
                    )
                )
            }
    }

    private fun disconnectMqtt() = container.intent {
        mqttClientRepository.disconnect()
            .onSuccess {
                postSideEffect(
                    effect = SettingsSideEffect.ShowSettingsInfo(
                        connectionInfo = SettingsInfo.DISCONNECT_SUCCESS
                    )
                )
            }
            .onFailure {
                postSideEffect(
                    effect = SettingsSideEffect.ShowSettingsInfo(
                        connectionInfo = SettingsInfo.DISCONNECT_FAILURE
                    )
                )
            }
    }
}