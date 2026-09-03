package com.minimechanicserviceapp.domain.model

data  class ServiceRequest (
    val mechanicId: String,
    val customerName: String,
    val phoneNumber: String,
    val vehicleNumber: String,
    val serviceType: String,
    val problemDescription: String,
)
data class ServiceRequestReceipt(
    val id : String,
    val mechanicId : String
)