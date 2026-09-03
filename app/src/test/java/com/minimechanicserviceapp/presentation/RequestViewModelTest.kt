package com.minimechanicserviceapp.presentation

import androidx.lifecycle.SavedStateHandle
import com.minimechanicserviceapp.core.result.AppResult
import com.minimechanicserviceapp.core.result.DataError
import com.minimechanicserviceapp.domain.usecase.GetMechanicByIdUseCase
import com.minimechanicserviceapp.domain.usecase.SubmitServiceRequestUseCase
import com.minimechanicserviceapp.domain.validation.FieldError
import com.minimechanicserviceapp.presentation.navigation.RequestServiceDestination
import com.minimechanicserviceapp.presentation.request.RequestViewModel
import com.minimechanicserviceapp.util.FakeMechanicRepository
import com.minimechanicserviceapp.util.FakeServiceRequestRepository
import com.minimechanicserviceapp.util.MainDispatcherRule
import com.minimechanicserviceapp.util.mechanic
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RequestViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val garage = mechanic(
        id = "1",
        name = "Sharma Auto Works",
        services = listOf("Brake Repair", "Oil Change", "AC Service"),
    )

    private lateinit var mechanicRepository: FakeMechanicRepository
    private lateinit var requestRepository: FakeServiceRequestRepository

    private fun viewModel(mechanicId: String = "1"): RequestViewModel {
        mechanicRepository = FakeMechanicRepository(initial = listOf(garage))
        requestRepository = FakeServiceRequestRepository()

        return RequestViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(RequestServiceDestination::mechanicId.name to mechanicId),
            ),
            getMechanicById = GetMechanicByIdUseCase(mechanicRepository),
            submitServiceRequest = SubmitServiceRequestUseCase(requestRepository),
        )
    }

    /** Fills every field with values that pass validation. */
    private fun RequestViewModel.fillValidForm() {
        onNameChange("Krish Patel")
        onPhoneChange("9876543210")
        onVehicleChange("KA01AB1234")
        onServiceChange("Brake Repair")
        onDescriptionChange("Brakes squeal loudly when stopping")
    }

    @Test
    fun `services and garage name are pre-filled from the selected mechanic`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("Sharma Auto Works", state.mechanicName)
        assertEquals(listOf("Brake Repair", "Oil Change", "AC Service"), state.availableServices)
        assertEquals("first service is selected by default", "Brake Repair", state.selectedService)
    }

    @Test
    fun `an unknown mechanic id leaves the form usable but empty`() = runTest {
        val vm = viewModel(mechanicId = "does-not-exist")
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("", state.mechanicName)
        assertTrue(state.availableServices.isEmpty())
    }

    @Test
    fun `errors surface on blur, not while typing`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()

        vm.onPhoneChange("123")
        assertNull("typing must not scold the user mid-word", vm.uiState.value.phoneError)

        vm.onPhoneBlur()
        assertEquals(FieldError.PHONE_INVALID, vm.uiState.value.phoneError)
    }

    @Test
    fun `typing clears a previously shown error`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()

        vm.onVehicleChange("bad")
        vm.onVehicleBlur()
        assertEquals(FieldError.VEHICLE_INVALID, vm.uiState.value.vehicleError)

        vm.onVehicleChange("KA01AB1234")
        assertNull(vm.uiState.value.vehicleError)
    }

    @Test
    fun `each field reports its own error on blur`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()

        vm.onNameBlur()
        vm.onPhoneBlur()
        vm.onVehicleBlur()
        vm.onDescriptionBlur()

        val state = vm.uiState.value
        assertEquals(FieldError.NAME_BLANK, state.nameError)
        assertEquals(FieldError.PHONE_BLANK, state.phoneError)
        assertEquals(FieldError.VEHICLE_BLANK, state.vehicleError)
        assertEquals(FieldError.DESCRIPTION_BLANK, state.descriptionError)
    }

    @Test
    fun `the description is capped at its maximum length`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()

        val allowed = "x".repeat(500)
        vm.onDescriptionChange(allowed)
        assertEquals(500, vm.uiState.value.problemDescription.length)

        vm.onDescriptionChange("x".repeat(501))
        assertEquals("the over-long edit is rejected", 500, vm.uiState.value.problemDescription.length)
    }

    @Test
    fun `submit is blocked until every field is valid`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        assertFalse(vm.uiState.value.canSubmit)

        vm.onNameChange("Krish Patel")
        vm.onPhoneChange("9876543210")
        vm.onVehicleChange("KA01AB1234")
        assertFalse("description is still empty", vm.uiState.value.canSubmit)

        vm.onDescriptionChange("Brakes squeal loudly when stopping")
        assertTrue(vm.uiState.value.canSubmit)
    }

    @Test
    fun `a valid submission produces a receipt id`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        vm.fillValidForm()

        vm.onSubmit()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isSubmitting)
        assertEquals("51", state.receiptId)
        assertTrue(state.isSubmitted)
        assertNull(state.submitError)
    }

    @Test
    fun `the submitted request carries the mechanic id from the route`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        vm.fillValidForm()

        vm.onSubmit()
        advanceUntilIdle()

        assertEquals(1, requestRepository.submitted.size)
        assertEquals("1", requestRepository.submitted.first().mechanicId)
    }

    @Test
    fun `the use case normalises phone and vehicle before they reach the api`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        vm.onNameChange("  Krish Patel  ")
        vm.onPhoneChange("+91 98765 43210")
        vm.onVehicleChange("ka-01 ab 1234")
        vm.onServiceChange("Oil Change")
        vm.onDescriptionChange("  Engine makes a knocking sound  ")

        vm.onSubmit()
        advanceUntilIdle()

        val sent = requestRepository.submitted.single()
        assertEquals("Krish Patel", sent.customerName)
        assertEquals("9876543210", sent.phoneNumber)
        assertEquals("KA01AB1234", sent.vehicleNumber)
        assertEquals("Engine makes a knocking sound", sent.problemDescription)
        assertEquals("Oil Change", sent.serviceType)
    }

    @Test
    fun `submitting an invalid form is refused and flags every bad field`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()

        // canSubmit is false here, but onSubmit must defend itself regardless
        vm.onSubmit()
        advanceUntilIdle()

        assertTrue("nothing may reach the api", requestRepository.submitted.isEmpty())
        val state = vm.uiState.value
        assertEquals(FieldError.NAME_BLANK, state.nameError)
        assertEquals(FieldError.PHONE_BLANK, state.phoneError)
        assertEquals(FieldError.VEHICLE_BLANK, state.vehicleError)
        assertEquals(FieldError.DESCRIPTION_BLANK, state.descriptionError)
        assertNull(state.receiptId)
    }

    @Test
    fun `a failed submission reports the error and stays on the form`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        requestRepository.result = AppResult.Failure(DataError.NoInternet)
        vm.fillValidForm()

        vm.onSubmit()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(DataError.NoInternet, state.submitError)
        assertNull(state.receiptId)
        assertFalse("the form must not flip to the confirmation", state.isSubmitted)
        assertFalse(state.isSubmitting)
    }

    @Test
    fun `retiring the submit error leaves the typed input untouched`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        requestRepository.result = AppResult.Failure(DataError.TimeOut)
        vm.fillValidForm()
        vm.onSubmit()
        advanceUntilIdle()

        vm.onSubmitErrorShown()

        val state = vm.uiState.value
        assertNull(state.submitError)
        assertEquals("Krish Patel", state.customerName)
        assertTrue("the user can retry immediately", state.canSubmit)
    }

    @Test
    fun `a double tap on submit only sends one request`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        vm.fillValidForm()

        vm.onSubmit()
        vm.onSubmit()
        advanceUntilIdle()

        assertEquals(1, requestRepository.submitted.size)
    }
}
