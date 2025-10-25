package com.bruno13palhano.hmiapp

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bruno13palhano.core.data.network.MqttConnectionConfig
import com.bruno13palhano.core.data.repository.EnvironmentRepository
import com.bruno13palhano.core.data.repository.MqttClientRepository
import com.bruno13palhano.core.data.repository.WidgetRepository
import com.bruno13palhano.core.model.DataSource
import com.bruno13palhano.core.model.Environment
import com.bruno13palhano.core.model.Widget
import com.bruno13palhano.core.model.WidgetType
import com.bruno13palhano.hmiapp.ui.dashboard.DashboardScreen
import com.bruno13palhano.hmiapp.ui.dashboard.DashboardState
import com.bruno13palhano.hmiapp.ui.dashboard.DashboardViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardScreenT {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private lateinit var fakeWidgetRepository: FakeWidgetRepository
    private lateinit var fakeEnvironmentRepository: FakeEnvironmentRepository
    private lateinit var fakeMqttClientRepository: FakeMqttClientRepository

    @Before
    fun setup() {
        fakeWidgetRepository = FakeWidgetRepository()
        fakeEnvironmentRepository = FakeEnvironmentRepository()
        fakeMqttClientRepository = FakeMqttClientRepository()
    }

    @After
    fun tearsDown() {
        fakeEnvironmentRepository.environments.clear()
        fakeWidgetRepository.fakeWidgets.clear()
    }

    private fun launchDashboardScreen() {
        val viewModel = DashboardViewModel(
            widgetRepository = fakeWidgetRepository,
            mqttClientRepository = fakeMqttClientRepository,
            environmentRepository = fakeEnvironmentRepository,
            initialState = DashboardState(loading = false)
        )

        composeRule.activity.setContent {
            DashboardScreen(
                navigateTo = {},
                viewModel = viewModel
            )
        }
    }

    @Test
    fun dashboard_renders_topAppBar_title() {
        launchDashboardScreen()

        composeRule.onAllNodesWithText(composeRule.activity.getString(R.string.dashboard))
            .onLast()
            .assertIsDisplayed()
    }

    @Test
    fun bottomBar_displays_environment_buttons() {
        launchDashboardScreen()

        composeRule.onNodeWithText("Home").assertIsDisplayed()
        composeRule.onNodeWithText("Garden").assertIsDisplayed()
        composeRule.onNodeWithText("Farm").assertIsDisplayed()
    }

    @Test
    fun clicking_environment_button_triggers_change() {
        launchDashboardScreen()

        composeRule.onNodeWithText("Garden").performClick()
        composeRule.onNodeWithText("Garden").assertIsDisplayed()
    }

    @Test
    fun open_environment_input_dialog() {
        fakeEnvironmentRepository.environments.clear()
        fakeWidgetRepository.fakeWidgets.clear()
        launchDashboardScreen()

        composeRule.onNodeWithContentDescription(
            composeRule.activity.getString(R.string.add_environment_button),
            useUnmergedTree = true
        ).performClick()

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.name))
            .assertExists()
    }

    @Test
    fun open_add_widget_input_dialog() {
        launchDashboardScreen()

        composeRule.onNodeWithTag("DashboardFab").performClick()

        composeRule.onNodeWithText("Widget").performClick()
        composeRule.onNodeWithText("Text").performClick()

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.widget_configuration)
        ).assertExists()
    }

    @Test
    fun open_edit_widget_input_dialog() {
        launchDashboardScreen()

        fakeWidgetRepository.fakeWidgets.add(
            Widget(
                id = "1",
                type = WidgetType.TEXT,
                label = "Temp",
                dataSource = DataSource.MQTT(topic = "test/topic"),
                environmentId = 1L
            )
        )

        composeRule.onNodeWithTag("WidgetMoreVert").performClick()

        composeRule.onNodeWithText("Edit").performClick()

//        composeRule.onAllNodesWithContentDescription(
//            composeRule.activity.getString(R.string.widgets_options_button)
//        ).onFirst().performClick()

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.widget_configuration)
        ).assertExists()
    }
}

class FakeEnvironmentRepository : EnvironmentRepository {
    val environments = mutableListOf(
        Environment(1L, "Home", 1f, 0f, 0f),
        Environment(2L, "Garden", 1f, 0f,0f),
        Environment(3L, "Farm", 1f, 0f,0f),
    )

    override suspend fun insert(environment: Environment) {}

    override suspend fun update(environment: Environment) {}

    override suspend fun deleteById(id: Long) {}

    override fun getAll(): Flow<List<Environment>> = flowOf(environments)

    override suspend fun getById(id: Long): Environment? = environments.firstOrNull { it.id == id }

    override suspend fun getLast(): Environment? = environments.lastOrNull()

    override suspend fun getLastEnvironmentId(): Long? = environments.lastOrNull()?.id

    override suspend fun setLastEnvironmentId(id: Long) {}
}

class FakeWidgetRepository : WidgetRepository {
    val fakeWidgets = mutableListOf<Widget>()

    override suspend fun getWidgets(environmentId: Long): List<Widget> = fakeWidgets

    override suspend fun insert(widget: Widget) {
        fakeWidgets.add(widget)
    }

    override suspend fun update(widget: Widget) {}

    override suspend fun deleteById(id: String) {
        fakeWidgets.removeIf { it.id == id }
    }

    override suspend fun updatePosition(id: String, x: Float, y: Float) {}
}

class FakeMqttClientRepository : MqttClientRepository {
    private val messages = MutableSharedFlow<Pair<String, String>>(replay = 1)
    override suspend fun connectIfSessionExists() {}

    override suspend fun connectMqtt(mqttConnectionConfig: MqttConnectionConfig): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun subscribeToTopic(topic: String): Result<Unit> = Result.success(Unit)

    override suspend fun publish(
        topic: String,
        message: String,
    ): Result<Unit> = Result.success(Unit)

    override fun isConnected(): Flow<Boolean> {
        return flowOf(true)
    }

    override suspend fun disconnect(): Result<Unit> {
        return Result.success(Unit)
    }

    override fun incomingMessages(): SharedFlow<Pair<String, String>> = messages
}
