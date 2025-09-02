package com.bruno13palhano.hmiapp.ui.dashboard

import com.bruno13palhano.core.data.configuration.LayoutConfig
import com.bruno13palhano.core.data.configuration.toEnvironment
import com.bruno13palhano.core.data.configuration.toEnvironmentConfig
import com.bruno13palhano.core.data.configuration.toWidget
import com.bruno13palhano.core.data.configuration.toWidgetConfig
import com.bruno13palhano.core.data.repository.EnvironmentRepository
import com.bruno13palhano.core.data.repository.MqttClientRepository
import com.bruno13palhano.core.data.repository.WidgetRepository
import com.bruno13palhano.core.model.DataSource
import com.bruno13palhano.core.model.Widget
import com.bruno13palhano.hmiapp.ui.components.WidgetEvent
import com.bruno13palhano.hmiapp.ui.shared.Container
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

class WidgetManager(
    private val widgetRepository: WidgetRepository,
    private val environmentRepository: EnvironmentRepository,
    private val mqttClientRepository: MqttClientRepository,
    private val container: Container<DashboardState, DashboardSideEffect>
) {
    private val widgetValues = mutableMapOf<String, String>()

    fun addWidget() = container.intent(dispatcher = Dispatchers.IO) {
        reduce { copy(isWidgetInputDialogVisible = false) }
        val environmentId = state.value.environment.id.takeIf { it != 0L } ?: return@intent

        val widget = Widget(
            type = state.value.type,
            label = state.value.label,
            dataSource = DataSource.MQTT(topic = state.value.endpoint),
            environmentId = environmentId
        )

        widgetRepository.insert(widget = widget)
        refreshWidgets(environmentId = environmentId)
        clearCurrentWidget()
    }

    fun editWidget() = container.intent(dispatcher = Dispatchers.IO) {
        reduce { copy(isWidgetInputDialogVisible = false) }

        val environmentId = state.value.environment.id.takeIf { it != 0L } ?: return@intent
        val widget = state.value.widgets.find { it.id == state.value.id } ?: return@intent

        val updated = widget.copy(
            type = state.value.type,
            label = state.value.label,
            dataSource = DataSource.MQTT(topic = state.value.endpoint)
        )

        widgetRepository.update(widget = updated)
        refreshWidgets(environmentId = environmentId)
        clearCurrentWidget()
    }

    fun updateWidgetPin(widget: Widget) = container.intent(dispatcher = Dispatchers.IO) {
        val environmentId = state.value.environment.id.takeIf { it != 0L } ?: return@intent
        val newWidget = widget.copy(isPinned = !widget.isPinned)

        widgetRepository.update(widget = newWidget)
        refreshWidgets(environmentId = environmentId)
        clearCurrentWidget()
    }

    fun clearCurrentWidget() = container.intent {
        reduce { copy(id = "", label = "", endpoint = "") }
    }

    fun removeWidget(id: String) = container.intent(dispatcher = Dispatchers.IO) {
        val environmentId = state.value.environment.id.takeIf { it != 0L } ?: return@intent
        widgetValues.remove(key = id)
        widgetRepository.deleteById(id = id)
        loadWidgets(environmentId = environmentId)
    }

    fun onWidgetDragEnd(id: String, x: Float, y: Float) =
        container.intent(dispatcher = Dispatchers.IO) {
            widgetRepository.updatePosition(id = id, x = x, y = y)
        }

    fun loadWidgets(environmentId: Long) = container.intent(dispatcher = Dispatchers.IO) {
        widgetRepository.getWidgets(environmentId = environmentId).let { widgets ->
            val updateWidgets = widgets.map { widget ->
                val value = widgetValues[widget.id] ?: ""
                widget.copy(value = value)
            }

            reduce { copy(widgets = updateWidgets) }
            val topics = widgets.mapNotNull {
                (it.dataSource as? DataSource.MQTT)?.topic
            }

            topics.forEach { topic -> mqttClientRepository.subscribeToTopic(topic) }
        }
    }

    fun observeMessages() = container.intent(dispatcher = Dispatchers.IO) {
        mqttClientRepository.incomingMessages().collect { (topic, message) ->
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

    fun onWidgetEvent(widgetEvent: WidgetEvent) {
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

    fun publishWidgetEvent(widgetId: String, message: String) =
        container.intent(dispatcher = Dispatchers.IO) {
            val widget = state.value.widgets.find { it.id == widgetId } ?: return@intent

            mqttClientRepository.publish(
                topic = (widget.dataSource as DataSource.MQTT).topic,
                message = message
            )
        }

    fun exportWidgets(stream: OutputStream) =
        container.intent(dispatcher = Dispatchers.Default) {
            val environmentId = environmentRepository.getLastEnvironmentId()
            if (environmentId == null) {
                postSideEffect(
                    effect = DashboardSideEffect.ShowInfo(info = DashboardInfo.EXPORT_FAILURE)
                )
                return@intent
            }

            val environment = environmentRepository.getById(id = environmentId) ?: return@intent
            val widgets = widgetRepository.getWidgets(environmentId = environmentId)
                .map { it.toWidgetConfig() }
            val layout = LayoutConfig(
                environment = environment.toEnvironmentConfig(),
                widgets = widgets
            )

            try {
                val json = Json.encodeToString(value = layout)
                stream.bufferedWriter().use { it.write(json) }
            } catch (_: Exception) {
                postSideEffect(
                    effect = DashboardSideEffect.ShowInfo(info = DashboardInfo.EXPORT_FAILURE)
                )
            }
        }

    fun importWidgets(stream: InputStream) =
        container.intent(dispatcher = Dispatchers.Default) {
            try {
                val json = stream.bufferedReader().use { it.readText() }
                val layout = Json.decodeFromString<LayoutConfig>(string = json)

                environmentRepository.insert(environment = layout.environment.toEnvironment())
                layout.widgets
                    .map { it.toWidget() }
                    .forEach { widget ->
                        widgetRepository.insert(widget = widget)
                    }

                // This is necessary to load the data if the Activity isn't recreated
                environmentRepository.getLast()?.let {
                    reduce { copy(environment = it) }
                    refreshWidgets(environmentId = it.id)
                }
            } catch (_: Exception) {
                postSideEffect(
                    effect = DashboardSideEffect.ShowInfo(info = DashboardInfo.IMPORT_FAILURE)
                )
            }
        }

    fun refreshWidgets(environmentId: Long) {
        loadWidgets(environmentId = environmentId)
        observeMessages()
    }
}