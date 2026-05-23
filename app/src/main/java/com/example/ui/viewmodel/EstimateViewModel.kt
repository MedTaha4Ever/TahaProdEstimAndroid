package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.Repository
import com.example.data.local.AppDatabase
import com.example.data.local.LocalSubmission
import com.example.data.model.BackendService
import com.example.data.model.SubmissionRequestPayload
import com.example.data.model.SubmissionServiceItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

sealed interface ServicesUiState {
    object Loading : ServicesUiState
    data class Success(val services: List<BackendService>) : ServicesUiState
    data class Error(val message: String) : ServicesUiState
}

data class SelectedItemState(
    val service: BackendService,
    val hours: Double = if (service.hasHours) 1.0 else 0.0,
    val quantity: Int = if (service.hasQuantity) 1 else 0,
    val distance: Double = 0.0
) {
    val totalCost: Double
        get() {
            var cost = service.basePrice
            if (service.hasHours) {
                cost += hours * service.hourlyRate
            }
            if (service.hasQuantity) {
                cost += quantity * service.quantityRate
            }
            if (service.hasDistance) {
                cost += distance * service.distanceRate
            }
            return cost
        }
}

class EstimateViewModel(application: Application) : AndroidViewModel(application) {

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            application.applicationContext,
            AppDatabase::class.java,
            "taha_prod_estimate.db"
        ).build()
    }

    private val repository: Repository by lazy {
        Repository(application.applicationContext, database)
    }

    // Services dynamic loading state
    private val _servicesUiState = MutableStateFlow<ServicesUiState>(ServicesUiState.Loading)
    val servicesUiState: StateFlow<ServicesUiState> = _servicesUiState.asStateFlow()

    // Configuration / URL editing state
    private val _apiBaseUrl = MutableStateFlow("")
    val apiBaseUrl: StateFlow<String> = _apiBaseUrl.asStateFlow()

    // Currently selected services
    private val _selectedItems = MutableStateFlow<Map<Int, SelectedItemState>>(emptyMap())
    val selectedItems: StateFlow<Map<Int, SelectedItemState>> = _selectedItems.asStateFlow()

    // Global settings states
    private val _currency = MutableStateFlow("€")
    val currency: StateFlow<String> = _currency.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Historic client requests stored locally using Room Flow
    val historicSubmissions: StateFlow<List<LocalSubmission>> = repository.localSubmissions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        _apiBaseUrl.value = repository.apiBaseUrl
        loadServicesAndSettings()
    }

    fun updateApiBaseUrl(newUrl: String) {
        val formatted = if (newUrl.startsWith("http://") || newUrl.startsWith("https://")) {
            newUrl
        } else {
            "https://$newUrl"
        }
        _apiBaseUrl.value = formatted
        repository.apiBaseUrl = formatted
        loadServicesAndSettings()
    }

    fun loadServicesAndSettings() {
        viewModelScope.launch {
            _servicesUiState.value = ServicesUiState.Loading
            _isRefreshing.value = true
            try {
                // Fetch settings first to look up currency formats
                val settingsRes = repository.fetchSettings()
                if (settingsRes.success && settingsRes.data != null) {
                    _currency.value = settingsRes.data.currency ?: "€"
                }

                // Fetch services list
                val servicesRes = repository.fetchServices()
                if (servicesRes.success && servicesRes.data != null) {
                    _servicesUiState.value = ServicesUiState.Success(servicesRes.data)
                } else {
                    _servicesUiState.value = ServicesUiState.Error("Erreur lors du traitement des services.")
                }
            } catch (e: Exception) {
                Log.e("EstimateViewModel", "Error fetching from remote API", e)
                _servicesUiState.value = ServicesUiState.Error(
                    "Impossible de se connecter au serveur. Veuillez vérifier l'adresse de votre serveur de dev ou votre connexion Internet."
                )
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    // Toggle service selection
    fun toggleService(service: BackendService) {
        val current = _selectedItems.value.toMutableMap()
        if (current.containsKey(service.id)) {
            current.remove(service.id)
        } else {
            current[service.id] = SelectedItemState(service = service)
        }
        _selectedItems.value = current
    }

    fun clearSelections() {
        _selectedItems.value = emptyMap()
    }

    fun isSelected(serviceId: Int): Boolean {
        return _selectedItems.value.containsKey(serviceId)
    }

    fun updateHours(serviceId: Int, hours: Double) {
        val current = _selectedItems.value.toMutableMap()
        val item = current[serviceId] ?: return
        current[serviceId] = item.copy(hours = maxOf(0.0, hours))
        _selectedItems.value = current
    }

    fun updateQuantity(serviceId: Int, quantity: Int) {
        val current = _selectedItems.value.toMutableMap()
        val item = current[serviceId] ?: return
        current[serviceId] = item.copy(quantity = maxOf(0, quantity))
        _selectedItems.value = current
    }

    fun updateDistance(serviceId: Int, distance: Double) {
        val current = _selectedItems.value.toMutableMap()
        val item = current[serviceId] ?: return
        current[serviceId] = item.copy(distance = maxOf(0.0, distance))
        _selectedItems.value = current
    }

    fun getGrandTotal(): Double {
        return _selectedItems.value.values.sumOf { it.totalCost }
    }

    fun formatPrice(value: Double): String {
        return try {
            val formatter = NumberFormat.getNumberInstance(Locale.FRANCE).apply {
                minimumFractionDigits = 2
                maximumFractionDigits = 2
            }
            "${formatter.format(value)} ${_currency.value}"
        } catch (e: Exception) {
            String.format(Locale.US, "%.2f %s", value, _currency.value)
        }
    }

    fun submitQuoteRequest(
        phone: String,
        eventDate: String,
        eventPlace: String,
        onSuccess: (pdfUrl: String?) -> Unit,
        onError: (String) -> Unit
    ) {
        if (phone.isBlank()) {
            onError("Le numéro de téléphone est obligatoire.")
            return
        }
        if (!phone.matches(Regex("^[0-9 +]+$"))) {
            onError("Le numéro de téléphone ne doit contenir que des chiffres, des espaces et le signe +.")
            return
        }
        if (phone.length > 30) {
            onError("Le numéro de téléphone ne doit pas dépasser 30 caractères.")
            return
        }
        if (eventDate.isBlank() || !eventDate.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
            onError("La date de l'événement est obligatoire et doit être au format AAAA-MM-JJ.")
            return
        }
        if (eventPlace.isBlank()) {
            onError("Le lieu de l'événement est obligatoire.")
            return
        }
        if (eventPlace.length > 255) {
            onError("Le lieu de l'événement ne doit pas dépasser 255 caractères.")
            return
        }

        val itemsToSubmit = _selectedItems.value.values.toList()
        if (itemsToSubmit.isEmpty()) {
            onError("Veuillez sélectionner au moins un service avant de soumettre.")
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                // Build Payload JSON mapped correctly
                val grandTotal = getGrandTotal()
                val formattedTotalStr = formatPrice(grandTotal)

                val servicesPayload = itemsToSubmit.map { item ->
                    SubmissionServiceItem(
                        name = item.service.name,
                        cost = formatPrice(item.totalCost)
                    )
                }

                val payload = SubmissionRequestPayload(
                    phone = phone,
                    eventDate = eventDate,
                    eventPlace = eventPlace,
                    total = grandTotal,
                    formattedTotal = formattedTotalStr,
                    services = servicesPayload
                )

                val response = repository.submitEstimate(payload)
                if (response.success && response.id != null) {
                    val serverId = response.id
                    val completedPdfUrl = if (repository.apiBaseUrl.endsWith("/")) {
                        "${repository.apiBaseUrl}api/index.php?route=pdf/generate&id=$serverId"
                    } else {
                        "${repository.apiBaseUrl}/api/index.php?route=pdf/generate&id=$serverId"
                    }

                    // Stringify the chosen services so it caches perfectly locally
                    val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
                    val type = Types.newParameterizedType(List::class.java, SubmissionServiceItem::class.java)
                    val adapter = moshi.adapter<List<SubmissionServiceItem>>(type)
                    val stringifiedServices = adapter.toJson(servicesPayload)

                    // Store locally in Room Database for user histories
                    val localSubmission = LocalSubmission(
                        serverId = serverId,
                        phone = phone,
                        eventDate = eventDate,
                        eventPlace = eventPlace,
                        totalCost = grandTotal,
                        formattedTotal = formattedTotalStr,
                        servicesDataJson = stringifiedServices,
                        pdfUrl = completedPdfUrl
                    )
                    repository.saveSubmissionLocally(localSubmission)

                    // Clear currently selected services on successful submission
                    clearSelections()
                    onSuccess(completedPdfUrl)
                } else {
                    onError(response.error ?: "Une erreur s'est produite lors de la soumission.")
                }
            } catch (e: Exception) {
                Log.e("EstimateViewModel", "Failed quote submission", e)
                onError("Échec de la connexion. Veuillez vérifier l'adresse du serveur ou la configuration réseau.")
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun deleteHistory(localSubmission: LocalSubmission) {
        viewModelScope.launch {
            repository.deleteLocalSubmission(localSubmission.id)
        }
    }
}
