package com.bruno13palhano.hmiapp.ui.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bruno13palhano.core.data.repository.MqttClientRepository
import com.bruno13palhano.core.data.repository.WidgetRepository
import com.bruno13palhano.core.model.DataSource
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

    init {
        loadWidgets()
        observeMessages()
    }

    fun onEvent(event: DashboardEvent) {
        container.intent {
            when (event) {
                is DashboardEvent.AddWidget -> {
                    widgetRepository.insert(event.widget)
                    if (event.widget.dataSource is DataSource.MQTT) {
                        mqttClientRepository.subscribeToTopic((event.widget.dataSource as DataSource.MQTT).topic)
                    }
                }
                is DashboardEvent.RemoveWidget -> {
                    widgetValues.remove(event.id)
                    widgetRepository.deleteById(id = event.id)
                }

                is DashboardEvent.MoveWidget -> {
                    widgetRepository.updatePosition(id = event.id, x = event.x, y = event.y)
                }

                DashboardEvent.ToggleMenu -> container.intent {
                    postSideEffect(effect = DashboardSideEffect.ToggleMenu)
                }
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
                Log.i("MQTT", "$topic: $message")
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