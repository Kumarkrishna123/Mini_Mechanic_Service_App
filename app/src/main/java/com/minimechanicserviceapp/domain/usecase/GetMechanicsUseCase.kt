package com.minimechanicserviceapp.domain.usecase

import com.minimechanicserviceapp.domain.model.Mechanic
import com.minimechanicserviceapp.domain.repository.MechanicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMechanicsUseCase @Inject constructor(
    private val repository: MechanicRepository,
) {
    operator fun invoke(): Flow<List<Mechanic>> = repository.observeMechanics()
}