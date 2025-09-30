package com.bruno13palhano.hmiapp

import app.cash.turbine.test
import com.bruno13palhano.core.data.repository.MqttClientRepository
import com.bruno13palhano.hmiapp.ui.navigation.Dashboard
import com.bruno13palhano.hmiapp.ui.settings.SettingsEvent
import com.bruno13palhano.hmiapp.ui.settings.SettingsInfo
import com.bruno13palhano.hmiapp.ui.settings.SettingsSideEffect
import com.bruno13palhano.hmiapp.ui.settings.SettingsState
import com.bruno13palhano.hmiapp.ui.settings.SettingsViewModel
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
    fun `UpdateClientId with empty string should mark invalid`() = runTest {
        viewModel.container.state.test {
            skipItems(1)
            viewModel.onEvent(event = SettingsEvent.UpdateClientId(clientId = ""))
            val state = awaitItem()
            assertThat(state.clientId).isEmpty()
            assertThat(state.isClientIdInvalid).isTrue()
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
    fun `UpdateHost with empty sting should mark invalid`() = runTest {
        viewModel.container.state.test  {
            skipItems(1)
            viewModel.onEvent(event = SettingsEvent.UpdateHost(host = ""))
            val state = awaitItem()
            assertThat(state.host).isEmpty()
            assertThat(state.isHostInvalid).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `UpdatePort should update state`() = runTest {
        val expected = "8883"

        viewModel.container.state.test {
            skipItems(1)
            viewModel.onEvent(event = SettingsEvent.UpdatePort(port = expected))
            assertThat(awaitItem().port).isEqualTo(expected)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `UpdatePort with empty string should mark invalid`() = runTest {
        viewModel.container.state.test {
            skipItems(1)
            viewModel.onEvent(event = SettingsEvent.UpdatePort(port = ""))
            val state = awaitItem()
            assertThat(state.port).isEmpty()
            assertThat(state.isPortInvalid).isTrue()
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
    fun `LoadCA should update state`() = runTest {
        val expected = byteArrayOf(1)

        viewModel.container.state.test {
            skipItems(1)
            viewModel.onEvent(event = SettingsEvent.LoadCA(caCert = expected))
            assertThat(awaitItem().caCert).isEqualTo(expected)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `LoadClientCert should update state`() = runTest {
        val expected = byteArrayOf(1)

        viewModel.container.state.test {
            skipItems(1)
            viewModel.onEvent(event = SettingsEvent.LoadClientCert(clientCert = expected))
            assertThat(awaitItem().clientP12).isEqualTo(expected)
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
            skipItems(1)
            viewModel.onEvent(event = SettingsEvent.ToggleCredentialDialog)
            assertThat(awaitItem().isCredentialDialogOpen).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ToggleMenu should emit ToggleMenu side effect`() = runTest {
        viewModel.container.sideEffect.test {
            viewModel.onEvent(event = SettingsEvent.ToggleMenu)
            assertThat(awaitItem()).isEqualTo(SettingsSideEffect.ToggleMenu)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `HideKeyboardAndClearFocus should emit HideKeyboardAndClearFocus side effect`() =  runTest {
        viewModel.container.sideEffect.test {
            viewModel.onEvent(event = SettingsEvent.HideKeyboardAndClearFocus)
            assertThat(awaitItem()).isEqualTo(SettingsSideEffect.HideKeyboardAndClearFocus)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `NavigateTo should emit NavigateTo side effect`() = runTest {
        viewModel.container.sideEffect.test {
            viewModel.onEvent(event = SettingsEvent.NavigateTo(destination = Dashboard))
            assertThat(
                awaitItem()
            ).isEqualTo(SettingsSideEffect.NavigateTo(destination = Dashboard))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ConnectMqtt should not attempt connection when required fields are missing`() = runTest {
        viewModel = SettingsViewModel(
            mqttClientRepository = mqttClientRepository,
            initialState = initialState.copy(
                clientId = "",
                host = "",
                port = ""
            )
        )

        viewModel.container.state.test {
            skipItems(1)
            viewModel.onEvent(event = SettingsEvent.ConnectMqtt)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 0) { mqttClientRepository.connectMqtt(any()) }
    }

    @Test
    fun `CheckConnection success should update isConnected to true`() = runTest {
        every { mqttClientRepository.isConnected() } returns flowOf(true)

        viewModel.container.state.test {
            skipItems(1)
            viewModel.onEvent(event = SettingsEvent.CheckConnection)
            assertThat(awaitItem().isConnected).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `CheckConnection failure should update isConnected to false`() = runTest {
        viewModel = SettingsViewModel(
            mqttClientRepository = mqttClientRepository,
            initialState = initialState.copy(isConnected = true)
        )

        every { mqttClientRepository.isConnected() } returns flowOf(false)

        viewModel.container.state.test {
            skipItems(1)
            viewModel.onEvent(event = SettingsEvent.CheckConnection)
            assertThat(awaitItem().isConnected).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ConnectMqtt success should emit ShowInfo CONNECT_SUCCESS`() = runTest {
        val mainDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(mainDispatcher)

        viewModel = SettingsViewModel(
            mqttClientRepository = mqttClientRepository,
            initialState = initialState.copy(
                clientId = "client",
                host = "192.168.1.101",
                port = "8883",
                username = "username",
                password = "password",
                p12Password = "p12Password"
            )
        )

        coEvery { mqttClientRepository.connectMqtt(any()) } returns Result.success(Unit)

        viewModel.container.sideEffect.test {
            viewModel.container.state.test {
                skipItems(1)
                viewModel.onEvent(event = SettingsEvent.ConnectMqtt)
                assertThat(awaitItem().isLoading).isTrue()

                testScheduler.advanceUntilIdle()
                assertThat(awaitItem().isLoading).isFalse()
            }

            assertThat(awaitItem()).isEqualTo(SettingsSideEffect.ShowSettingsInfo(info = SettingsInfo.CONNECT_SUCCESS))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ConnectMqtt failure should emit ShowInfo CONNECT_FAILURE`() = runTest {
        viewModel = SettingsViewModel(
            mqttClientRepository = mqttClientRepository,
            initialState = initialState.copy(
                clientId = "client",
                host = "192.168.1.101",
                port = "8883",
                username = "username",
                password = "password",
                p12Password = "p12Password"
            )
        )

        coEvery { mqttClientRepository.connectMqtt(any()) } returns Result.failure(exception = Exception())

        viewModel.container.sideEffect.test {
            viewModel.container.state.test {
                skipItems(1)
                viewModel.onEvent(event = SettingsEvent.ConnectMqtt)
                assertThat(awaitItem().isLoading).isTrue()

                testScheduler.advanceUntilIdle()
                assertThat(awaitItem().isLoading).isFalse()
            }

            assertThat(awaitItem()).isEqualTo(SettingsSideEffect.ShowSettingsInfo(info = SettingsInfo.CONNECT_FAILURE))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `DisconnectMqtt success should emit ShowInfo DISCONNECT_SUCCESS`() = runTest {
        coEvery { mqttClientRepository.disconnect() } returns Result.success(Unit)

        viewModel.container.sideEffect.test {
            viewModel.onEvent(event = SettingsEvent.DisconnectMqtt)
            assertThat(
                awaitItem()
            ).isEqualTo(SettingsSideEffect.ShowSettingsInfo(info = SettingsInfo.DISCONNECT_SUCCESS))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `DisconnectMqtt failure should emit ShowInfo DISCONNECT_FAILURE`() = runTest {
        coEvery { mqttClientRepository.disconnect() } returns Result.failure(exception = Exception())

        viewModel.container.sideEffect.test {
            viewModel.onEvent(event = SettingsEvent.DisconnectMqtt)
            assertThat(
                awaitItem()
            ).isEqualTo(SettingsSideEffect.ShowSettingsInfo(info = SettingsInfo.DISCONNECT_FAILURE))
            cancelAndIgnoreRemainingEvents()
        }
    }
}