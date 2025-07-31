package com.bruno13palhano.hmiapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.bruno13palhano.hmiapp.ui.dashboard.DashboardScreen
import kotlinx.serialization.Serializable

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(Dashboard)

    NavDisplay(
        modifier = modifier,
        entryDecorators = listOf(
            rememberSceneSetupNavEntryDecorator(),
            rememberSavedStateNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() }
    ) { key ->
        when (key) {
            Dashboard -> NavEntry(key) { entry ->
                DashboardScreen()
            }
            else -> {
                error("Unknown route: $key")
            }
        }
    }
}

@Serializable
data object Dashboard : NavKey