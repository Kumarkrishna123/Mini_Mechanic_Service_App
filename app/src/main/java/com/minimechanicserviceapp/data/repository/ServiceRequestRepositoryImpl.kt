package com.minimechanicserviceapp.data.repository

import com.minimechanicserviceapp.core.result.AppResult
import com.minimechanicserviceapp.core.result.toDataError
import com.minimechanicserviceapp.data.remote.MechanicApi
import com.minimechanicserviceapp.data.remote.dto.ServiceRequestBody
import com.minimechanicserviceapp.domain.model.ServiceRequest
import com.minimechanicserviceapp.domain.model.ServiceRequestReceipt
import com.minimechanicserviceapp.domain.repository.ServiceRequestRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceRequestRepositoryImpl @Inject constructor(
    private val api : MechanicApi
) : ServiceRequestRepository {
    override suspend fun submit(request: ServiceRequest): AppResult<ServiceRequestReceipt> = try{
        val  response =  api.submitServiceRequest(
            ServiceRequestBody(
                mechanicId = request.mechanicId,
                customerName = request.customerName,
                phoneNumber = request.phoneNumber,
                vehicleNumber = request.vehicleNumber,
                serviceType = request.serviceType,
                problemDescription = request.problemDescription,
            )
        )
        AppResult.Success(
            ServiceRequestReceipt(
                id = response.id ,
                mechanicId = response.mechanicId ?: request.mechanicId
            )
        )
    }catch (e : CancellationException){
        throw e
    }catch (e : Exception){
        AppResult.Failure(e.toDataError())
    }






}