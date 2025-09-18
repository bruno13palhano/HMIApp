package com.bruno13palhano.hmiapp

import com.bruno13palhano.core.data.repository.EnvironmentRepository
import com.bruno13palhano.core.data.repository.MqttClientRepository
import com.bruno13palhano.core.data.repository.WidgetRepository
import com.bruno13palhano.hmiapp.ui.dashboard.DashboardState
import com.bruno13palhano.hmiapp.ui.dashboard.DashboardViewModel
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
}