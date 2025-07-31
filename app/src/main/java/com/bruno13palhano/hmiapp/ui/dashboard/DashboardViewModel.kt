package com.bruno13palhano.hmiapp.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bruno13palhano.core.data.database.dao.WidgetDao
import com.bruno13palhano.core.data.database.entity.toDomain
import com.bruno13palhano.core.data.database.entity.toEntity
import com.bruno13palhano.core.data.repository.WidgetRepository
import com.bruno13palhano.hmiapp.ui.shared.Container
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: WidgetRepository
) : ViewModel() {
    val container: Container<DashboardState, DashboardSideEffect> = Container(
        initialSTATE = DashboardState(),
        scope = viewModelScope
    )

    init {
        loadWidgets()
    }

    fun onEvent(event: DashboardEvent) {
        container.intent {
            when (event) {
                is DashboardEvent.AddWidget -> {
                    repository.insert(event.widget)
                }
                is DashboardEvent.RemoveWidget -> {
                    repository.deleteById(id = event.id)
                }

                is DashboardEvent.MoveWidget -> {
                    repository.updatePosition(id = event.id, x = event.x, y = event.y)
                }
            }
        }
    }

    private fun loadWidgets() = container.intent {
        repository.getAll().collect {
            reduce { copy(widgets = it) }
        }
    }
}