package com.bruno13palhano.hmiapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.bruno13palhano.core.data.repository.EnvironmentRepository
import com.bruno13palhano.core.data.repository.MqttClientRepository
import com.bruno13palhano.core.data.repository.WidgetRepository
import com.bruno13palhano.core.model.Environment
import com.bruno13palhano.hmiapp.ui.dashboard.DashboardScreen
import com.bruno13palhano.hmiapp.ui.dashboard.DashboardState
import com.bruno13palhano.hmiapp.ui.dashboard.DashboardViewModel
import com.bruno13palhano.hmiapp.ui.theme.HMIAppTheme
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DashboardScreenIntegrationTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var widgetRepository: WidgetRepository
    private lateinit var mqttRepository: MqttClientRepository
    private lateinit var environmentRepository: EnvironmentRepository

    @Before
    fun setup() {
        widgetRepository = mockk(relaxed = true)
        mqttRepository = mockk(relaxed = true)
        environmentRepository = mockk(relaxed = true)

        every { mqttRepository.isConnected() } returns flowOf(true)
        coEvery { environmentRepository.getLast() } returns Environment(
            id = 1L,
            name = "Home",
            scale = 1f,
            offsetX = 0f,
            offsetY = 0f
        )
        every { environmentRepository.getAll() } returns flowOf(
            listOf(
                Environment(1L, "Home", 1f, 0f, 0f),
                Environment(2L, "Farm", 1f, 0f, 0f)
            )
        )
    }

    private fun createViewModel(): DashboardViewModel {
        return DashboardViewModel(
            widgetRepository = widgetRepository,
            mqttClientRepository = mqttRepository,
            environmentRepository = environmentRepository,
            initialState = DashboardState()
        )
    }

    @Test
    fun whenNoEnvironment_showsAddEnvironmentFab_andAfter_showsWidgetCanvas() {
        val viewModel = createViewModel()

        composeRule.setContent {
            HMIAppTheme {
                DashboardScreen(
                    navigateTo = {},
                    viewModel = viewModel
                )
            }
        }

        composeRule.onNodeWithTag("AddEnvironmentFab")
            .assertIsDisplayed()
            .performClick()

        composeRule.onNodeWithText("Name").assertIsDisplayed()
    }

    @Test
    fun whenEnvironmentExists_showsEnvironmentSwitchingButtons() {
        val viewModel = createViewModel()

        composeRule.setContent {
            HMIAppTheme {
                DashboardScreen(
                    navigateTo = {},
                    viewModel = viewModel
                )
            }
        }

        composeRule.onNodeWithText("Home").assertIsDisplayed()
        composeRule.onNodeWithText("Farm").assertIsDisplayed()
    }
}
