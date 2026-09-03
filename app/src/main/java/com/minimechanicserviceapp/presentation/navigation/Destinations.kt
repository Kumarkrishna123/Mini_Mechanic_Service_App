package com.minimechanicserviceapp.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
data object HomeDestination

@Serializable
data class MechanicDetailsDestination(val mechanicId: String)

@Serializable
data class RequestServiceDestination(val mechanicId: String)