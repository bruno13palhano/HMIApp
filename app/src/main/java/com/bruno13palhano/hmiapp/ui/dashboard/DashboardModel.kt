package com.bruno13palhano.hmiapp.ui.dashboard

import androidx.compose.runtime.Immutable
import com.bruno13palhano.core.model.Widget

@Immutable
data class DashboardState(val widgets: List<Widget> = emptyList())

@Immutable
sealed interface DashboardEvent {
    data class AddWidget(val widget: Widget) : DashboardEvent
    data class RemoveWidget(val id: String) : DashboardEvent
    data class MoveWidget(val id: String, val x: Float, val y: Float) : DashboardEvent
    data object ToggleMenu : DashboardEvent
    data object Init : DashboardEvent
}

@Immutable
sealed interface DashboardSideEffect {
    data object ToggleMenu : DashboardSideEffect
    data class ShowInfo(val info: DashboardInfo) : DashboardSideEffect
}

enum class DashboardInfo {
    DISCONNECTED
}