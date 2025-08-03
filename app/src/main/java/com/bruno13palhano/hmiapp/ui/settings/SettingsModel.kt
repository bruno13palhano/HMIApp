package com.bruno13palhano.hmiapp.ui.settings

import androidx.compose.runtime.Immutable

@Immutable
data class SettingsState(
    val clientId: String = "AndroidClient_${System.currentTimeMillis()}",
    val host: String = "",
    val port: Int = 1883,
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isConnected: Boolean = false
)

@Immutable
sealed interface SettingsEvent {
    data class UpdateClientId(val clientId: String) : SettingsEvent
    data class UpdateHost(val host: String) : SettingsEvent
    data class UpdatePort(val port: Int) : SettingsEvent
    data class UpdateUsername(val username: String) : SettingsEvent
    data class UpdatePassword(val password: String) : SettingsEvent
    data object ToggleMenu : SettingsEvent
    data object ConnectMqtt : SettingsEvent
    data object DisconnectMqtt : SettingsEvent
    data object HideKeyboardAndClearFocus : SettingsEvent
}

@Immutable
sealed interface SettingsSideEffect {
    data object ToggleMenu : SettingsSideEffect
    data object HideKeyboardAndClearFocus : SettingsSideEffect
    data class ShowSettingsInfo(val connectionInfo: SettingsInfo) : SettingsSideEffect
}

enum class SettingsInfo {
    CLIENT_ID,
    HOST,
    PORT,
    USERNAME,
    PASSWORD,
    CONNECT_SUCCESS,
    CONNECT_FAILURE,
    DISCONNECT_SUCCESS,
    DISCONNECT_FAILURE,
}