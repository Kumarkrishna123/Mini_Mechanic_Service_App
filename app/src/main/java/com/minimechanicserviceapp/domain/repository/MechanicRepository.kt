package com.minimechanicserviceapp.domain.repository

import com.minimechanicserviceapp.core.result.AppResult
import com.minimechanicserviceapp.domain.model.Mechanic
import kotlinx.coroutines.flow.*

interface MechanicRepository {
    fun observeMechanics(): Flow<List<Mechanic>>
    fun observeMechanic(id: String): Flow<Mechanic?>
    suspend fun refreshMechanics(): AppResult<Unit>
}