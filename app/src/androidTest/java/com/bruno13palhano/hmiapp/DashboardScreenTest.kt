package com.bruno13palhano.hmiapp

import androidx.activity.ComponentActivity
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.bruno13palhano.core.model.Environment
import com.bruno13palhano.hmiapp.ui.dashboard.DashboardContent
import com.bruno13palhano.hmiapp.ui.dashboard.DashboardState
import com.bruno13palhano.hmiapp.ui.theme.HMIAppTheme
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Rule
import org.junit.Test

class DashboardScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val testScope = CoroutineScope(Dispatchers.Main)

    @After
    fun tearDown() {
        testScope.cancel()
    }

    @Test
    fun dashboardScreen_showScaffold() {
        composeTestRule.setContent {
            HMIAppTheme {
                DashboardContent(
                    drawerState = DrawerState(initialValue = DrawerValue.Closed),
                    snackbarHostState = SnackbarHostState(),
                    state = DashboardState(),
                    onEvent = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("DashboardScreen").assertIsDisplayed()
    }

    @Test
    fun whenLoading_showCircularProgress() {
        composeTestRule.setContent {
            HMIAppTheme {
                DashboardContent(
                    drawerState = DrawerState(DrawerValue.Closed),
                    snackbarHostState = SnackbarHostState(),
                    state = DashboardState(loading = true),
                    onEvent = {},
                )
            }
        }

        composeTestRule.onNodeWithTag("CircularProgress").assertIsDisplayed()
        composeTestRule.onNodeWithTag("WidgetCanvas").assertDoesNotExist()
    }

    @Test
    fun snackbar_shows_on_message() {
        val snackbarHostState = SnackbarHostState()

        composeTestRule.setContent {
            HMIAppTheme {
                DashboardContent(
                    drawerState = DrawerState(DrawerValue.Closed),
                    snackbarHostState = snackbarHostState,
                    state = DashboardState(),
                    onEvent = {},
                )
            }
        }

        composeTestRule.runOnUiThread {
            testScope.launch {
                snackbarHostState.showSnackbar("Test message")
            }
        }

        composeTestRule.onNodeWithText("Test message").assertIsDisplayed()
    }

    @Test
    fun drawer_opens_when_menu_button_clicked() {
        val drawerState = DrawerState(DrawerValue.Closed)

        composeTestRule.setContent {
            HMIAppTheme {
                DashboardContent(
                    drawerState = drawerState,
                    snackbarHostState = SnackbarHostState(),
                    state = DashboardState(),
                    onEvent = {},
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Menu button").performClick()

        composeTestRule.runOnUiThread {
            testScope.launch {
                assertThat(drawerState.isOpen).isTrue()
            }
        }
    }

    @Test
    fun fab_shows_options_when_expanded() {
        val state = DashboardState(
            loading = false,
            environment = Environment(1L, "Home", 0f, 0f, 0f),
            isDashboardOptionsExpanded = true,
        )

        composeTestRule.setContent {
            HMIAppTheme {
                DashboardContent(
                    drawerState = DrawerState(DrawerValue.Closed),
                    snackbarHostState = SnackbarHostState(),
                    state = state,
                    onEvent = {},
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Add environment button").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Widgets options button").assertIsDisplayed()
    }

    @Test
    fun bottomAppBar_shows_environments() {
        val environments = listOf(
            Environment(1L, "Home", 0f, 0f, 0f),
            Environment(2L, "Garden", 0f, 0f, 0f),
        )
        val state = DashboardState(
            loading = false,
            environment = environments.first(),
            environments = environments,
        )

        composeTestRule.setContent {
            HMIAppTheme {
                DashboardContent(
                    drawerState = DrawerState(DrawerValue.Closed),
                    snackbarHostState = SnackbarHostState(),
                    state = state,
                    onEvent = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Home").assertIsDisplayed()
        composeTestRule.onNodeWithText("Garden").assertIsDisplayed()
    }

    @Test
    fun widgetDialog_shows_when_visible() {
        val state = DashboardState(isWidgetInputDialogVisible = true, loading = false)

        composeTestRule.setContent {
            HMIAppTheme {
                DashboardContent(
                    drawerState = DrawerState(DrawerValue.Closed),
                    snackbarHostState = SnackbarHostState(),
                    state = state,
                    onEvent = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Widget Configuration").assertIsDisplayed()
    }
}
