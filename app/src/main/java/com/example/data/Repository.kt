package com.example.data

import android.content.Context
import android.util.Log
import com.example.data.api.ApiService
import com.example.data.local.AppDatabase
import com.example.data.local.LocalSubmission
import com.example.data.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class Repository(private val context: Context, private val database: AppDatabase) {

    private val sharedPrefs = context.getSharedPreferences("TahaProdEstimatePrefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_API_URL = "api_url"
        // Active server URL as default endpoint
        const val DEFAULT_API_URL = "https://ais-dev-v3oa6vc6xfix2uln3v4pum-874074439465.europe-west2.run.app/"
    }

    @Volatile
    private var activeApiService: ApiService? = null

    var apiBaseUrl: String
        get() = sharedPrefs.getString(KEY_API_URL, DEFAULT_API_URL) ?: DEFAULT_API_URL
        set(value) {
            val sanitized = if (value.endsWith("/")) value else "$value/"
            sharedPrefs.edit().putString(KEY_API_URL, sanitized).apply()
            rebuildApiService(sanitized)
        }

    init {
        rebuildApiService(apiBaseUrl)
    }

    @Synchronized
    private fun rebuildApiService(baseUrl: String) {
        try {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            activeApiService = retrofit.create(ApiService::class.java)
            Log.d("Repository", "Moshi Retrofit initialized successfully with URL: $baseUrl")
        } catch (e: Exception) {
            Log.e("Repository", "Failed to build ApiService for URL: $baseUrl", e)
        }
    }

    private fun getApiService(): ApiService {
        return activeApiService ?: synchronized(this) {
            activeApiService ?: ApiServiceInstanceHolder.stubService.also { activeApiService = it }
        }
    }

    // --- Remote API Calls ---

    suspend fun fetchServices(): ServiceGetResponse {
        return getApiService().getServices()
    }

    suspend fun fetchSettings(): SettingsGetResponse {
        return getApiService().getSettings()
    }

    suspend fun submitEstimate(payload: SubmissionRequestPayload): SubmissionCreateResponse {
        return getApiService().createSubmission(payload)
    }

    // --- Local Database Calls ---

    val localSubmissions: Flow<List<LocalSubmission>> = database.localSubmissionDao().getAllSubmissions()

    suspend fun saveSubmissionLocally(submission: LocalSubmission): Long {
        return database.localSubmissionDao().insertSubmission(submission)
    }

    suspend fun deleteLocalSubmission(id: Int) {
        database.localSubmissionDao().deleteById(id)
    }

    // Helper object for simple stub placeholder if initialization fails
    private object ApiServiceInstanceHolder {
        val stubService = object : ApiService {
            override suspend fun getServices(): ServiceGetResponse {
                return ServiceGetResponse(success = false, data = emptyList())
            }
            override suspend fun getSettings(): SettingsGetResponse {
                return SettingsGetResponse(success = false, data = null)
            }
            override suspend fun createSubmission(payload: SubmissionRequestPayload): SubmissionCreateResponse {
                return SubmissionCreateResponse(success = false, error = "API Client unititialized due to incorrect format URL")
            }
        }
    }
}
