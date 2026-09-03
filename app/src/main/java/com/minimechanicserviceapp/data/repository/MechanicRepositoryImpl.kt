package com.minimechanicserviceapp.data.repository

import com.minimechanicserviceapp.core.result.AppResult
import com.minimechanicserviceapp.core.result.DataError
import com.minimechanicserviceapp.core.result.toDataError
import com.minimechanicserviceapp.data.local.MechanicDao
import com.minimechanicserviceapp.data.mapper.toDomain
import com.minimechanicserviceapp.data.mapper.toEntityOrNull
import com.minimechanicserviceapp.data.remote.MechanicApi
import com.minimechanicserviceapp.domain.model.Mechanic
import com.minimechanicserviceapp.domain.repository.MechanicRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MechanicRepositoryImpl @Inject constructor(
    private val api : MechanicApi,
    private val dao: MechanicDao
) : MechanicRepository{
    override fun observeMechanics(): Flow<List<Mechanic>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }


    override fun observeMechanic(id: String): Flow<Mechanic?> =
        dao.observeById(id).map { it?.toDomain()}


    override suspend fun refreshMechanics(): AppResult<Unit>  = try{
        val entities = api.getMechanics().mapNotNull { it.toEntityOrNull() }
        dao.replaceAll(entities)
        AppResult.Success(Unit)
    }catch ( e  : CancellationException){
        throw  e
    }catch (e : Exception){
        AppResult.Failure(e.toDataError())
    }


}