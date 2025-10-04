package com.bruno13palhano.hmiapp.ui.dashboard

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import com.bruno13palhano.core.model.Environment
import com.bruno13palhano.core.model.Widget
import com.bruno13palhano.core.model.WidgetType
import com.bruno13palhano.hmiapp.ui.components.WidgetEvent
import com.bruno13palhano.hmiapp.ui.navigation.Dashboard
import java.io.InputStream
import java.io.OutputStream

@Immutable
data class DashboardState(
    val loading: Boolean = true,
    val widgets: List<Widget> = emptyList(),
    val environments: List<Environment> = emptyList(),
    val environment: Environment =
        Environment(
            0L,
            "",
            1f,
            0f,
            0f
        ),
    val id: String = "",
    val label: String = "",
    val endpoint: String = "",
    val hasExtras: Boolean = false,
    val extras: List<String> = emptyList(),
    val type: WidgetType = WidgetType.TEXT,
    val isWidgetWithLimit: Boolean = false,
    val enableLimit: Boolean = false,
    val limit: String? = null,
    val isPinned: Boolean = false,
    val currentDestination: NavKey = Dashboard,
    val isEditEnvironmentName: Boolean = false,
    val isGestureEnabled: Boolean = false,
    val isToolboxExpanded: Boolean = false,
    val isDashboardOptionsExpanded: Boolean = false,
    val isVertMenuVisible: Boolean = false,
    val isWidgetInputDialogVisible: Boolean = false,
    val isEnvironmentDialogVisible: Boolean = false,
)

@Immutable
sealed interface DashboardEvent {
    data object ConfirmWidget : DashboardEvent
    data class RemoveWidget(val id: String) : DashboardEvent
    data class OnWidgetDragEnd(val id: String, val x: Float, val y: Float) : DashboardEvent
    data class OpenEditWidgetDialog(val id: String) : DashboardEvent
    data class OnWidgetEvent(val widgetEvent: WidgetEvent) : DashboardEvent
    data class OnUpdateCanvasState(
        val scale: Float,
        val offsetX: Float,
        val offsetY: Float
    ) : DashboardEvent
    data class UpdateLabel(val label: String) : DashboardEvent
    data class UpdateEndpoint(val endpoint: String) : DashboardEvent
    data class UpdateExtra(val index: Int, val value: String) : DashboardEvent
    data class UpdateLimit(val limit: String) : DashboardEvent
    data object AddExtra : DashboardEvent
    data class UpdateEnvironmentName(val name: String) : DashboardEvent
    data class OnToggleWidgetPin(val id: String) : DashboardEvent
    data object AddEnvironment : DashboardEvent
    data object EditEnvironment : DashboardEvent
    data class ChangeEnvironment(val id: Long) : DashboardEvent
    data class ShowWidgetDialog(val type: WidgetType) : DashboardEvent
    data class NavigateTo(val destination: NavKey) : DashboardEvent
    data class ExportWidgetsConfig(val stream: OutputStream) : DashboardEvent
    data class ImportWidgetsConfig(val stream: InputStream) : DashboardEvent
    data object HideWidgetConfig : DashboardEvent
    data object ToggleVertMenu : DashboardEvent
    data object ToggleDashboardOptions : DashboardEvent
    data class OnVertMenuItemClick(val item: ConfigurationOptions) : DashboardEvent
    data object ToggleIsToolboxExpanded : DashboardEvent
    data class OpenEnvironmentInputDialog(val isEdit: Boolean) : DashboardEvent
    data object CloseEnvironmentInputDialog : DashboardEvent
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
    EXPORT_SUCCESS,
    IMPORT_SUCCESS,
    EXPORT_FAILURE,
    IMPORT_FAILURE
}

enum class ConfigurationOptions {
    EXPORT,
    IMPORT
}