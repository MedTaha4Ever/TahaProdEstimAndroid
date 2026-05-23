package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BackendService(
    val id: Int,
    val name: String,
    val description: String? = "",
    @Json(name = "base_price") val basePrice: Double = 0.0,
    @Json(name = "has_hours") val hasHours: Boolean = false,
    @Json(name = "hourly_rate") val hourlyRate: Double = 0.0,
    @Json(name = "has_quantity") val hasQuantity: Boolean = false,
    @Json(name = "quantity_rate") val quantityRate: Double = 0.0,
    @Json(name = "has_distance") val hasDistance: Boolean = false,
    @Json(name = "distance_rate") val distanceRate: Double = 0.0,
    val category: String? = "general",
    @Json(name = "sort_order") val sortOrder: Int = 0
)

@JsonClass(generateAdapter = true)
data class ServiceGetResponse(
    val success: Boolean,
    val data: List<BackendService>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class BackendSettings(
    val currency: String? = "€",
    @Json(name = "tax_rate") val taxRate: Double? = 0.0,
    @Json(name = "contract_terms") val contractTerms: String? = ""
)

@JsonClass(generateAdapter = true)
data class SettingsGetResponse(
    val success: Boolean,
    val data: BackendSettings? = null
)

@JsonClass(generateAdapter = true)
data class SubmissionServiceItem(
    val name: String,
    val cost: String
)

@JsonClass(generateAdapter = true)
data class SubmissionRequestPayload(
    val phone: String,
    @Json(name = "event_date") val eventDate: String,
    @Json(name = "event_place") val eventPlace: String,
    val total: Double,
    @Json(name = "formatted_total") val formattedTotal: String,
    val services: List<SubmissionServiceItem>
)

@JsonClass(generateAdapter = true)
data class SubmissionCreateResponse(
    val success: Boolean,
    val id: Int? = null,
    val error: String? = null
)
