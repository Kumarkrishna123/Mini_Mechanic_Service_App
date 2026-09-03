package com.minimechanicserviceapp.domain.repository

import com.minimechanicserviceapp.core.result.AppResult
import com.minimechanicserviceapp.domain.model.ServiceRequest
import com.minimechanicserviceapp.domain.model.ServiceRequestReceipt

interface ServiceRequestRepository {
    suspend fun submit(request: ServiceRequest): AppResult<ServiceRequestReceipt>
}