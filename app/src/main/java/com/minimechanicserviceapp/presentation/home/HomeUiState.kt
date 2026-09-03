package com.minimechanicserviceapp.presentation.home

import com.minimechanicserviceapp.core.result.DataError
import com.minimechanicserviceapp.domain.model.OpenStatus


data class MechanicListItem(
    val id: String,
    val name: String,
    val rating: Double,
    val reviewCount: Int,
    val locality: String,
    val services: List<String>,
    val distanceKm: Double,
    val openStatus: OpenStatus,
)

enum class MechanicSort(val label: String) {
    DISTANCE("Nearest"),
    RATING("Top rated"),
    NAME("Name"),
}




data class HomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val mechanics: List<MechanicListItem> = emptyList(),
    val query: String = "",
    val sort: MechanicSort = MechanicSort.DISTANCE,
    val openNowOnly: Boolean = false,
    val error: DataError? = null,
    val refreshError: DataError? = null,
) {
    val isEmptyResult: Boolean
        get() = !isLoading && error == null && mechanics.isEmpty()

    val hasActiveFilters: Boolean
        get() = query.isNotBlank() || openNowOnly
}