package com.example.ui.screens

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.LocalSubmission
import com.example.data.model.BackendService
import com.example.data.model.SubmissionServiceItem
import com.example.ui.viewmodel.EstimateViewModel
import com.example.ui.viewmodel.SelectedItemState
import com.example.ui.viewmodel.ServicesUiState
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.Calendar
import java.util.Locale

// Natural Tones Design Theme Colors
private val CarbonDarkBG = Color(0xFFFBFDF8)
private val CarbonCardBG = Color(0xFFFFFFFF)
private val StudioGold = Color(0xFF386B40)
private val CarbonSleekDivider = Color(0xFFDCE5DB)
private val SlickSuccess = Color(0xFF386B40)

private val NaturalMossGreen = Color(0xFFD7E8D3)
private val NaturalContainerGreen = Color(0xFFE8F5E9)
private val NaturalTextDark = Color(0xFF191C19)
private val NaturalTextGray = Color(0xFF717971)
private val NaturalAccentLabel = Color(0xFF111F10)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: EstimateViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }

    val servicesState by viewModel.servicesUiState.collectAsState()
    val selectedItems by viewModel.selectedItems.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val historicSubmissions by viewModel.historicSubmissions.collectAsState()
    val apiBaseUrl by viewModel.apiBaseUrl.collectAsState()

    val grandTotal = viewModel.getGrandTotal()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Studio Icon",
                            tint = StudioGold,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "TahaProd",
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif,
                            color = NaturalTextDark,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "ESTIMATE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioGold,
                            modifier = Modifier
                                .background(NaturalMossGreen, shape = RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CarbonDarkBG,
                    titleContentColor = NaturalTextDark
                ),
                actions = {
                    IconButton(
                        onClick = { viewModel.loadServicesAndSettings() },
                        modifier = Modifier.testTag("refresh_action_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Rafraîchir les services",
                            tint = StudioGold
                        )
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .background(Color(0xFFF0F5EF))
                    .navigationBarsPadding()
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFFF0F5EF),
                    contentColor = StudioGold,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = StudioGold
                        )
                    },
                    divider = { HorizontalDivider(color = CarbonSleekDivider) }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Explorer", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        },
                        selectedContentColor = StudioGold,
                        unselectedContentColor = NaturalTextGray
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BadgedBox(badge = {
                                    if (selectedItems.isNotEmpty()) {
                                        Badge(containerColor = StudioGold) {
                                            Text(
                                                selectedItems.size.toString(),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Mon Devis", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        },
                        selectedContentColor = StudioGold,
                        unselectedContentColor = NaturalTextGray
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BadgedBox(badge = {
                                    if (historicSubmissions.isNotEmpty()) {
                                        Badge(containerColor = NaturalMossGreen) {
                                            Text(
                                                historicSubmissions.size.toString(),
                                                color = NaturalAccentLabel,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.List,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Historiques", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        },
                        selectedContentColor = StudioGold,
                        unselectedContentColor = NaturalTextGray
                    )
                }
            }
        },
        containerColor = CarbonDarkBG,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> ExploreServicesScreen(
                    servicesState = servicesState,
                    selectedItems = selectedItems,
                    onToggleService = { viewModel.toggleService(it) },
                    onUpdateHours = { id, hrs -> viewModel.updateHours(id, hrs) },
                    onUpdateQty = { id, qty -> viewModel.updateQuantity(id, qty) },
                    onUpdateDistance = { id, dist -> viewModel.updateDistance(id, dist) },
                    formatPrice = { viewModel.formatPrice(it) },
                    grandTotal = grandTotal,
                    onRefresh = { viewModel.loadServicesAndSettings() },
                    onGoToQuoteCart = { selectedTab = 1 }
                )
                1 -> QuoteCartScreen(
                    selectedItems = selectedItems,
                    formatPrice = { viewModel.formatPrice(it) },
                    grandTotal = grandTotal,
                    isSubmitting = isSubmitting,
                    onSubmitQuote = { phone, date, place, successCb, errorCb ->
                        viewModel.submitQuoteRequest(phone, date, place, successCb, errorCb)
                    },
                    onRemoveService = { viewModel.toggleService(it) }
                )
                2 -> SubmissionsHistoryScreen(
                    historicSubmissions = historicSubmissions,
                    apiBaseUrl = apiBaseUrl,
                    onUpdateApiUrl = { viewModel.updateApiBaseUrl(it) },
                    onDeleteHistory = { viewModel.deleteHistory(it) }
                )
            }
        }
    }
}

// --- Screen 1: Services Explorer ---
@Composable
fun ExploreServicesScreen(
    servicesState: ServicesUiState,
    selectedItems: Map<Int, SelectedItemState>,
    onToggleService: (BackendService) -> Unit,
    onUpdateHours: (Int, Double) -> Unit,
    onUpdateQty: (Int, Int) -> Unit,
    onUpdateDistance: (Int, Double) -> Unit,
    formatPrice: (Double) -> String,
    grandTotal: Double,
    onRefresh: () -> Unit,
    onGoToQuoteCart: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (servicesState) {
            is ServicesUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = StudioGold)
                }
            }
            is ServicesUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = NaturalTextGray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Échec de connexion",
                        color = NaturalTextDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = servicesState.message,
                        color = NaturalTextGray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onRefresh,
                        colors = ButtonDefaults.buttonColors(containerColor = StudioGold, contentColor = Color.White)
                    ) {
                        Text("Recommencer", fontWeight = FontWeight.Bold)
                    }
                }
            }
            is ServicesUiState.Success -> {
                val services = servicesState.services
                if (services.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Aucun service disponible pour le moment.", color = NaturalTextDark)
                    }
                } else {
                    val grouped = services.groupBy { it.category ?: "Général" }

                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .background(NaturalMossGreen, shape = RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Studio d'Estimation",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = NaturalAccentLabel
                                    )
                                    Text(
                                        text = "${selectedItems.size} service(s) sélectionné(s)",
                                        fontSize = 12.sp,
                                        color = NaturalTextGray
                                    )
                                }

                                if (selectedItems.isNotEmpty()) {
                                    Button(
                                        onClick = onGoToQuoteCart,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = StudioGold,
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier.testTag("go_to_cart_top_button"),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Calculer Devis", fontWeight = FontWeight.Black, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        LazyColumn(
                             modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                             contentPadding = PaddingValues(16.dp),
                             verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            grouped.forEach { (category, serviceList) ->
                                item {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    ) {
                                        Text(
                                            text = category.uppercase(),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp,
                                            color = StudioGold,
                                            letterSpacing = 1.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        HorizontalDivider(
                                            modifier = Modifier.weight(1f),
                                            color = CarbonSleekDivider
                                        )
                                    }
                                }

                                items(serviceList, key = { it.id }) { service ->
                                    val isChosen = selectedItems.containsKey(service.id)
                                    val itemState = selectedItems[service.id]

                                    ServiceListItemCard(
                                        service = service,
                                        isSelected = isChosen,
                                        itemState = itemState,
                                        onToggle = { onToggleService(service) },
                                        onUpdateHours = { onUpdateHours(service.id, it) },
                                        onUpdateQty = { onUpdateQty(service.id, it) },
                                        onUpdateDistance = { onUpdateDistance(service.id, it) },
                                        formatPrice = formatPrice
                                    )
                                }
                            }
                        }

                        if (selectedItems.isNotEmpty()) {
                            Surface(
                                color = CarbonCardBG,
                                border = BorderStroke(1.dp, CarbonSleekDivider),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Coût Estimé Instantané", fontSize = 11.sp, color = NaturalTextGray)
                                        Text(
                                            formatPrice(grandTotal),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            color = StudioGold
                                        )
                                    }

                                    Button(
                                        onClick = onGoToQuoteCart,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = StudioGold,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("go_to_estimate_footer_button")
                                    ) {
                                        Text("Suivant", fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceListItemCard(
    service: BackendService,
    isSelected: Boolean,
    itemState: SelectedItemState?,
    onToggle: () -> Unit,
    onUpdateHours: (Double) -> Unit,
    onUpdateQty: (Int) -> Unit,
    onUpdateDistance: (Double) -> Unit,
    formatPrice: (Double) -> String
) {
    val borderColorAnimated by animateColorAsState(
        targetValue = if (isSelected) StudioGold else CarbonSleekDivider,
        label = "Border Color"
    )

    Surface(
        color = CarbonCardBG,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColorAnimated),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .testTag("service_card_${service.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = service.name,
                        color = NaturalTextDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    if (!service.description.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = service.description,
                            color = NaturalTextGray,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatPrice(service.basePrice),
                        color = StudioGold,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "de base",
                        color = NaturalTextGray,
                        fontSize = 9.sp
                    )
                }
            }

            if (service.hasHours || service.hasQuantity || service.hasDistance) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (service.hasHours) {
                        Text(
                            text = "+ ${formatPrice(service.hourlyRate)}/h",
                            fontSize = 10.sp,
                            color = StudioGold,
                            modifier = Modifier
                                .background(NaturalContainerGreen, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (service.hasQuantity) {
                        Text(
                            text = "+ ${formatPrice(service.quantityRate)}/uté",
                            fontSize = 10.sp,
                            color = StudioGold,
                            modifier = Modifier
                                .background(NaturalContainerGreen, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (service.hasDistance) {
                        Text(
                            text = "+ ${formatPrice(service.distanceRate)}/km",
                            fontSize = 10.sp,
                            color = StudioGold,
                            modifier = Modifier
                                .background(NaturalContainerGreen, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isSelected && itemState != null,
                enter = fadeIn() + androidx.compose.animation.expandVertically(spring()),
                exit = fadeOut() + shrinkVertically()
            ) {
                if (itemState != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .background(Color(0xFFF2F5F2), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "CONFIGURER LE SERVICE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalTextGray,
                            letterSpacing = 0.5.sp
                        )

                        if (service.hasHours) {
                            ParameterStepperRow(
                                label = "Heures supplémentaires",
                                unit = "h",
                                value = itemState.hours,
                                step = 0.5,
                                format = { String.format("%.1f", it) },
                                onValueUpdate = onUpdateHours
                            )
                        }

                        if (service.hasQuantity) {
                            ParameterStepperRow(
                                label = "Quantité d'unités",
                                unit = "uté",
                                value = itemState.quantity.toDouble(),
                                step = 1.0,
                                format = { it.toInt().toString() },
                                onValueUpdate = { onUpdateQty(it.toInt()) }
                            )
                        }

                        if (service.hasDistance) {
                            ParameterStepperRow(
                                label = "Déplacement / Distance",
                                unit = "km",
                                value = itemState.distance,
                                step = 5.0,
                                format = { it.toInt().toString() },
                                onValueUpdate = onUpdateDistance
                            )
                        }

                        HorizontalDivider(color = CarbonSleekDivider)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sous-total service",
                                fontSize = 12.sp,
                                color = NaturalTextGray
                            )
                            Text(
                                text = formatPrice(itemState.totalCost),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudioGold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = if (isSelected) NaturalMossGreen else Color(0xFFF2F5F2),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, if (isSelected) StudioGold.copy(alpha = 0.5f) else Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = StudioGold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SÉLECTIONNÉ", color = StudioGold, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = NaturalTextGray
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SÉLECTIONNER", color = NaturalTextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ParameterStepperRow(
    label: String,
    unit: String,
    value: Double,
    step: Double,
    format: (Double) -> String,
    onValueUpdate: (Double) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 12.sp, color = NaturalTextGray, fontWeight = FontWeight.Medium)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(NaturalMossGreen)
                    .clickable { onValueUpdate(maxOf(0.0, value - step)) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "-",
                    color = StudioGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Text(
                text = "${format(value)} $unit",
                color = NaturalTextDark,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.widthIn(min = 54.dp),
                textAlign = TextAlign.Center
            )

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(NaturalMossGreen)
                    .clickable { onValueUpdate(value + step) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    color = StudioGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


// --- Screen 2: Devis / Estimator Cart & Submit ---
@Composable
fun QuoteCartScreen(
    selectedItems: Map<Int, SelectedItemState>,
    formatPrice: (Double) -> String,
    grandTotal: Double,
    isSubmitting: Boolean,
    onSubmitQuote: (phone: String, date: String, place: String, (pdfUrl: String?) -> Unit, (String) -> Unit) -> Unit,
    onRemoveService: (BackendService) -> Unit
) {
    val context = LocalContext.current

    var phone by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("") }
    var eventPlace by remember { mutableStateOf("") }

    var successQuoteUrl by remember { mutableStateOf<String?>(null) }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth)
            eventDate = formattedDate
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonCardBG),
                border = BorderStroke(1.dp, CarbonSleekDivider),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "RÉSUMÉ DES SERVICES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NaturalTextGray,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (selectedItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = NaturalTextGray,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Aucun service n'a été sélectionné.",
                                    color = NaturalTextGray,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        selectedItems.values.forEach { selected ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = StudioGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = selected.service.name,
                                            color = NaturalTextDark,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )

                                        val breakdown = mutableListOf<String>()
                                        if (selected.service.hasHours) breakdown.add("${selected.hours}h")
                                        if (selected.service.hasQuantity) breakdown.add("${selected.quantity}qté")
                                        if (selected.service.hasDistance) breakdown.add("${selected.distance}km")

                                        if (breakdown.isNotEmpty()) {
                                            Text(
                                                text = breakdown.joinToString(" • "),
                                                color = NaturalTextGray,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = formatPrice(selected.totalCost),
                                        color = NaturalTextDark,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    IconButton(
                                        onClick = { onRemoveService(selected.service) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Retirer",
                                            tint = Color.Red,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = CarbonSleekDivider)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Montant Estimatif Total",
                                fontWeight = FontWeight.Bold,
                                color = NaturalTextDark,
                                fontSize = 15.sp
                            )
                            Text(
                                formatPrice(grandTotal),
                                color = StudioGold,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }

                        Text(
                            "* Ceci est une estimation instantanée générale et non contractuelle.",
                            color = NaturalTextGray,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        if (selectedItems.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CarbonCardBG),
                    border = BorderStroke(1.dp, CarbonSleekDivider),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "DEMANDER UN DEVIS OFFICIEL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioGold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Numéro de Téléphone *") },
                            placeholder = { Text("+216 12 345 678") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = StudioGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = StudioGold,
                                focusedLabelColor = StudioGold,
                                unfocusedBorderColor = CarbonSleekDivider,
                                unfocusedLabelColor = NaturalTextGray,
                                focusedTextColor = NaturalTextDark,
                                unfocusedTextColor = NaturalTextDark
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("phone_input_field")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = eventDate,
                            onValueChange = { eventDate = it },
                            label = { Text("Date de l'événement *") },
                            placeholder = { Text("AAAA-MM-JJ") },
                            leadingIcon = {
                                IconButton(onClick = { datePickerDialog.show() }) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = StudioGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = StudioGold,
                                focusedLabelColor = StudioGold,
                                unfocusedBorderColor = CarbonSleekDivider,
                                unfocusedLabelColor = NaturalTextGray,
                                focusedTextColor = NaturalTextDark,
                                unfocusedTextColor = NaturalTextDark
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { datePickerDialog.show() }
                                .testTag("date_input_field"),
                            readOnly = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = eventPlace,
                            onValueChange = { eventPlace = it },
                            label = { Text("Lieu de l'événement *") },
                            placeholder = { Text("Salle des fêtes, Tunis") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = StudioGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = StudioGold,
                                focusedLabelColor = StudioGold,
                                unfocusedBorderColor = CarbonSleekDivider,
                                unfocusedLabelColor = NaturalTextGray,
                                focusedTextColor = NaturalTextDark,
                                unfocusedTextColor = NaturalTextDark
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("place_input_field")
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                onSubmitQuote(
                                    phone, eventDate, eventPlace,
                                    { pdfUrl ->
                                        phone = ""
                                        eventDate = ""
                                        eventPlace = ""
                                        successQuoteUrl = pdfUrl
                                    },
                                    { error ->
                                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            enabled = !isSubmitting,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StudioGold,
                                contentColor = Color.White,
                                disabledContainerColor = StudioGold.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("submit_estimate_button")
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.White
                                    )
                                    Text("ENVOYER LA DEMANDE DE DEVIS", fontWeight = FontWeight.Black, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (successQuoteUrl != null) {
        AlertDialog(
            onDismissRequest = { successQuoteUrl = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SlickSuccess,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Devis Envoyé !", color = NaturalTextDark, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        "Votre demande de devis officielle a été soumise avec succès au studio.",
                        color = NaturalTextGray,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Voulez-vous télécharger ou ouvrir une copie de votre devis officiel en format PDF ?",
                        color = NaturalTextGray,
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(successQuoteUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Impossible d'ouvrir l'URL PDF.", Toast.LENGTH_SHORT).show()
                        }
                        successQuoteUrl = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioGold, contentColor = Color.White)
                ) {
                    Text("Ouvrir le PDF", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { successQuoteUrl = null }) {
                    Text("Plus tard", color = NaturalTextDark)
                }
            },
            containerColor = CarbonCardBG
        )
    }
}


// --- Screen 3: Mes Demandes & Server Configuration ---
@Composable
fun SubmissionsHistoryScreen(
    historicSubmissions: List<LocalSubmission>,
    apiBaseUrl: String,
    onUpdateApiUrl: (String) -> Unit,
    onDeleteHistory: (LocalSubmission) -> Unit
) {
    val context = LocalContext.current
    var urlInput by remember { mutableStateOf(apiBaseUrl) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonCardBG),
                border = BorderStroke(1.dp, CarbonSleekDivider),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CONNEXION AU SERVEUR SHARED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioGold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Vous pouvez connecter l'application directement à votre instance d'hébergement web (par ex. sur OVH).",
                        color = NaturalTextGray,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = urlInput,
                            onValueChange = { urlInput = it },
                            label = { Text("Base URL API PHP") },
                            placeholder = { Text("https://votresite.com") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = StudioGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = StudioGold,
                                focusedLabelColor = StudioGold,
                                unfocusedBorderColor = CarbonSleekDivider,
                                unfocusedLabelColor = NaturalTextGray,
                                focusedTextColor = NaturalTextDark,
                                unfocusedTextColor = NaturalTextDark
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("server_url_input_field")
                        )

                        Button(
                            onClick = {
                                onUpdateApiUrl(urlInput)
                                Toast.makeText(context, "URL de connexion mise à jour !", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioGold, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(54.dp)
                                .testTag("update_url_button"),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("Sauver", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Serveur actuel : $apiBaseUrl",
                        color = NaturalTextGray,
                        fontSize = 11.sp
                    )
                }
            }
        }

        item {
            Text(
                text = "MES DEMANDES DE DEVIS (${historicSubmissions.size})",
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                color = NaturalTextDark,
                letterSpacing = 1.sp
            )
        }

        if (historicSubmissions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = null,
                            tint = NaturalTextGray,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Aucune demande de devis soumise.",
                            color = NaturalTextGray,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(historicSubmissions, key = { it.id }) { sub ->
                HistoryItemCard(
                    submission = sub,
                    onOpenPdf = {
                        if (!it.isNullOrEmpty()) {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Impossible d'ouvrir l'URL PDF.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onDelete = { onDeleteHistory(sub) }
                )
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    submission: LocalSubmission,
    onOpenPdf: (String?) -> Unit,
    onDelete: () -> Unit
) {
    val services = remember(submission.servicesDataJson) {
        try {
            val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
            val type = Types.newParameterizedType(List::class.java, SubmissionServiceItem::class.java)
            val adapter = moshi.adapter<List<SubmissionServiceItem>>(type)
            adapter.fromJson(submission.servicesDataJson) ?: emptyList<SubmissionServiceItem>()
        } catch (e: Exception) {
            emptyList<SubmissionServiceItem>()
        }
    }

    Surface(
        color = CarbonCardBG,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CarbonSleekDivider),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_item_${submission.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SlickSuccess)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Soumis avec succès",
                            color = SlickSuccess,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Événement le : ${submission.eventDate}",
                        color = NaturalTextDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Text(
                    text = submission.formattedTotal,
                    color = StudioGold,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = CarbonSleekDivider)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = NaturalTextGray,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Tél: ${submission.phone}", color = NaturalTextDark, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = NaturalTextGray,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Lieu: ${submission.eventPlace}", color = NaturalTextDark, fontSize = 12.sp)
            }

            if (services.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Services sollicités :",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NaturalTextGray
                )
                Spacer(modifier = Modifier.height(4.dp))

                for (item in services) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Text(text = "• ${item.name}", color = NaturalTextDark, fontSize = 12.sp)
                        Text(text = item.cost, color = NaturalTextGray, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = CarbonSleekDivider)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer l'historique",
                        tint = Color.Red,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                if (!submission.pdfUrl.isNullOrEmpty()) {
                    Button(
                        onClick = { onOpenPdf(submission.pdfUrl) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NaturalMossGreen,
                            contentColor = StudioGold
                        ),
                        border = BorderStroke(1.dp, StudioGold.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = StudioGold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("DEVIS PDF", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
