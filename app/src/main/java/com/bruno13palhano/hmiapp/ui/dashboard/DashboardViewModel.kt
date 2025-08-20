package com.bruno13palhano.hmiapp.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import com.bruno13palhano.core.data.configuration.LayoutConfig
import com.bruno13palhano.core.data.configuration.toWidget
import com.bruno13palhano.core.data.configuration.toWidgetConfig
import com.bruno13palhano.core.data.repository.EnvironmentRepository
import com.bruno13palhano.core.data.repository.MqttClientRepository
import com.bruno13palhano.core.data.repository.WidgetRepository
import com.bruno13palhano.core.model.DataSource
import com.bruno13palhano.core.model.Widget
import com.bruno13palhano.core.model.WidgetType
import com.bruno13palhano.hmiapp.ui.components.WidgetEvent
import com.bruno13palhano.hmiapp.ui.components.extractEndpoint
import com.bruno13palhano.hmiapp.ui.shared.Container
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.json.Json
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

    private val widgetValues = mutableMapOf<String, String>()

    fun onEvent(event: DashboardEvent) {
        when (event) {
            DashboardEvent.Init -> dashboardInit()
            is DashboardEvent.AddWidget -> addWidget()
            is DashboardEvent.RemoveWidget -> removeWidget(id = event.id)
            is DashboardEvent.MoveWidget -> moveWidget(id = event.id, x = event.x, y = event.y)
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

    private fun addEnvironment() {
        container.intent(dispatcher = Dispatchers.IO) {
            val environment = state.value.environment.copy(
                id = 0L,
                scale = 1f,
                offsetX = 0f,
                offsetY = 0f
            )

            environmentRepository.insert(environment = environment)
            reduce { copy(isEnvironmentDialogVisible = false) }
        }

        container.intent(dispatcher = Dispatchers.IO) {
            environmentRepository.getLast()?.let {
                reduce { copy(environment = it) }
            }
        }
    }

    private fun editEnvironment() = container.intent(dispatcher = Dispatchers.IO) {
        val environment = state.value.environment
        environmentRepository.update(environment = environment)

        reduce { copy(isEnvironmentDialogVisible = false) }
    }

    private fun changeEnvironment(id: Long){
        container.intent(dispatcher = Dispatchers.IO) {
            environmentRepository.getById(id = id)?.let {
                reduce { copy(environment = it) }
            }
        }

        loadWidgets(environmentId = id)
    }

    private fun addWidget() = container.intent(dispatcher = Dispatchers.IO) {
        reduce { copy(isWidgetInputDialogVisible = false) }

        val state = state.value
        val widget = Widget(
            type = state.type,
            label = state.label,
            dataSource = DataSource.MQTT(topic = state.endpoint),
            environmentId = state.environment.id
        )

        widgetRepository.insert(widget)
        subscribeToTopic(widget = widget)
    }

    private fun editWidget() = container.intent(dispatcher = Dispatchers.IO) {
        reduce { copy(isWidgetInputDialogVisible = false) }

        val state = state.value
        val widget = state.widgets.find { it.id == state.id }

        widget?.let {
            val editWidget = it.copy(
                type = state.type,
                label = state.label,
                dataSource = DataSource.MQTT(topic = state.endpoint)
            )

            widgetRepository.update(widget = editWidget)
            subscribeToTopic(widget = widget)
        }
    }

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

    private suspend fun subscribeToTopic(widget: Widget) {
        if (widget.dataSource is DataSource.MQTT) {
            mqttClientRepository.subscribeToTopic(
                topic = (widget.dataSource as DataSource.MQTT).topic
            )
        }

        clearCurrentWidget()
    }

    private fun clearCurrentWidget() = container.intent {
        reduce { copy(id = "", label = "", endpoint = "") }
    }

    private fun removeWidget(id: String) = container.intent(dispatcher = Dispatchers.IO) {
        widgetValues.remove(key = id)
        widgetRepository.deleteById(id = id)
    }

    private fun moveWidget(id: String, x: Float, y: Float) =
        container.intent(dispatcher = Dispatchers.IO) {
            widgetRepository.updatePosition(id = id, x = x, y = y)
        }

    private fun onWidgetEvent(widgetEvent: WidgetEvent) {
        when (widgetEvent) {
            is WidgetEvent.ButtonClicked -> {
                publishWidgetEvent(widgetId = widgetEvent.widgetId, message = "1")
            }
            is WidgetEvent.ColorPicked -> {
//                publishWidgetEvent(
//                    widgetId = widgetEvent.widgetId,
//                    message = widgetEvent.color
//                )
            }
            is WidgetEvent.DropdownSelected -> {
                publishWidgetEvent(widgetId = widgetEvent.widgetId, message = widgetEvent.selected)
            }
            is WidgetEvent.InputSubmitted -> {
                publishWidgetEvent(widgetId = widgetEvent.widgetId, message = widgetEvent.text)
            }
            is WidgetEvent.SliderChanged -> {
                publishWidgetEvent(
                    widgetId = widgetEvent.widgetId,
                    message = widgetEvent.value.toString()
                )
            }
            is WidgetEvent.SwitchToggled -> {
                publishWidgetEvent(
                    widgetId = widgetEvent.widgetId,
                    message = widgetEvent.state.toString()
                )
            }
            is WidgetEvent.ToggleButtonChanged -> {
               publishWidgetEvent(
                   widgetId = widgetEvent.widgetId,
                   message = widgetEvent.state.toString()
               )
            }
        }
    }

    private fun publishWidgetEvent(widgetId: String, message: String) =
        container.intent(dispatcher = Dispatchers.IO) {
            val widget = state.value.widgets.find { it.id == widgetId }

            widget?.let {
                mqttClientRepository.publish(
                    topic = (it.dataSource as DataSource.MQTT).topic,
                    message = message
                )
            }
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
        var id = 0L

        loadEnvironments()

        container.intent(dispatcher = Dispatchers.IO) {
            //Change to the right environment
            environmentRepository.getLast()?.let {
                reduce { copy(environment = it) }

                if (it.id != id) {
                    id = it.id
                    loadWidgets(environmentId = id)
                }
            }
        }

        container.intent(dispatcher = Dispatchers.IO) {
            mqttClientRepository.isConnected().collect { isConnected ->
                if (!isConnected) {
                    postSideEffect(
                        effect = DashboardSideEffect.ShowInfo(info = DashboardInfo.DISCONNECTED)
                    )
                } else {
                    val environmentId = state.value.environment.id
                    if (environmentId != 0L) {
                        observeMessages()
                    }
                }
            }
        }
    }

    private fun loadEnvironments() = container.intent(dispatcher = Dispatchers.IO) {
        environmentRepository.getAll().collectLatest {
            reduce { copy(environments = it) }
        }
    }

    private fun onUpdateCanvasState(
        scale: Float,
        offsetX: Float,
        offsetY: Float
    ) = container.intent {
        val environment = container.state.value.environment.copy(
            scale = scale,
            offsetX = offsetX,
            offsetY = offsetY
        )
        environmentRepository.update(environment = environment)
    }

    private fun loadWidgets(environmentId: Long) = container.intent(dispatcher = Dispatchers.IO) {
        widgetRepository.getWidgets(environmentId = environmentId).let { widgets ->
            val updatedWidgets = widgets.map { widget ->
                val value = widgetValues[widget.id] ?: ""
                widget.copy(value = value)
            }

            reduce { copy(widgets = updatedWidgets) }
            val topics = widgets.mapNotNull {
                (it.dataSource as? DataSource.MQTT)?.topic
            }

            topics.forEach { topic ->
                mqttClientRepository.subscribeToTopic(topic = topic)
            }
        }
    }

    private fun observeMessages() {
        container.intent(dispatcher = Dispatchers.IO) {
            mqttClientRepository.incomingMessages().collect { (topic, message) ->
                val updatedWidgets = state.value.widgets.map { widget ->
                    if ((widget.dataSource as DataSource.MQTT).topic == topic) {
                        val updated = widget.copy(value = message)
                        widgetValues[widget.id] = message
                        updated
                    } else widget
                }
                reduce { copy(widgets = updatedWidgets) }
            }
        }
    }

    private fun exportWidgets(stream: OutputStream) =
        container.intent(dispatcher = Dispatchers.Default) {
            val widgets = state.value.widgets.map { it.toWidgetConfig() }
            val layout = LayoutConfig(widgets)

            try {
                val json = Json.encodeToString(value = layout)
                stream.bufferedWriter().use { it.write(json) }
            } catch (_: Exception) {
                postSideEffect(
                    effect = DashboardSideEffect.ShowInfo(info = DashboardInfo.EXPORT_FAILURE)
                )
            }
        }

    private fun importWidgets(stream: InputStream) =
        container.intent(dispatcher = Dispatchers.IO) {
            try {
                val json = stream.bufferedReader().use { it.readText() }
                val layout = Json.decodeFromString<LayoutConfig>(string = json)

                layout.widgets.map { it.toWidget() }.forEach { widget ->
                    widgetRepository.insert(widget = widget)
                }
            } catch (_: Exception) {
                postSideEffect(
                    effect = DashboardSideEffect.ShowInfo(info = DashboardInfo.IMPORT_FAILURE)
                )
            }
        }

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