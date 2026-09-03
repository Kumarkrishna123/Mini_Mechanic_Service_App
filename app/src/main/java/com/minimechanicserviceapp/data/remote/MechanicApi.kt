package com.minimechanicserviceapp.data.remote

import com.minimechanicserviceapp.data.remote.dto.MechanicDto
import com.minimechanicserviceapp.data.remote.dto.ServiceRequestBody
import com.minimechanicserviceapp.data.remote.dto.ServiceRequestResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface MechanicApi {

    @GET("mechanics")
    suspend fun getMechanics(): List<MechanicDto>

    @POST("serviceRequests")
    suspend fun submitServiceRequest(
        @Body body: ServiceRequestBody,
    ): ServiceRequestResponse
}