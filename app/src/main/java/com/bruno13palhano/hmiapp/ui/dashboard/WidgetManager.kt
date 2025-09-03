package com.bruno13palhano.hmiapp.ui.dashboard

import com.bruno13palhano.core.data.configuration.LayoutConfig
import com.bruno13palhano.core.data.configuration.toEnvironment
import com.bruno13palhano.core.data.configuration.toEnvironmentConfig
import com.bruno13palhano.core.data.configuration.toWidget
import com.bruno13palhano.core.data.configuration.toWidgetConfig
import com.bruno13palhano.core.data.repository.EnvironmentRepository
import com.bruno13palhano.core.data.repository.WidgetRepository
import com.bruno13palhano.core.model.Environment
import com.bruno13palhano.core.model.Widget
import com.bruno13palhano.hmiapp.ui.components.WidgetEvent
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

class WidgetManager(
    private val widgetRepository: WidgetRepository,
    private val environmentRepository: EnvironmentRepository
) {
    suspend fun addWidget(widget: Widget) {
        widgetRepository.insert(widget = widget)
    }

    suspend fun editWidget(widget: Widget) {
        widgetRepository.update(widget = widget)
    }

    suspend fun updateWidgetPin(widget: Widget) {
        widgetRepository.update(widget = widget)
    }

    suspend fun removeWidget(id: String) {
        widgetRepository.deleteById(id = id)
    }

    suspend fun onWidgetDragEnd(id: String, x: Float, y: Float) {
        widgetRepository.updatePosition(id = id, x = x, y = y)
    }

    suspend fun loadWidgets(environmentId: Long): List<Widget> {
        return widgetRepository.getWidgets(environmentId = environmentId)
    }

    fun onWidgetEvent(
        widgetEvent: WidgetEvent,
        publishWidgetEvent: (widget: Widget, message: String) -> Unit
    ) {
        when (widgetEvent) {
            is WidgetEvent.ButtonClicked -> publishWidgetEvent(widgetEvent.widget, "1")
            is WidgetEvent.ColorPicked -> {
//                publishWidgetEvent(
//                    widgetEvent.widgetId,
//                    widgetEvent.color
//                )
            }
            is WidgetEvent.DropdownSelected -> {
                publishWidgetEvent(widgetEvent.widget, widgetEvent.selected)
            }
            is WidgetEvent.InputSubmitted -> {
                publishWidgetEvent(widgetEvent.widget, widgetEvent.text)
            }
            is WidgetEvent.SliderChanged -> {
                publishWidgetEvent(widgetEvent.widget, widgetEvent.value.toString())
            }
            is WidgetEvent.SwitchToggled -> {
                publishWidgetEvent(widgetEvent.widget, widgetEvent.state.toString())
            }
            is WidgetEvent.ToggleButtonChanged -> {
                publishWidgetEvent(widgetEvent.widget, widgetEvent.state.toString())
            }
        }
    }

    suspend fun exportWidgets(
        stream: OutputStream,
        onFail: () -> Unit,
        onSuccess: () -> Unit
    ) {
        val environmentId = environmentRepository.getLastEnvironmentId()
        if (environmentId == null) {
            onFail()
            return
        }

        val environment = environmentRepository.getById(id = environmentId)
        if (environment == null) {
            onFail()
            return
        }

        val widgets = widgetRepository.getWidgets(environmentId = environmentId)
            .map { it.toWidgetConfig() }
        val layout = LayoutConfig(
            environment = environment.toEnvironmentConfig(),
            widgets = widgets
        )

        try {
            val json = Json.encodeToString(value = layout)
            stream.bufferedWriter().use { it.write(json) }
            onSuccess()
        } catch (_: Exception) {
            onFail()
        }
    }

    suspend fun importWidgets(
        stream: InputStream,
        onFail: () -> Unit,
        onSuccess: (environment: Environment) -> Unit,
    ) {
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
            val importedEnv = environmentRepository.getLast()
            if (importedEnv == null) {
                onFail()
                return
            }
            onSuccess(importedEnv)
        } catch (_: Exception) {
            onFail()
        }
    }
}