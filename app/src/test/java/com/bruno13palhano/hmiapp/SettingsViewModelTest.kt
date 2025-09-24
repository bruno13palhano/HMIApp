package com.bruno13palhano.hmiapp

import com.bruno13palhano.core.data.repository.MqttClientRepository
import com.bruno13palhano.hmiapp.ui.settings.SettingsState
import com.bruno13palhano.hmiapp.ui.settings.SettingsViewModel
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var viewModel: SettingsViewModel

    @MockK
    lateinit var mqttClientRepository: MqttClientRepository

    private val initialState = SettingsState()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(StandardTestDispatcher())
        viewModel = SettingsViewModel(
            mqttClientRepository = mqttClientRepository,
            initialState = initialState
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}