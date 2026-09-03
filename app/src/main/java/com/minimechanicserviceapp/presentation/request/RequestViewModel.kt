package com.minimechanicserviceapp.presentation.request

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimechanicserviceapp.core.result.AppResult
import com.minimechanicserviceapp.domain.model.ServiceRequest
import com.minimechanicserviceapp.domain.usecase.GetMechanicByIdUseCase
import com.minimechanicserviceapp.domain.usecase.SubmitServiceRequestUseCase
import com.minimechanicserviceapp.domain.validation.ServiceRequestValidator
import com.minimechanicserviceapp.presentation.navigation.RequestServiceDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RequestViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getMechanicById: GetMechanicByIdUseCase,
    private val submitServiceRequest: SubmitServiceRequestUseCase,
) : ViewModel() {

    private val mechanicId: String =
        checkNotNull(savedStateHandle[RequestServiceDestination::mechanicId.name])

    private val _uiState = MutableStateFlow(RequestUiState())
    val uiState: StateFlow<RequestUiState> = _uiState.asStateFlow()

    private var submitJob: Job? = null

    init {
        viewModelScope.launch {
            getMechanicById(mechanicId).collect { mechanic ->
                if (mechanic == null) return@collect
                _uiState.update { state ->
                    state.copy(
                        mechanicName = mechanic.name,
                        availableServices = mechanic.services,
                        selectedService = state.selectedService.ifBlank {
                            mechanic.services.firstOrNull().orEmpty()
                        },
                    )
                }
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update {
        it.copy(customerName = value, nameError = null)
    }

    fun onPhoneChange(value: String) = _uiState.update {
        it.copy(phoneNumber = value, phoneError = null)
    }

    fun onVehicleChange(value: String) = _uiState.update {
        it.copy(vehicleNumber = value, vehicleError = null)
    }

    fun onServiceChange(value: String) = _uiState.update {
        it.copy(selectedService = value, serviceError = null)
    }

    fun onDescriptionChange(value: String) {
        if (value.length > ServiceRequestValidator.DESCRIPTION_MAX_LENGTH) return
        _uiState.update { it.copy(problemDescription = value, descriptionError = null) }
    }

    fun onNameBlur() = _uiState.update {
        it.copy(nameError = ServiceRequestValidator.validateName(it.customerName))
    }

    fun onPhoneBlur() = _uiState.update {
        it.copy(phoneError = ServiceRequestValidator.validatePhone(it.phoneNumber))
    }

    fun onVehicleBlur() = _uiState.update {
        it.copy(vehicleError = ServiceRequestValidator.validateVehicle(it.vehicleNumber))
    }

    fun onDescriptionBlur() = _uiState.update {
        it.copy(
            descriptionError =
                ServiceRequestValidator.validateDescription(it.problemDescription),
        )
    }

    fun onSubmit() {
        if (submitJob?.isActive == true) return

        val validated = validateAll()
        if (!validated.hasNoErrors) return

        submitJob = viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitError = null) }

            val request = ServiceRequest(
                mechanicId = mechanicId,
                customerName = validated.state.customerName,
                phoneNumber = validated.state.phoneNumber,
                vehicleNumber = validated.state.vehicleNumber,
                serviceType = validated.state.selectedService,
                problemDescription = validated.state.problemDescription,
            )

            when (val result = submitServiceRequest(request)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(isSubmitting = false, receiptId = result.data.id)
                }

                is AppResult.Failure -> _uiState.update {
                    it.copy(isSubmitting = false, submitError = result.error)
                }
            }
        }
    }

    fun onSubmitErrorShown() = _uiState.update { it.copy(submitError = null) }

    /**
     * Re-checks every field rather than trusting [RequestUiState.canSubmit], so a stale
     * enabled state on the button cannot let an invalid form through.
     */
    private fun validateAll(): ValidatedForm {
        lateinit var validated: RequestUiState
        _uiState.update { current ->
            validated = current.copy(
                nameError = ServiceRequestValidator.validateName(current.customerName),
                phoneError = ServiceRequestValidator.validatePhone(current.phoneNumber),
                vehicleError = ServiceRequestValidator.validateVehicle(current.vehicleNumber),
                serviceError = ServiceRequestValidator.validateService(current.selectedService),
                descriptionError =
                    ServiceRequestValidator.validateDescription(current.problemDescription),
            )
            validated
        }
        return ValidatedForm(validated)
    }
}

private class ValidatedForm(val state: RequestUiState) {
    val hasNoErrors: Boolean
        get() = state.nameError == null &&
            state.phoneError == null &&
            state.vehicleError == null &&
            state.serviceError == null &&
            state.descriptionError == null
}
