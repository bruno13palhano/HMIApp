package com.bruno13palhano.hmiapp.ui.dashboard

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import com.bruno13palhano.core.model.Widget
import com.bruno13palhano.core.model.WidgetType
import com.bruno13palhano.hmiapp.ui.navigation.Dashboard

@Immutable
data class DashboardState(
    val widgets: List<Widget> = emptyList(),
    val label: String = "",
    val endpoint: String = "",
    val type: WidgetType = WidgetType.TEXT,
    val currentDestination: NavKey = Dashboard,
    val isGestureEnabled: Boolean = true,
    val isToolboxExpanded: Boolean = false,
    val isWidgetInputDialogVisible: Boolean = false,
)

@Immutable
sealed interface DashboardEvent {
    data object AddWidget : DashboardEvent
    data class RemoveWidget(val id: String) : DashboardEvent
    data class MoveWidget(val id: String, val x: Float, val y: Float) : DashboardEvent
    data class  UpdateLabel(val label: String) : DashboardEvent
    data class UpdateEndpoint(val endpoint: String) : DashboardEvent
    data class ShowWidgetDialog(val type: WidgetType) : DashboardEvent
    data class NavigateTo(val destination: NavKey) : DashboardEvent
    data object HideWidgetConfig : DashboardEvent
    data object ToggleIsToolboxExpanded : DashboardEvent
    data object ToggleMenu : DashboardEvent
    data object Init : DashboardEvent
}

@Immutable
sealed interface DashboardSideEffect {
    data object ToggleMenu : DashboardSideEffect
    data class ShowInfo(val info: DashboardInfo) : DashboardSideEffect
    data class NavigateTo(val destination: NavKey) : DashboardSideEffect
}

enum class DashboardInfo {
    DISCONNECTED
}