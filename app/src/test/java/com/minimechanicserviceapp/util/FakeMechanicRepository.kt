package com.minimechanicserviceapp.util

import com.minimechanicserviceapp.core.result.AppResult
import com.minimechanicserviceapp.domain.model.Mechanic
import com.minimechanicserviceapp.domain.repository.MechanicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeMechanicRepository(
    initial: List<Mechanic> = emptyList(),
) : MechanicRepository {

    private val cache = MutableStateFlow(initial)

    /** What [refreshMechanics] returns. Flip this to exercise failure paths. */
    var refreshResult: AppResult<Unit> = AppResult.Success(Unit)

    /** Rows written into the cache on a successful refresh, mimicking write-through. */
    var refreshPayload: List<Mechanic>? = null

    var refreshCount: Int = 0
        private set

    override fun observeMechanics(): Flow<List<Mechanic>> = cache.asStateFlow()

    override fun observeMechanic(id: String): Flow<Mechanic?> =
        cache.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun refreshMechanics(): AppResult<Unit> {
        refreshCount++
        if (refreshResult is AppResult.Success) {
            refreshPayload?.let { cache.value = it }
        }
        return refreshResult
    }
}
