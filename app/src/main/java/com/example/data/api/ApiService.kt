package com.example.data.api

import com.example.data.model.ServiceGetResponse
import com.example.data.model.SettingsGetResponse
import com.example.data.model.SubmissionRequestPayload
import com.example.data.model.SubmissionCreateResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("api/index.php?route=services/get")
    suspend fun getServices(): ServiceGetResponse

    @GET("api/index.php?route=settings/get")
    suspend fun getSettings(): SettingsGetResponse

    @POST("api/index.php?route=submit")
    suspend fun createSubmission(@Body payload: SubmissionRequestPayload): SubmissionCreateResponse
}
