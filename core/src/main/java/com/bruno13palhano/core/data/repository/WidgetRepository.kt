package com.bruno13palhano.core.data.repository

import com.bruno13palhano.core.model.Widget

interface WidgetRepository {
    suspend fun getWidgets(environmentId: Long): List<Widget>
    suspend fun insert(widget: Widget)
    suspend fun update(widget: Widget)
    suspend fun deleteById(id: String)
    suspend fun updatePosition(id: String, x: Float, y: Float)
}
