package com.bruno13palhano.hmiapp.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import com.bruno13palhano.core.data.configuration.LayoutConfig
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
import kotlinx.coroutines.delay
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

    private val widgetManager = WidgetManager(
        widgetRepository = widgetRepository,
        environmentRepository = environmentRepository,
        mqttClientRepository = mqttClientRepository
    )
    private val environmentManager = EnvironmentManager(
        environmentRepository = environmentRepository
    )

    private val widgetValues = mutableMapOf<String, String>()

    fun onEvent(event: DashboardEvent) {
        when (event) {
            DashboardEvent.Init -> dashboardInit()
            is DashboardEvent.ConfirmWidget -> confirmWidget()
            is DashboardEvent.RemoveWidget -> removeWidget(id = event.id)
            is DashboardEvent.OnWidgetDragEnd -> onWidgetDragEnd(
                id = event.id,
                x = event.x,
                y = event.y
            )
            is DashboardEvent.OpenEditWidgetDialog -> openEditWidgetDialog(id = event.id)
            is DashboardEvent.OnUpdateCanvasState -> onUpdateCanvasState(
                scale = event.scale,
                offsetX = event.offsetX,
                offsetY = event.offsetY
            )
            is DashboardEvent.OnToggleWidgetPin -> onToggleWidgetPin(id = event.id)
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
            is DashboardEvent.UpdateExtra -> updateExtra(index = event.index, value = event.value)
            DashboardEvent.AddExtra -> addExtra()
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
                type = type,
                hasExtras = type == WidgetType.DROPDOWN
            )
        }
    }

    private fun hideWidgetDialog() = container.intent {
        reduce { copy(isWidgetInputDialogVisible = false) }
        delay(450)
        reduce {
            copy(
                id = "",
                label = "",
                endpoint = "",
                extras = emptyList(),
                hasExtras = false
            )
        }
    }

    private fun updateEndpoint(endpoint: String) = container.intent {
        reduce { copy(endpoint = endpoint) }
    }

    private fun updateLabel(label: String) = container.intent {
        reduce { copy(label = label) }
    }

    private fun updateExtra(index: Int, value: String) = container.intent {
        val updated = state.value.extras.toMutableList()
        if (index in updated.indices) {
            updated[index] = value
            reduce { copy(extras = updated) }
        }
    }

    private fun addExtra() = container.intent {
        val updated = state.value.extras.toMutableList()
        updated.add("")
        reduce { copy(extras = updated) }
    }

    private fun updateEnvironmentName(name: String) = container.intent {
        val environment = container.state.value.environment
        reduce { copy(environment = environment.copy(name = name)) }
    }

    private fun onToggleWidgetPin(id: String) = container.intent(dispatcher = Dispatchers.IO) {
        state.value.widgets.find { it.id == id }?.let { widget ->
            val environmentId = state.value.environment.id.takeIf { it != 0L } ?: return@intent
            val newWidget = widget.copy(isPinned = !widget.isPinned)

            widgetManager.updateWidgetPin(widget = newWidget)
            refreshWidgets(environmentId = environmentId)
        }
    }

    private fun addEnvironment() = container.intent(dispatcher = Dispatchers.IO) {
        reduce { copy(isEnvironmentDialogVisible = false) }

        val environment = state.value.environment.copy(
            id = 0L,
            scale = 1f,
            offsetX = 0f,
            offsetY = 0f
        )
        val newEnvironment = environmentManager.addEnvironment(environment = environment)
        if (newEnvironment == null) return@intent

        reduce { copy(environment = newEnvironment) }
        loadWidgets(environmentId = newEnvironment.id)
    }

    private fun editEnvironment() = container.intent(dispatcher = Dispatchers.IO) {
        reduce { copy(isEnvironmentDialogVisible = false) }

        val environment = state.value.environment
        environmentManager.editEnvironment(environment = environment)
        loadWidgets(environmentId = environment.id)
    }

    private fun changeEnvironment(id: Long) = container.intent(dispatcher = Dispatchers.IO) {
        val updated = environmentManager.changeEnvironment(id = id)
        updated?.let { reduce { copy(environment = it) } }
        loadWidgets(environmentId = id)
    }

    private fun confirmWidget() = container.intent {
        if (state.value.id == "") {
            addWidget()
        } else {
            editWidget()
        }
    }

    private fun addWidget() = container.intent(dispatcher = Dispatchers.IO) {
        reduce { copy(isWidgetInputDialogVisible = false) }
        delay(450)

        val environmentId = state.value.environment.id

        widgetManager.addWidget(
            environmentId = environmentId,
            type = state.value.type,
            label = state.value.label,
            endpoint = state.value.endpoint,
            extras = state.value.extras
        )
        val widgets = widgetManager.loadWidgets(environmentId = environmentId).map {
            it.copy(value = widgetValues[it.id] ?: "")
        }
        reduce { copy(widgets = widgets, id = "", label = "", endpoint = "", extras = emptyList()) }

        widgetManager.subscribeToTopics(
            widgets.mapNotNull {
                (it.dataSource as? DataSource.MQTT)?.topic
            }
        )
        refreshWidgets(environmentId = environmentId)
    }

    private fun editWidget() = container.intent(dispatcher = Dispatchers.IO) {
        reduce { copy(isWidgetInputDialogVisible = false) }
        delay(450)

        val environmentId = state.value.environment.id.takeIf { it != 0L } ?: return@intent
        val current = state.value.widgets.find { it.id == state.value.id } ?: return@intent

        val updated = current.copy(
            type = state.value.type,
            label = state.value.label,
            dataSource = DataSource.MQTT(topic = state.value.endpoint),
            extras = state.value.extras
        )

        widgetManager.editWidget(widget = updated)
        val widgets = widgetManager.loadWidgets(environmentId = environmentId).map {
            it.copy(value = widgetValues[it.id] ?: "")
        }
        reduce { copy(widgets = widgets, id = "", label = "", endpoint = "", extras = emptyList()) }
        refreshWidgets(environmentId = environmentId)
    }

    private fun loadWidgets(environmentId: Long) = container.intent(Dispatchers.IO) {
        val widgets = widgetManager.loadWidgets(environmentId = environmentId)

        val updateWidgets = widgets.map { widget ->
            val value = widgetValues[widget.id] ?: ""
            widget.copy(value = value)
        }

        reduce { copy(widgets = updateWidgets) }
        val topics = widgets.mapNotNull { (it.dataSource as? DataSource.MQTT)?.topic }
        topics.forEach { topic -> mqttClientRepository.subscribeToTopic(topic = topic) }
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
                    },
                    extras = it.extras ?: emptyList()
                )
            }
        }
    }

    private fun removeWidget(id: String) = container.intent(dispatcher = Dispatchers.IO) {
        val environmentId = state.value.environment.id.takeIf { it != 0L } ?: return@intent
        widgetManager.removeWidget(id = id)
        val widgets = widgetManager.loadWidgets(environmentId = environmentId).map {
            it.copy(value = widgetValues[it.id] ?: "")
        }
        reduce { copy(widgets = widgets) }
    }

    private fun onWidgetDragEnd(id: String, x: Float, y: Float) =
        container.intent(dispatcher = Dispatchers.IO) {
            widgetManager.onWidgetDragEnd(id = id, x = x, y = y)
        }

    private fun onWidgetEvent(widgetEvent: WidgetEvent) = container.intent(dispatcher = Dispatchers.IO) {
        when (widgetEvent) {
            is WidgetEvent.ButtonClicked -> {
                widgetManager.publish(widget = widgetEvent.widget, message = "1")
            }
            is WidgetEvent.DropdownSelected -> {
                widgetManager.publish(widget = widgetEvent.widget, message = widgetEvent.selected)
            }
            is WidgetEvent.InputSubmitted -> {
                widgetManager.publish(widget = widgetEvent.widget, message = widgetEvent.text)
            }
            is WidgetEvent.SliderChanged -> {
                widgetManager.publish(widget = widgetEvent.widget, message = widgetEvent.value.toString())
            }
            is WidgetEvent.SwitchToggled -> {
                widgetManager.publish(widget = widgetEvent.widget, message = widgetEvent.state.toString())
            }
            is WidgetEvent.ToggleButtonChanged -> {
                widgetManager.publish(widget = widgetEvent.widget, message = widgetEvent.state.toString())
            }
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
        container.intent { reduce { copy(loading = true) } }

        container.intent(dispatcher = Dispatchers.IO) {
            environmentManager.loadEnvironments().collectLatest {
                reduce { copy(environments = it) }
            }
        }

        container.intent(dispatcher = Dispatchers.IO) {
            val environment = environmentManager.loadPreviousEnvironment()

            if (environment == null) return@intent

            reduce { copy(environment = environment) }
            refreshWidgets(environmentId = environment.id)
        }

        container.intent(dispatcher = Dispatchers.IO) {
            delay(250)
            reduce { copy(loading = false) }

            mqttClientRepository.isConnected().collect { isConnected ->
                if (!isConnected) {
                    postSideEffect(
                        effect = DashboardSideEffect.ShowInfo(info = DashboardInfo.DISCONNECTED)
                    )
                }
            }
        }
    }

    private fun onUpdateCanvasState(scale: Float, offsetX: Float, offsetY: Float) =
        container.intent(dispatcher = Dispatchers.IO) {
            val currentEnvironment = state.value.environment.copy(
                scale = scale,
                offsetX = offsetX,
                offsetY = offsetY
            )
            environmentManager.updateEnvironmentState(environment = currentEnvironment)
        }

    private fun observeMessages() = container.intent(dispatcher = Dispatchers.IO) {
        widgetManager.observeMessages().collect { (topic, message) ->
            val updateWidgets = state.value.widgets.map { widget ->
                if ((widget.dataSource as DataSource.MQTT).topic == topic) {
                    val updated = widget.copy(value = message)
                    widgetValues[widget.id] = message
                    updated
                } else widget
            }
            reduce { copy(widgets = updateWidgets) }
        }
    }

    private fun refreshWidgets(environmentId: Long) {
        loadWidgets(environmentId = environmentId)
        observeMessages()
    }

    private fun exportWidgets(stream: OutputStream) =
        container.intent(dispatcher = Dispatchers.IO) {
            try {
                val environmentId = state.value.environment.id.takeIf { it != 0L }
                    ?: environmentRepository.getLastEnvironmentId()
                    ?: return@intent postSideEffect(
                        effect = DashboardSideEffect.ShowInfo(info = DashboardInfo.EXPORT_FAILURE)
                    )

                val layout = widgetManager.exportLayout(environmentId = environmentId)
                    ?: return@intent postSideEffect(
                        effect = DashboardSideEffect.ShowInfo(info = DashboardInfo.EXPORT_FAILURE)
                    )

                val json = Json.encodeToString(layout)
                stream.bufferedWriter().use { it.write(json) }

                postSideEffect(
                    effect = DashboardSideEffect.ShowInfo(info = DashboardInfo.EXPORT_SUCCESS)
                )
            } catch (_: Exception) {
                postSideEffect(
                    effect = DashboardSideEffect.ShowInfo(info = DashboardInfo.EXPORT_FAILURE)
                )
            }
        }

    private fun importWidgets(stream: InputStream) = container.intent(dispatcher = Dispatchers.IO) {
        try {
            val json = stream.bufferedReader().use { it.readText() }
            val layout = Json.decodeFromString<LayoutConfig>(json)

            widgetManager.importLayout(layout = layout)

            environmentRepository.getLast()?.let { environment ->
                reduce { copy(environment = environment) }
                val widgets = widgetManager.loadWidgets(environmentId = environment.id).map {
                    it.copy(value = widgetValues[it.id] ?: "")
                }
                reduce { copy(widgets = widgets) }
                widgetManager.subscribeToTopics(
                    topics = widgets.mapNotNull { (it.dataSource as? DataSource.MQTT)?.topic }
                )

                postSideEffect(
                    effect = DashboardSideEffect.ShowInfo(info = DashboardInfo.IMPORT_SUCCESS)
                )
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