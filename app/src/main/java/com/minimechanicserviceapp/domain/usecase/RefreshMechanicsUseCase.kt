package com.minimechanicserviceapp.domain.usecase

import com.minimechanicserviceapp.core.result.AppResult
import com.minimechanicserviceapp.domain.repository.MechanicRepository
import javax.inject.Inject

class RefreshMechanicsUseCase @Inject constructor(
    private val repository: MechanicRepository,
) {
    suspend operator fun invoke(): AppResult<Unit> = repository.refreshMechanics()
}