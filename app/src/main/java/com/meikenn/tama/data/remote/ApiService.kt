package com.meikenn.tama.data.remote

import com.meikenn.tama.data.model.ApiRequestBody
import com.meikenn.tama.data.model.ApiResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface ApiService {

    // === Authentication ===
    @POST("up/pk/Pky001Resource/login")
    suspend fun login(@Body body: ApiRequestBody): ApiResponse

    @POST("up/pk/Pky002Resource/logout")
    suspend fun logout(@Body body: ApiRequestBody): ApiResponse

    @POST("up/ap/Apa001Resource/firstSetting")
    suspend fun firstSetting(@Body body: ApiRequestBody): ApiResponse

    // === Timetable ===
    @POST("up/ap/Apa004Resource/getJugyoKeijiMenuInfo")
    suspend fun getTimetable(@Body body: ApiRequestBody): ApiResponse

    // === Course Detail ===
    @POST("up/ap/Apa004Resource/getJugyoDetailInfo")
    suspend fun getCourseDetail(@Body body: ApiRequestBody): ApiResponse

    @POST("up/ap/Apa004Resource/setJugyoMemoInfo")
    suspend fun saveMemo(@Body body: ApiRequestBody): ApiResponse
}
