package com.minimechanicserviceapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimechanicserviceapp.core.result.AppResult
import com.minimechanicserviceapp.domain.model.Mechanic
import com.minimechanicserviceapp.domain.usecase.GetMechanicsUseCase
import com.minimechanicserviceapp.domain.usecase.RefreshMechanicsUseCase
import com.minimechanicserviceapp.domain.utill.DistanceCalculator
import com.minimechanicserviceapp.domain.utill.OpenStatusResolver
import com.minimechanicserviceapp.domain.utill.UserLocation

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

private data class HomeControls(
    val query: String = "",
    val sort: MechanicSort = MechanicSort.DISTANCE,
    val openNowOnly: Boolean = false,
)

private data class LoadState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: com.minimechanicserviceapp.core.result.DataError? = null,
    val refreshError: com.minimechanicserviceapp.core.result.DataError? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getMechanics: GetMechanicsUseCase,
    private val refreshMechanics: RefreshMechanicsUseCase,
) : ViewModel() {

    private val controls = MutableStateFlow(HomeControls())
    private val loadState = MutableStateFlow(LoadState())

    private var refreshJob: Job? = null

    val uiState: StateFlow<HomeUiState> = combine(
        getMechanics(),
        controls,
        loadState,
    ) { mechanics, c, load ->
        val now = LocalDateTime.now()
        val items = mechanics.map { it.toListItem(now) }

        HomeUiState(
            isLoading = load.isLoading && items.isEmpty(),
            isRefreshing = load.isRefreshing,
            mechanics = items.applyControls(c),
            query = c.query,
            sort = c.sort,
            openNowOnly = c.openNowOnly,
            error = load.error.takeIf { items.isEmpty() },
            refreshError = load.refreshError.takeIf { items.isNotEmpty() },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(isLoading = true),
    )

    init {
        refresh()
    }

    fun refresh(isPullToRefresh: Boolean = false) {
        if (refreshJob?.isActive == true) return

        refreshJob = viewModelScope.launch {
            loadState.update {
                it.copy(
                    isLoading = !isPullToRefresh,
                    isRefreshing = isPullToRefresh,
                    error = null,
                    refreshError = null,
                )
            }

            when (val result = refreshMechanics()) {
                is AppResult.Success -> loadState.update {
                    it.copy(isLoading = false, isRefreshing = false)
                }

                is AppResult.Failure -> loadState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = result.error,
                        refreshError = result.error,
                    )
                }
            }
        }
    }

        fun onQueryChange(value: String) = controls.update { it.copy(query = value) }

        fun onSortChange(value: MechanicSort) = controls.update { it.copy(sort = value) }

        fun onOpenNowToggle() = controls.update { it.copy(openNowOnly = !it.openNowOnly) }

        fun onClearFilters() = controls.update {
            it.copy(query = "", openNowOnly = false)
        }

        fun onRefreshErrorShown() = loadState.update { it.copy(refreshError = null) }

}
    private fun Mechanic.toListItem(now: LocalDateTime) = MechanicListItem(
        id = id,
        name = name,
        rating = rating,
        reviewCount = reviewCount,
        locality = locality,
        services = services,
        distanceKm = DistanceCalculator.distanceKm(
            startLat = UserLocation.LATITUDE,
            startLng = UserLocation.LONGITUDE,
            endLat = latitude,
            endLng = longitude,
        ),
        openStatus = OpenStatusResolver.resolve(workingHours, now),
    )

    private fun List<MechanicListItem>.applyControls(
        controls: HomeControls,
    ): List<MechanicListItem> {
        val term = controls.query.trim()

        return asSequence()
            .filter { item ->
                term.isBlank() ||
                        item.name.contains(term, ignoreCase = true) ||
                        item.locality.contains(term, ignoreCase = true) ||
                        item.services.any { it.contains(term , ignoreCase = true)}
                        }
            .filter { !controls.openNowOnly || it.openStatus.isOpen }
            .sortedWith(
                when (controls.sort) {
                    MechanicSort.DISTANCE -> compareBy { it.distanceKm }
                    MechanicSort.RATING -> compareByDescending { it.rating }
                    MechanicSort.NAME -> compareBy { it.name.lowercase() }
                }
            )
            .toList()
    }

