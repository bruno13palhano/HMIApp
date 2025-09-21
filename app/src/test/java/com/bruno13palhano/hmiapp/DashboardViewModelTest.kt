package com.bruno13palhano.hmiapp

import app.cash.turbine.test
import com.bruno13palhano.core.data.repository.EnvironmentRepository
import com.bruno13palhano.core.data.repository.MqttClientRepository
import com.bruno13palhano.core.data.repository.WidgetRepository
import com.bruno13palhano.core.model.DataSource
import com.bruno13palhano.core.model.Environment
import com.bruno13palhano.core.model.Widget
import com.bruno13palhano.core.model.WidgetType
import com.bruno13palhano.hmiapp.ui.dashboard.DashboardEvent
import com.bruno13palhano.hmiapp.ui.dashboard.DashboardInfo
import com.bruno13palhano.hmiapp.ui.dashboard.DashboardSideEffect
import com.bruno13palhano.hmiapp.ui.dashboard.DashboardState
import com.bruno13palhano.hmiapp.ui.dashboard.DashboardViewModel
import com.bruno13palhano.hmiapp.ui.navigation.Dashboard
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {
    private lateinit var viewModel: DashboardViewModel

    @MockK
    lateinit var widgetRepository: WidgetRepository

    @MockK
    lateinit var mqttClientRepository: MqttClientRepository

    @MockK
    lateinit var environmentRepository: EnvironmentRepository

    private val initialState = DashboardState()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(StandardTestDispatcher())
        viewModel = DashboardViewModel(
            widgetRepository = widgetRepository,
            mqttClientRepository = mqttClientRepository,
            environmentRepository = environmentRepository,
            initialState = initialState
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    //
    // Simple state updates
    //
    @Test
    fun `Initial state should equal provided initial state`() = runTest {
        val state = viewModel.container.state.value
        assertThat(state).isEqualTo(initialState)
    }

    @Test
    fun `UpdateEndpoint should update state`() = runTest {
        val expected = "endpoint"

        viewModel.container.state.test {
            skipItems(1)
            viewModel.onEvent(event = DashboardEvent.UpdateEndpoint(endpoint = expected))
            assertThat(awaitItem().endpoint).isEqualTo(expected)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `UpdateLabel should update state`() = runTest {
        val expected = "label"

        viewModel.container.state.test {
            skipItems(1)
            viewModel.onEvent(event = DashboardEvent.UpdateLabel(label = expected))
            assertThat(awaitItem().label).isEqualTo(expected)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `AddExtra and UpdateExtra should update extras`() = runTest {
        val index = 0
        val expected = "extra"

        viewModel.container.state.test {
            skipItems(1)
            viewModel.onEvent(event = DashboardEvent.AddExtra)
            skipItems(1)
            viewModel.onEvent(event = DashboardEvent.UpdateExtra(index = index, value = expected))
            assertThat(awaitItem().extras[index]).isEqualTo(expected)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `UpdateEnvironmentName should update environment name`() = runTest {
        val expected = "name"

        viewModel.container.state.test {
            skipItems(1)
            viewModel.onEvent(event = DashboardEvent.UpdateEnvironmentName(name = expected))
            assertThat(awaitItem().environment.name).isEqualTo(expected)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ToggleVertMenu should invert visibility`() = runTest {
        viewModel.container.state.test {
            skipItems(1)
            viewModel.onEvent(event = DashboardEvent.ToggleVertMenu)
            assertThat(awaitItem().isVertMenuVisible).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    //
    //Side effects
    //
    @Test
    fun `NavigateTo should emit NavigateTo side effect`() = runTest {
        viewModel.container.sideEffect.test {
            viewModel.onEvent(
                event = DashboardEvent.NavigateTo(destination = Dashboard)
            )
            assertThat(
                awaitItem()
            ).isEqualTo(DashboardSideEffect.NavigateTo(destination = Dashboard))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ToggleMenu should emit ToggleMenu side effect`() = runTest {
        viewModel.container.sideEffect.test {
            viewModel.onEvent(DashboardEvent.ToggleMenu)
            assertThat(awaitItem()).isEqualTo(DashboardSideEffect.ToggleMenu)
            cancelAndIgnoreRemainingEvents()
        }
    }

    //
    // Dialogs
    //
    @Test
    fun `HideWidgetDialog should clear widget config after delay`() = runTest {
        viewModel.container.state.test {
            skipItems(1)
            viewModel.onEvent(event = DashboardEvent.HideWidgetConfig)


            val afterClose = awaitItem()
            assertThat(afterClose.isWidgetInputDialogVisible).isFalse()

            advanceTimeBy(500)

            val afterClear = awaitItem()
            assertThat(afterClear.id).isEmpty()
            assertThat(afterClear.label).isEmpty()
            assertThat(afterClear.label).isEmpty()
            assertThat(afterClear.endpoint).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    //
    // Repository integration
    //

    @Test
    fun `ConfirmWidget should insert widget and update state`() = runTest {
        val environment = Environment(1L, "test", 1f, 0f, 0f)
        val widget = Widget(
            type = WidgetType.BUTTON,
            dataSource = DataSource.MQTT(topic = "topic"),
            label = "label",
            environmentId = environment.id
        )
        val expected = listOf(widget)

        coEvery { widgetRepository.insert(any()) } returns Unit
        coEvery { widgetRepository.getWidgets(any()) } returns expected
        coEvery { mqttClientRepository.subscribeToTopic(any()) } returns Result.success(Unit)
        coEvery { mqttClientRepository.incomingMessages() } returns MutableSharedFlow()

        viewModel.container.state.test {
            skipItems(1)
            viewModel.onEvent(event = DashboardEvent.ConfirmWidget)
            val state = awaitItem()
            assertThat(state.isWidgetInputDialogVisible).isFalse()
            assertThat(state.widgets).isEqualTo(expected)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `EditWidget should update widget and refresh state`() = runTest {
        val environment = Environment(1L, "env", 1f, 0f, 0f)
        val widget = Widget(
            id = "w1",
            type = WidgetType.BUTTON,
            dataSource = DataSource.MQTT(topic = "oldTopic"),
            label = "oldLabel",
            environmentId = environment.id
        )
        val updateWidget = widget.copy(
            type = WidgetType.SWITCH,
            label = "newLabel",
            dataSource = DataSource.MQTT(topic = "newTopic")
        )

        coEvery { widgetRepository.update(any()) } returns Unit
        coEvery { widgetRepository.getWidgets(environment.id) } returns listOf(updateWidget)
        coEvery { mqttClientRepository.subscribeToTopic(any()) } returns Result.success(Unit)

        viewModel.container.intentSync {
            reduce { copy(environment = environment, widgets = listOf(widget), id =  widget.id, label = "newLabel", endpoint = "newTopic", type = WidgetType.SWITCH) }
        }

        viewModel.container.state.test {
            skipItems(1)
            viewModel.onEvent(event = DashboardEvent.ConfirmWidget)
            val state = awaitItem()
            assertThat(state.widgets.first().label).isEqualTo("newLabel")
            assertThat(state.widgets.first().type).isEqualTo(WidgetType.SWITCH)
            cancelAndIgnoreRemainingEvents()
        }
    }

    //
    // Flows and Init
    //
    @Test
    fun `DashboardInit should emit ShowInfo when disconnected`() = runTest {
        val env = Environment(1L, "test", 1f, 0f, 0f)

        coEvery { environmentRepository.getLast() } returns env
        coEvery { widgetRepository.getWidgets(env.id) } returns emptyList()
        coEvery { mqttClientRepository.isConnected() } returns MutableSharedFlow<Boolean>(1).apply { tryEmit(false) }

        viewModel.container.sideEffect.test {
            viewModel.onEvent(DashboardEvent.Init)
            advanceUntilIdle()
            assertThat(awaitItem()).isEqualTo(
                DashboardSideEffect.ShowInfo(DashboardInfo.DISCONNECTED)
            )
            cancelAndIgnoreRemainingEvents()
        }
    }
}