package com.bruno13palhano.hmiapp.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import com.bruno13palhano.core.data.configuration.LayoutConfig
import com.bruno13palhano.core.data.configuration.toWidget
import com.bruno13palhano.core.data.configuration.toWidgetConfig
import com.bruno13palhano.core.data.repository.MqttClientRepository
import com.bruno13palhano.core.data.repository.WidgetRepository
import com.bruno13palhano.core.model.DataSource
import com.bruno13palhano.core.model.Widget
import com.bruno13palhano.core.model.WidgetType
import com.bruno13palhano.hmiapp.ui.shared.Container
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val widgetRepository: WidgetRepository,
    private val mqttClientRepository: MqttClientRepository
) : ViewModel() {
    val container: Container<DashboardState, DashboardSideEffect> = Container(
        initialSTATE = DashboardState(),
        scope = viewModelScope
    )

    private val widgetValues = mutableMapOf<String, String>()

    fun onEvent(event: DashboardEvent) {
        when (event) {
            DashboardEvent.Init -> dashboardInit()
            is DashboardEvent.AddWidget -> addWidget()
            is DashboardEvent.RemoveWidget -> removeWidget(id = event.id)
            is DashboardEvent.MoveWidget -> moveWidget(id = event.id, x = event.x, y = event.y)
            DashboardEvent.ToggleIsToolboxExpanded -> toggleIsToolboxExpanded()
            DashboardEvent.ToggleMenu -> toggleMenu()
            DashboardEvent.HideWidgetConfig -> hideWidgetDialog()
            is DashboardEvent.ShowWidgetDialog -> showWidgetDialog(type = event.type)
            is DashboardEvent.UpdateEndpoint -> updateEndpoint(endpoint = event.endpoint)
            is DashboardEvent.UpdateLabel -> updateLabel(label = event.label)
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
        reduce { copy(isWidgetInputDialogVisible = true, type = type) }
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

    private fun addWidget() = container.intent {
        hideWidgetDialog()

        val state = state.value
        val widget = Widget(
            type = state.type,
            label = state.label,
            dataSource = DataSource.MQTT(topic = state.endpoint)
        )

        widgetRepository.insert(widget)
        if (widget.dataSource is DataSource.MQTT) {
            mqttClientRepository.subscribeToTopic(
                topic = (widget.dataSource as DataSource.MQTT).topic
            )
        }
    }

    private fun removeWidget(id: String) = container.intent {
        widgetValues.remove(key = id)
        widgetRepository.deleteById(id = id)
    }

    private fun moveWidget(id: String, x: Float, y: Float) = container.intent {
        widgetRepository.updatePosition(id = id, x = x, y = y)
    }

    private fun toggleIsToolboxExpanded() = container.intent {
        reduce { copy(isToolboxExpanded = !isToolboxExpanded) }
    }

    private fun toggleMenu() = container.intent {
        postSideEffect(effect = DashboardSideEffect.ToggleMenu)
    }

    private fun dashboardInit() = container.intent {
        mqttClientRepository.isConnected().collect { isConnected ->
            if (!isConnected) {
                postSideEffect(
                    effect = DashboardSideEffect.ShowInfo(info = DashboardInfo.DISCONNECTED)
                )
            } else {
                loadWidgets()
                observeMessages()
            }
        }
    }

    private fun loadWidgets() = container.intent {
        widgetRepository.getAll().collectLatest { widgets ->
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
        container.intent {
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

    private fun exportWidgets(stream: OutputStream) = container.intent {
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

    private fun importWidgets(stream: InputStream) = container.intent {
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