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
import com.bruno13palhano.hmiapp.ui.dashboard.DashboardSideEffect
import com.bruno13palhano.hmiapp.ui.dashboard.DashboardState
import com.bruno13palhano.hmiapp.ui.dashboard.DashboardViewModel
import com.bruno13palhano.hmiapp.ui.navigation.Dashboard
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
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

    @Test
    fun `Initial state should have defaults`() = runTest {
        val state = viewModel.container.state.value
        assertThat(state).isEqualTo(initialState)
    }

    @Test
    fun `UpdateEndpoint should update state correctly`() = runTest {
        val expected = "endpoint"

        viewModel.container.state.test {
            skipItems(1)
            viewModel.onEvent(event = DashboardEvent.UpdateEndpoint(endpoint = expected))
            assertThat(awaitItem().endpoint).isEqualTo(expected)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `UpdateLabel should update state correctly`() = runTest {
        val expected = "label"

        viewModel.container.state.test {
            skipItems(1)
            viewModel.onEvent(event = DashboardEvent.UpdateLabel(label = expected))
            assertThat(awaitItem().label).isEqualTo(expected)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `UpdateExtra should update state correctly`() = runTest {
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
    fun `UpdateEnvironmentName should update state correctly`() = runTest {
        val expected = "name"

        viewModel.container.state.test {
            skipItems(1)
            viewModel.onEvent(event = DashboardEvent.UpdateEnvironmentName(name = expected))
            assertThat(awaitItem().environment.name).isEqualTo(expected)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ToggleVertMenu should toggle state correctly`() = runTest {
        val expected = true

        viewModel.container.state.test {
            skipItems(1)
            viewModel.onEvent(event = DashboardEvent.ToggleVertMenu)
            assertThat(awaitItem().isVertMenuVisible).isEqualTo(expected)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `NavigateTo triggers correct side effect`() = runTest {
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
    fun `ConfirmWidget add`() = runTest {

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
}