package com.bruno13palhano.hmiapp.ui.navigation

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.bruno13palhano.hmiapp.R
import com.bruno13palhano.hmiapp.ui.dashboard.DashboardScreen
import kotlinx.coroutines.launch
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
                DashboardScreen()
            }

            Settings -> NavEntry(key) { entry ->

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
        icon = Icons.Outlined.Home,
        resourceId = R.string.app_name
    )

    data object SettingsScreen: Screen(
        key = Settings,
        icon = Icons.Outlined.Settings,
        resourceId = R.string.app_name
    )
}

@Composable
fun DrawerMenu(
    drawerState: DrawerState,
    navBackStack: NavBackStack,
    gesturesEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val orientation = LocalConfiguration.current.orientation
    val items = listOf(Screen.DashboardScreen, Screen.SettingsScreen)
    val scope = rememberCoroutineScope()
    val currentDestination = navBackStack.lastOrNull()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = gesturesEnabled,
        drawerContent = {
            ModalDrawerSheet(
                modifier = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    Modifier.fillMaxWidth(.78F)
                } else {
                    Modifier
                },
            ) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Spacer(modifier = Modifier.height(12.dp))
                    items.forEach { screen ->
                        NavigationDrawerItem(
                            shape = RoundedCornerShape(0, 50, 50, 0),
                            icon = { Icon(imageVector = screen.icon, contentDescription = null) },
                            label = { Text(text = stringResource(id = screen.resourceId)) },
                            selected = currentDestination == screen.key,
                            onClick = {
                                if (currentDestination != screen.key) {
                                    navBackStack.add(element = screen.key)
                                    if (currentDestination != Dashboard) {
                                        navBackStack.remove(element = currentDestination)
                                    }
                                }
                                scope.launch {
                                    drawerState.close()
                                }
                            },
                            modifier = Modifier
                                .padding(top = 4.dp, bottom = 4.dp, end = 8.dp)
                                .height(52.dp),
                        )
                    }
                }
            }
        },
        content = content
    )
}