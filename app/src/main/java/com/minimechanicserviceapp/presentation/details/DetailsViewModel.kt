package com.minimechanicserviceapp.presentation.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimechanicserviceapp.core.result.AppResult
import com.minimechanicserviceapp.core.result.DataError
import com.minimechanicserviceapp.domain.model.Mechanic
import com.minimechanicserviceapp.domain.model.WorkingHours
import com.minimechanicserviceapp.domain.usecase.GetMechanicByIdUseCase
import com.minimechanicserviceapp.domain.usecase.RefreshMechanicsUseCase
import com.minimechanicserviceapp.domain.utill.DistanceCalculator
import com.minimechanicserviceapp.domain.utill.OpenStatusResolver
import com.minimechanicserviceapp.domain.utill.UserLocation
import com.minimechanicserviceapp.presentation.navigation.MechanicDetailsDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDateTime
import javax.inject.Inject
private data class DetailsLoadState(
    val isLoading: Boolean = true,
    val error: DataError? = null,
)

@HiltViewModel
class DetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getMechanicById: GetMechanicByIdUseCase,
    private val refreshMechanics: RefreshMechanicsUseCase,
) : ViewModel() {

    private val mechanicId: String =
        checkNotNull(savedStateHandle[MechanicDetailsDestination::mechanicId.name])

    private val loadState = MutableStateFlow(DetailsLoadState())

    private var refreshJob: Job? = null


    val uiState: StateFlow<DetailsUiState> = combine(
        getMechanicById(mechanicId),
        loadState,
    ) { mechanic, load ->
        val now = LocalDateTime.now()

        when {
            mechanic != null -> DetailsUiState(
                isLoading = false,
                details = mechanic.toDetails(now),
            )

            load.isLoading -> DetailsUiState(isLoading = true)

            load.error != null -> DetailsUiState(
                isLoading = false,
                error = load.error,
            )

            else -> DetailsUiState(isLoading = false, notFound = true)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DetailsUiState(isLoading = true),
    )

    init {
        refresh()
    }

    fun refresh() {
        if (refreshJob?.isActive == true) return

        refreshJob = viewModelScope.launch {
            loadState.update { it.copy(isLoading = true, error = null) }

            when (val result = refreshMechanics()) {
                is AppResult.Success -> loadState.update {
                    it.copy(isLoading = false)
                }

                is AppResult.Failure -> loadState.update {
                    it.copy(isLoading = false, error = result.error)
                }
            }
        }
    }
}

private fun Mechanic.toDetails(now: LocalDateTime) = MechanicDetails(
    id = id,
    name = name,
    rating = rating,
    reviewCount = reviewCount,
    address = address,
    locality = locality,
    phoneNumber = phoneNumber,
    services = services,
    distanceKm = DistanceCalculator.distanceKm(
        startLat = UserLocation.LATITUDE,
        startLng = UserLocation.LONGITUDE,
        endLat = latitude,
        endLng = longitude,
    ),
    openStatus = OpenStatusResolver.resolve(workingHours, now),
    week = buildWeek(workingHours, now.dayOfWeek),
)


internal fun buildWeek(
    hours: List<WorkingHours>,
    today: DayOfWeek,
): List<DaySchedule> {
    val byDay = hours.associateBy { it.day }

    return DayOfWeek.entries.map { day ->
        val entry = byDay[day]
        DaySchedule(
            day = day,
            opensAt = entry?.opensAt,
            closesAt = entry?.closesAt,
            isToday = day == today,
        )
    }
}

