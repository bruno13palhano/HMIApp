package com.bruno13palhano.hmiapp.ui.dashboard

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import com.bruno13palhano.core.model.Widget
import com.bruno13palhano.core.model.WidgetType
import com.bruno13palhano.hmiapp.ui.components.WidgetEvent
import com.bruno13palhano.hmiapp.ui.navigation.Dashboard
import java.io.InputStream
import java.io.OutputStream

@Immutable
data class DashboardState(
    val widgets: List<Widget> = emptyList(),
    val id: String = "",
    val label: String = "",
    val endpoint: String = "",
    val type: WidgetType = WidgetType.TEXT,
    val currentDestination: NavKey = Dashboard,
    val isGestureEnabled: Boolean = true,
    val isToolboxExpanded: Boolean = false,
    val isVertMenuVisible: Boolean = false,
    val isWidgetInputDialogVisible: Boolean = false,
)

@Immutable
sealed interface DashboardEvent {
    data object AddWidget : DashboardEvent
    data object EditWidget: DashboardEvent
    data class RemoveWidget(val id: String) : DashboardEvent
    data class MoveWidget(val id: String, val x: Float, val y: Float) : DashboardEvent
    data class OpenEditWidgetDialog(val id: String) : DashboardEvent
    data class OnWidgetEvent(val widgetEvent: WidgetEvent) : DashboardEvent
    data class UpdateLabel(val label: String) : DashboardEvent
    data class UpdateEndpoint(val endpoint: String) : DashboardEvent
    data class ShowWidgetDialog(val type: WidgetType) : DashboardEvent
    data class NavigateTo(val destination: NavKey) : DashboardEvent
    data class ExportWidgetsConfig(val stream: OutputStream) : DashboardEvent
    data class ImportWidgetsConfig(val stream: InputStream) : DashboardEvent
    data object HideWidgetConfig : DashboardEvent
    data object ToggleVertMenu : DashboardEvent
    data class OnVertMenuItemClick(val item: ConfigurationOptions) : DashboardEvent
    data object ToggleIsToolboxExpanded : DashboardEvent
    data object ToggleMenu : DashboardEvent
    data object Init : DashboardEvent
}

@Immutable
sealed interface DashboardSideEffect {
    data object ToggleMenu : DashboardSideEffect
    data class ShowInfo(val info: DashboardInfo) : DashboardSideEffect
    data class NavigateTo(val destination: NavKey) : DashboardSideEffect
    data object LaunchExportWidgetsConfig : DashboardSideEffect
    data object LaunchImportWidgetsConfig : DashboardSideEffect
}

enum class DashboardInfo {
    DISCONNECTED,
    EXPORT_FAILURE,
    IMPORT_FAILURE
}

enum class ConfigurationOptions {
    EXPORT,
    IMPORT
}