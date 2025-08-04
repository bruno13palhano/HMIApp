package com.bruno13palhano.hmiapp.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.bruno13palhano.hmiapp.R
import com.bruno13palhano.hmiapp.ui.dashboard.DashboardScreen
import com.bruno13palhano.hmiapp.ui.settings.SettingsScreen
import kotlinx.serialization.Serializable

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navBackStack: NavBackStack,
    onGesturesEnable: (enable: Boolean) -> Unit,
    onMenuIconClick: () -> Unit
) {
    NavDisplay(
        modifier = modifier,
        entryDecorators = listOf(
            rememberSceneSetupNavEntryDecorator(),
            rememberSavedStateNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        backStack = navBackStack,
        onBack = { navBackStack.removeLastOrNull() }
    ) { key ->
        when (key) {
            Dashboard -> NavEntry(key) { entry ->
                DashboardScreen(onMenuIconClick = onMenuIconClick)
            }
            Settings -> NavEntry(key) { entry ->
                SettingsScreen(onMenuIconClick = onMenuIconClick)
            }
            else -> {
                error("Unknown route: $key")
            }
        }
    }
}

@Serializable
data object Dashboard : NavKey

@Serializable
data object Settings :  NavKey

sealed class Screen(
    val key: NavKey,
    val icon: ImageVector,
    @StringRes val resourceId: Int,
) {
    data object DashboardScreen : Screen(
        key = Dashboard,
        icon = Icons.Outlined.Dashboard,
        resourceId = R.string.dashboard
    )

    data object SettingsScreen: Screen(
        key = Settings,
        icon = Icons.Outlined.Settings,
        resourceId = R.string.settings
    )
}