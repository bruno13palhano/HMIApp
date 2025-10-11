package com.bruno13palhano.core.data.repository

import com.bruno13palhano.core.data.database.dao.WidgetDao
import com.bruno13palhano.core.data.database.entity.toDomain
import com.bruno13palhano.core.data.database.entity.toEntity
import com.bruno13palhano.core.model.Widget
import javax.inject.Inject

internal class WidgetRepositoryImpl @Inject constructor(private val dao: WidgetDao) :
    WidgetRepository {
    override suspend fun getWidgets(environmentId: Long): List<Widget> =
        dao.getWidgets(environmentId = environmentId).map { it.toDomain() }

    override suspend fun insert(widget: Widget) {
        dao.insert(entity = widget.toEntity())
    }

    override suspend fun update(widget: Widget) {
        dao.update(entity = widget.toEntity())
    }

    override suspend fun deleteById(id: String) {
        dao.deleteById(id = id)
    }

    override suspend fun updatePosition(id: String, x: Float, y: Float) {
        dao.updatePosition(id = id, x = x, y = y)
    }
}
