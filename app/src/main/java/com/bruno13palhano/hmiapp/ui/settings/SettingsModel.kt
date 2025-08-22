package com.bruno13palhano.hmiapp.ui.settings

import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import com.bruno13palhano.hmiapp.ui.navigation.Settings

@Immutable
data class SettingsState(
    val clientId: String = "bruno",
    val host: String = "192.168.1.113",
    val port: String = "8883",
    val username: String = "mqtt_user",
    val password: String = "34151430",
    val caCert: ByteArray? = null,
    val clientCert: ByteArray? = null,
    val clientKey: ByteArray? = null,
    val clientP12Uri: Uri? = null,
    val caUri: Uri? = null,
    val insecure: Boolean = false,
    val passwordVisibility: Boolean = false,
    val currentDestination: NavKey = Settings,
    val isGestureEnabled: Boolean = true,
    val isClientIdInvalid: Boolean = false,
    val isHostInvalid: Boolean = false,
    val isPortInvalid: Boolean = false,
    val isUsernameInvalid: Boolean = false,
    val isPasswordInvalid: Boolean = false,
    val isLoading: Boolean = false,
    val isConnected: Boolean = false
)

@Immutable
sealed interface SettingsEvent {
    data class UpdateClientId(val clientId: String) : SettingsEvent
    data class UpdateHost(val host: String) : SettingsEvent
    data class UpdatePort(val port: String) : SettingsEvent
    data class UpdateUsername(val username: String) : SettingsEvent
    data class UpdatePassword(val password: String) : SettingsEvent
    data object TogglePasswordVisibility : SettingsEvent
    data object CheckConnection : SettingsEvent
    data class NavigateTo(val destination: NavKey) : SettingsEvent
    data object ToggleMenu : SettingsEvent
    data object ConnectMqtt : SettingsEvent
    data object DisconnectMqtt : SettingsEvent
    data object HideKeyboardAndClearFocus : SettingsEvent
    data class LoadCA(val caCert: ByteArray?, val caUri: Uri?) : SettingsEvent
    data class LoadClientCert(val clientCert: ByteArray?, val clientP12Uri: Uri?) : SettingsEvent
    data class LoadClientKey(val clientKey: ByteArray?) : SettingsEvent
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