package com.bruno13palhano.core.data.repository

import com.bruno13palhano.core.data.database.dao.WidgetDao
import com.bruno13palhano.core.data.database.entity.toDomain
import com.bruno13palhano.core.data.database.entity.toEntity
import com.bruno13palhano.core.model.Widget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class WidgetRepositoryImpl @Inject constructor(
    private val dao: WidgetDao
) : WidgetRepository {
    override fun getAll(): Flow<List<Widget>> {
        return dao.getAll().map { it.map { entity -> entity.toDomain() } }
    }

    override suspend fun insert(widget: Widget) {
        dao.insert(entity = widget.toEntity())
    }

    override suspend fun deleteById(id: String) {
        dao.deleteById(id = id)
    }

    override suspend fun updatePosition(id: String, x: Float, y: Float) {
        dao.updatePosition(id = id, x = x, y = y)
    }
}