package com.minimechanicserviceapp.util

import com.minimechanicserviceapp.data.local.MechanicDao
import com.minimechanicserviceapp.data.local.entity.MechanicEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory stand-in for Room. `replaceAll` is not overridden on purpose so the
 * interface's own clear-then-insert body is what runs, same as in production.
 */
class FakeMechanicDao : MechanicDao {

    private val rows = MutableStateFlow<List<MechanicEntity>>(emptyList())

    override fun observeAll(): Flow<List<MechanicEntity>> = rows.asStateFlow()

    override fun observeById(id: String): Flow<MechanicEntity?> =
        rows.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun upsertAll(mechanics: List<MechanicEntity>) {
        val byId = rows.value.associateBy { it.id }.toMutableMap()
        mechanics.forEach { byId[it.id] = it }
        rows.value = byId.values.toList()
    }

    override suspend fun clear() {
        rows.value = emptyList()
    }

    fun current(): List<MechanicEntity> = rows.value
}
