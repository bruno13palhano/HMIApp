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
import com.bruno13palhano.core.model.WidgetType
import kotlinx.coroutines.flow.Flow

class WidgetManager(
    private val widgetRepository: WidgetRepository,
    private val environmentRepository: EnvironmentRepository,
    private val mqttClientRepository: MqttClientRepository
) {
    suspend fun addWidget(
        environmentId: Long,
        type: WidgetType,
        label: String,
        endpoint: String,
        limit: String?,
        extras: List<String>?
    ) {
        val widget = Widget(
            type = type,
            label = label,
            dataSource = DataSource.MQTT(topic = endpoint),
            environmentId = environmentId,
            limit = limit,
            extras = extras
        )
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

    fun observeMessages(): Flow<Pair<String, String>> {
        return mqttClientRepository.incomingMessages()
    }

    suspend fun subscribeToTopics(topics: List<String>) {
        topics.forEach { topic -> mqttClientRepository.subscribeToTopic(topic = topic) }
    }

    suspend fun publish(widget: Widget, message: String) {
        (widget.dataSource as? DataSource.MQTT)?.let {
            mqttClientRepository.publish(topic = it.topic, message)
        }
    }

    suspend fun exportLayout(environmentId: Long): LayoutConfig? {
        val environment = environmentRepository.getById(id = environmentId) ?: return null
        val widgets = widgetRepository.getWidgets(environmentId = environmentId)
            .map { it.toWidgetConfig() }
        return LayoutConfig(
            environment = environment.toEnvironmentConfig(),
            widgets = widgets
        )
    }

    suspend fun importLayout(layout: LayoutConfig) {
        environmentRepository.insert(environment = layout.environment.toEnvironment())
        layout.widgets
            .map { it.toWidget() }
            .forEach { widgetRepository.insert(widget = it) }
    }
}