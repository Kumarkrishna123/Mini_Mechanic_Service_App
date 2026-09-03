package com.minimechanicserviceapp.presentation.request

import com.minimechanicserviceapp.core.result.DataError
import com.minimechanicserviceapp.domain.validation.FieldError

data class RequestUiState(
    val mechanicName: String = "",
    val availableServices: List<String> = emptyList(),
    val customerName: String = "",
    val phoneNumber: String = "",
    val vehicleNumber: String = "",
    val selectedService: String = "",
    val problemDescription: String = "",
    val nameError: FieldError? = null,
    val phoneError: FieldError? = null,
    val vehicleError: FieldError? = null,
    val serviceError: FieldError? = null,
    val descriptionError: FieldError? = null,
    val isSubmitting: Boolean = false,
    val submitError: DataError? = null,
    val receiptId: String? = null,
) {
    val isSubmitted: Boolean get() = receiptId != null

    val canSubmit: Boolean
        get() = !isSubmitting &&
            customerName.isNotBlank() &&
            phoneNumber.isNotBlank() &&
            vehicleNumber.isNotBlank() &&
            selectedService.isNotBlank() &&
            problemDescription.isNotBlank() &&
            nameError == null &&
            phoneError == null &&
            vehicleError == null &&
            serviceError == null &&
            descriptionError == null
}
