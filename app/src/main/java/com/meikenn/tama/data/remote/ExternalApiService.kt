package com.meikenn.tama.data.remote

import com.google.gson.JsonObject
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ExternalApiService {

    @POST("kadai")
    suspend fun getAssignments(@Body body: JsonObject): JsonObject

    @GET("bus/app_data")
    suspend fun getBusSchedule(): JsonObject

    @GET("tmail")
    suspend fun getTeachers(): JsonObject
}
