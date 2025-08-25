package com.bruno13palhano.hmiapp.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import com.bruno13palhano.core.data.repository.EnvironmentRepository
import com.bruno13palhano.core.data.repository.MqttClientRepository
import com.bruno13palhano.core.data.repository.WidgetRepository
import com.bruno13palhano.core.model.DataSource
import com.bruno13palhano.core.model.WidgetType
import com.bruno13palhano.hmiapp.ui.components.WidgetEvent
import com.bruno13palhano.hmiapp.ui.components.extractEndpoint
import com.bruno13palhano.hmiapp.ui.shared.Container
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import java.io.InputStream
import java.io.OutputStream

class DashboardViewModel @AssistedInject constructor(
    private val widgetRepository: WidgetRepository,
    private val mqttClientRepository: MqttClientRepository,
    private val environmentRepository: EnvironmentRepository,
    @Assisted private val initialState: DashboardState,
) : ViewModel() {
    val container: Container<DashboardState, DashboardSideEffect> = Container(
        initialSTATE = initialState,
        scope = viewModelScope
    )

    private val widgetManager = WidgetManager(
        widgetRepository = widgetRepository,
        mqttClientRepository = mqttClientRepository,
        container = container
    )
    private val environmentManager = EnvironmentManager(
        environmentRepository = environmentRepository,
        container = container
    )

    fun onEvent(event: DashboardEvent) {
        when (event) {
            DashboardEvent.Init -> dashboardInit()
            is DashboardEvent.AddWidget -> addWidget()
            is DashboardEvent.RemoveWidget -> removeWidget(id = event.id)
            is DashboardEvent.OnWidgetDragEnd -> onWidgetDragEnd(id = event.id, x = event.x, y = event.y)
            is DashboardEvent.OpenEditWidgetDialog -> openEditWidgetDialog(id = event.id)
            is DashboardEvent.EditWidget -> editWidget()
            is DashboardEvent.OnUpdateCanvasState -> onUpdateCanvasState(
                scale = event.scale,
                offsetX = event.offsetX,
                offsetY = event.offsetY
            )
            is DashboardEvent.OnWidgetEvent -> onWidgetEvent(widgetEvent = event.widgetEvent)
            DashboardEvent.AddEnvironment -> addEnvironment()
            DashboardEvent.EditEnvironment -> editEnvironment()
            is DashboardEvent.ChangeEnvironment -> changeEnvironment(id = event.id)
            DashboardEvent.ToggleIsToolboxExpanded -> toggleIsToolboxExpanded()
            DashboardEvent.ToggleMenu -> toggleMenu()
            DashboardEvent.ToggleDashboardOptions -> toggleDashboardOptions()
            is DashboardEvent.OpenEnvironmentInputDialog -> openEnvironmentInputDialog(isEdit = event.isEdit)
            DashboardEvent.CloseEnvironmentInputDialog -> closeEnvironmentInputDialog()
            DashboardEvent.HideWidgetConfig -> hideWidgetDialog()
            is DashboardEvent.ShowWidgetDialog -> showWidgetDialog(type = event.type)
            is DashboardEvent.UpdateEndpoint -> updateEndpoint(endpoint = event.endpoint)
            is DashboardEvent.UpdateLabel -> updateLabel(label = event.label)
            is DashboardEvent.UpdateEnvironmentName -> updateEnvironmentName(name = event.name)
            is DashboardEvent.NavigateTo -> navigateTo(key = event.destination)
            is DashboardEvent.ExportWidgetsConfig -> exportWidgets(stream = event.stream)
            is DashboardEvent.ImportWidgetsConfig -> importWidgets(stream = event.stream)
            is DashboardEvent.OnVertMenuItemClick -> onVertMenuItemClick(item = event.item)
            DashboardEvent.ToggleVertMenu -> toggleVertMenu()
        }
    }

    private fun navigateTo(key: NavKey) = container.intent {
        postSideEffect(effect = DashboardSideEffect.NavigateTo(destination = key))
    }

    private fun showWidgetDialog(type: WidgetType) = container.intent {
        reduce {
            copy(
                isToolboxExpanded = false,
                isWidgetInputDialogVisible = true,
                type = type
            )
        }
    }

    private fun hideWidgetDialog() = container.intent {
        reduce { copy(isWidgetInputDialogVisible = false) }
    }

    private fun updateEndpoint(endpoint: String) = container.intent {
        reduce { copy(endpoint = endpoint) }
    }

    private fun updateLabel(label: String) = container.intent {
        reduce { copy(label = label) }
    }

    private fun updateEnvironmentName(name: String) = container.intent {
        val environment = container.state.value.environment
        reduce { copy(environment = environment.copy(name = name)) }
    }

    private fun addEnvironment() = environmentManager.addEnvironment()

    private fun editEnvironment() = environmentManager.editEnvironment()

    private fun changeEnvironment(id: Long) {
        environmentManager.changeEnvironment(id = id)
        widgetManager.loadWidgets(environmentId = id)
    }

    private fun addWidget() = widgetManager.addWidget()

    private fun editWidget() = widgetManager.editWidget()

    private fun openEditWidgetDialog(id: String) = container.intent {
        val state = state.value
        val widget = state.widgets.find { it.id == id }

        widget?.let {
            showWidgetDialog(type = it.type)

            reduce {
                copy(
                    id = it.id,
                    label = it.label,
                    endpoint = when (val dataSource = it.dataSource) {
                        is DataSource.MQTT -> dataSource.topic
                        is DataSource.HTTP -> extractEndpoint(url = dataSource.url)
                    }
                )
            }
        }
    }

    private fun removeWidget(id: String) = widgetManager.removeWidget(id = id)

    private fun onWidgetDragEnd(id: String, x: Float, y: Float) {
        widgetManager.onWidgetDragEnd(id = id, x = x, y = y)
    }

    private fun onWidgetEvent(widgetEvent: WidgetEvent) {
        widgetManager.onWidgetEvent(widgetEvent = widgetEvent)
    }

    private fun toggleIsToolboxExpanded() = container.intent {
        reduce {
            copy(isToolboxExpanded = !isToolboxExpanded, isDashboardOptionsExpanded = false)
        }
    }

    private fun toggleMenu() = container.intent {
        postSideEffect(effect = DashboardSideEffect.ToggleMenu)
    }

    private fun toggleDashboardOptions() = container.intent {
        reduce { copy(isDashboardOptionsExpanded = !isDashboardOptionsExpanded) }
    }

    private fun openEnvironmentInputDialog(isEdit: Boolean) = container.intent {
        reduce {
            copy(
                isEnvironmentDialogVisible = true,
                isDashboardOptionsExpanded = false,
                isEditEnvironmentName = isEdit
            )
        }
    }

    private fun closeEnvironmentInputDialog() = container.intent {
        reduce { copy(isEnvironmentDialogVisible = false) }
    }

    private fun dashboardInit() {
        environmentManager.loadEnvironments()

        environmentManager.loadPreviousEnvironment { id ->
            widgetManager.loadWidgets(environmentId = id)
            widgetManager.observeMessages()
        }


        container.intent(dispatcher = Dispatchers.IO) {
            mqttClientRepository.isConnected().collect { isConnected ->
                if (!isConnected) {
                    postSideEffect(
                        effect = DashboardSideEffect.ShowInfo(info = DashboardInfo.DISCONNECTED)
                    )
                }
            }
        }
    }

    private fun onUpdateCanvasState(scale: Float, offsetX: Float, offsetY: Float) {
        environmentManager.updateEnvironmentState(
            scale = scale,
            offsetX = offsetX,
            offsetY = offsetY
        )
    }

    private fun exportWidgets(stream: OutputStream) = widgetManager.exportWidgets(stream = stream)

    private fun importWidgets(stream: InputStream) = widgetManager.importWidgets(stream = stream)

    private fun onExportWidgetsConfig() = container.intent {
        postSideEffect(effect = DashboardSideEffect.LaunchExportWidgetsConfig)
    }

    private fun onImportWidgetsConfig() = container.intent {
        postSideEffect(effect = DashboardSideEffect.LaunchImportWidgetsConfig)
    }

    private fun onVertMenuItemClick(item: ConfigurationOptions) {
        when (item) {
            ConfigurationOptions.EXPORT -> onExportWidgetsConfig()
            ConfigurationOptions.IMPORT -> onImportWidgetsConfig()
        }
    }

    private fun toggleVertMenu() = container.intent {
        reduce { copy(isVertMenuVisible = !isVertMenuVisible) }
    }
}