package com.minimechanicserviceapp.data.remote.dto

import kotlinx.serialization.Serializable


@Serializable
data class ServiceRequestBody(
    val mechanicId: String,
    val customerName: String,
    val phoneNumber: String,
    val vehicleNumber: String,
    val serviceType: String,
    val problemDescription: String,
)

@Serializable
data class ServiceRequestResponse(
    val id: String,
    val mechanicId: String? = null,
)