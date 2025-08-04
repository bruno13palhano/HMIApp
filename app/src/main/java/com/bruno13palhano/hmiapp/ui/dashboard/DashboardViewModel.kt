package com.bruno13palhano.hmiapp.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bruno13palhano.core.data.repository.MqttClientRepository
import com.bruno13palhano.core.data.repository.WidgetRepository
import com.bruno13palhano.core.model.DataSource
import com.bruno13palhano.core.model.Widget
import com.bruno13palhano.hmiapp.ui.shared.Container
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
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
        container.intent {
            when (event) {
                DashboardEvent.Init -> dashboardInit()
                is DashboardEvent.AddWidget -> addWidget(widget = event.widget)
                is DashboardEvent.RemoveWidget -> removeWidget(id = event.id)
                is DashboardEvent.MoveWidget -> moveWidget(id = event.id, x = event.x, y = event.y)
                DashboardEvent.ToggleMenu -> toggleMenu()
            }
        }
    }

    private fun addWidget(widget: Widget) = container.intent {
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
}