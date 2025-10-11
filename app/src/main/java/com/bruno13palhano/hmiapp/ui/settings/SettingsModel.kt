package com.bruno13palhano.hmiapp.ui.settings

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import com.bruno13palhano.hmiapp.ui.navigation.Settings

@Immutable
data class SettingsState(
    val clientId: String = "",
    val host: String = "",
    val port: String = "",
    val username: String = "",
    val password: String = "",
    val p12Password: String = "",
    val caCert: ByteArray? = null,
    val clientP12: ByteArray? = null,
    val insecure: Boolean = false,
    val passwordVisibility: Boolean = false,
    val p12PasswordVisibility: Boolean = false,
    val currentDestination: NavKey = Settings,
    val isGestureEnabled: Boolean = true,
    val isClientIdInvalid: Boolean = false,
    val isHostInvalid: Boolean = false,
    val isPortInvalid: Boolean = false,
    val isLoading: Boolean = false,
    val isConnected: Boolean = false,
    val isCredentialDialogOpen: Boolean = false,
)

@Immutable
sealed interface SettingsEvent {
    data class UpdateClientId(val clientId: String) : SettingsEvent
    data class UpdateHost(val host: String) : SettingsEvent
    data class UpdatePort(val port: String) : SettingsEvent
    data class UpdateUsername(val username: String) : SettingsEvent
    data class UpdatePassword(val password: String) : SettingsEvent
    data class UpdateP12Password(val p12Password: String) : SettingsEvent
    data object TogglePasswordVisibility : SettingsEvent
    data object ToggleP12PasswordVisibility : SettingsEvent
    data object CheckConnection : SettingsEvent
    data class NavigateTo(val destination: NavKey) : SettingsEvent
    data object ToggleMenu : SettingsEvent
    data object ConnectMqtt : SettingsEvent
    data object DisconnectMqtt : SettingsEvent
    data object HideKeyboardAndClearFocus : SettingsEvent
    data object ToggleCredentialDialog : SettingsEvent
    data class LoadCA(val caCert: ByteArray?) : SettingsEvent
    data class LoadClientCert(val clientCert: ByteArray?) : SettingsEvent
}

@Immutable
sealed interface SettingsSideEffect {
    data object ToggleMenu : SettingsSideEffect
    data object HideKeyboardAndClearFocus : SettingsSideEffect
    data class ShowSettingsInfo(val info: SettingsInfo) : SettingsSideEffect
    data class NavigateTo(val destination: NavKey) : SettingsSideEffect
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
