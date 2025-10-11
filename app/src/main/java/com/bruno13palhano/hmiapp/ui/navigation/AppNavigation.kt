package com.bruno13palhano.hmiapp.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
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
    navBackStack: NavBackStack = rememberNavBackStack(Dashboard),
) {
    NavDisplay(
        modifier = modifier,
        entryDecorators = listOf(
            rememberSceneSetupNavEntryDecorator(),
            rememberSavedStateNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        backStack = navBackStack,
        onBack = { navBackStack.removeLastOrNull() },
    ) { key ->
        when (key) {
            Dashboard -> NavEntry(key) { entry ->
                DashboardScreen(
                    navigateTo = { destination ->
                        if (key != destination) {
                            navBackStack.add(element = destination)
                        }
                    },
                )
            }

            Settings -> NavEntry(key) { entry ->
                SettingsScreen(
                    navigateTo = { destination ->
                        if (key != destination) {
                            if (destination == Dashboard) {
                                // Necessary to keep only one Dashboard screen on the stack
                                val size = navBackStack.size
                                navBackStack.removeRange(1, size)
                            } else {
                                navBackStack.add(element = destination)
                                navBackStack.remove(element = Settings)
                            }
                        }
                    },
                )
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
data object Settings : NavKey

sealed class Screen(val key: NavKey, val icon: ImageVector, @param:StringRes val resourceId: Int) {
    data object DashboardScreen : Screen(
        key = Dashboard,
        icon = Icons.Outlined.Dashboard,
        resourceId = R.string.dashboard,
    )

    data object SettingsScreen : Screen(
        key = Settings,
        icon = Icons.Outlined.Settings,
        resourceId = R.string.settings,
    )
}
