package com.minimechanicserviceapp.domain.usecase

import com.minimechanicserviceapp.core.result.AppResult
import com.minimechanicserviceapp.domain.model.ServiceRequest
import com.minimechanicserviceapp.domain.model.ServiceRequestReceipt
import com.minimechanicserviceapp.domain.repository.ServiceRequestRepository
import com.minimechanicserviceapp.domain.validation.ServiceRequestValidator
import javax.inject.Inject

class SubmitServiceRequestUseCase @Inject constructor(
    private val repository: ServiceRequestRepository,
) {
    suspend operator fun invoke(
        request: ServiceRequest,
    ): AppResult<ServiceRequestReceipt> = repository.submit(
        request.copy(
            customerName = request.customerName.trim(),
            phoneNumber = ServiceRequestValidator.normalisePhone(request.phoneNumber),
            vehicleNumber = ServiceRequestValidator.normaliseVehicle(request.vehicleNumber),
            problemDescription = request.problemDescription.trim(),
        )
    )
}
