package com.bruno13palhano.hmiapp

import app.cash.turbine.test
import com.bruno13palhano.core.data.repository.MqttClientRepository
import com.bruno13palhano.hmiapp.ui.settings.SettingsEvent
import com.bruno13palhano.hmiapp.ui.settings.SettingsState
import com.bruno13palhano.hmiapp.ui.settings.SettingsViewModel
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

    @Test
    fun `Initial state should equal provided initial state`() = runTest {
        val state = viewModel.container.state.value
        assertThat(state).isEqualTo(initialState)
    }

    @Test
    fun `UpdateClientId should update state`() = runTest {
        val expected = "client01"

        viewModel.container.state.test {
            skipItems(1)
            viewModel.onEvent(event = SettingsEvent.UpdateClientId(clientId = expected))
            assertThat(awaitItem().clientId).isEqualTo(expected)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `UpdateHost should update state`() = runTest {
        val expected = "host"

        viewModel.container.state.test {
            skipItems(1)
            viewModel.onEvent(event = SettingsEvent.UpdateHost(host = expected))
            assertThat(awaitItem().host).isEqualTo(expected)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `UpdateUsername should update state`() = runTest {
        val expected = "username"

        viewModel.container.state.test {
            skipItems(1)
            viewModel.onEvent(event = SettingsEvent.UpdateUsername(username = expected))
            assertThat(awaitItem().username).isEqualTo(expected)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `UpdatePassword should update state`() = runTest {
        val expected = "password"

        viewModel.container.state.test {
            skipItems(1)
            viewModel.onEvent(event = SettingsEvent.UpdatePassword(password = expected))
            assertThat(awaitItem().password).isEqualTo(expected)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `UpdateP12Password should  update state`() = runTest {
        val expected = "p12Password"

        viewModel.container.state.test {
            skipItems(1)
            viewModel.onEvent(event = SettingsEvent.UpdateP12Password(p12Password = expected))
            assertThat(awaitItem().p12Password).isEqualTo(expected)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `TogglePasswordVisibility should invert visibility`() = runTest {
        viewModel.container.state.test {
            skipItems(1)
            viewModel.onEvent(event = SettingsEvent.TogglePasswordVisibility)
            assertThat(awaitItem().passwordVisibility).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ToggleP12PasswordVisibility should invert visibility`() = runTest {
        viewModel.container.state.test {
            skipItems(1)
            viewModel.onEvent(event = SettingsEvent.ToggleP12PasswordVisibility)
            assertThat(awaitItem().p12PasswordVisibility).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ToggleCredentialDialog should invert visibility`() = runTest {
        viewModel.container.state.test {
            viewModel.onEvent(event = SettingsEvent.ToggleCredentialDialog)
            assertThat(awaitItem().isCredentialDialogOpen).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ToggleMenu should emit ToggleMenu side effect`() = runTest {

    }

    @Test
    fun `HideKeyboardAndClearFocus should emit HideKeyboardAndClearFocus side effect`() =  runTest {

    }

    @Test
    fun `NavigateTo should emit NavigateTo side effect`() = runTest {

    }

    @Test
    fun `CheckConnection`() = runTest {

    }

    @Test
    fun `ConnectMqtt`() = runTest {

    }

    @Test
    fun `DisconnectMqtt`() = runTest {

    }

    @Test
    fun `LoadCA`() = runTest {

    }

    @Test
    fun `LoadClientCert`() = runTest {

    }
}