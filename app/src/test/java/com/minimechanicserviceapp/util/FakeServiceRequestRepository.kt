package com.minimechanicserviceapp.util

import com.minimechanicserviceapp.core.result.AppResult
import com.minimechanicserviceapp.domain.model.ServiceRequest
import com.minimechanicserviceapp.domain.model.ServiceRequestReceipt
import com.minimechanicserviceapp.domain.repository.ServiceRequestRepository

class FakeServiceRequestRepository : ServiceRequestRepository {

    var result: AppResult<ServiceRequestReceipt> =
        AppResult.Success(ServiceRequestReceipt(id = "51", mechanicId = "1"))

    /** Every request the use case handed over, so normalisation can be asserted. */
    val submitted = mutableListOf<ServiceRequest>()

    override suspend fun submit(
        request: ServiceRequest,
    ): AppResult<ServiceRequestReceipt> {
        submitted += request
        return result
    }
}
