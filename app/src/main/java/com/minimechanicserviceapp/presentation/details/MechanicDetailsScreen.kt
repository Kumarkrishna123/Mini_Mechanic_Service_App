package com.minimechanicserviceapp.presentation.details
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.*
import com.minimechanicserviceapp.domain.model.OpenStatus
import com.minimechanicserviceapp.presentation.*
import com.minimechanicserviceapp.presentation.home.formatRating
import com.minimechanicserviceapp.presentation.*
import com.minimechanicserviceapp.presentation.home.formatDistance
import com.minimechanicserviceapp.presentation.home.toUserMessage
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val timeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("h:mm a", Locale.US)

@Composable
fun MechanicDetailsRoute(
    onBack: () -> Unit,
    onRequestService: (String) -> Unit,
    viewModel: DetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MechanicDetailsScreen(
        state = state,
        onBack = onBack,
        onRequestService = onRequestService,
        onRetry = viewModel::refresh,
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MechanicDetailsScreen(
    state: DetailsUiState,
    onBack: () -> Unit,
    onRequestService: (String) -> Unit,
    onRetry: () -> Unit,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.details?.name ?: "Garage") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ){ padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {

            when {
                state.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }

                state.error != null -> MessageState(
                    message = state.error.toUserMessage(),
                    actionLabel = "Try again",
                    onAction = onRetry,
                )

                state.notFound -> MessageState(
                    message = "This garage is no longer available.",
                    actionLabel = "Go back",
                    onAction = onBack,
                )

                state.details != null -> DetailsContent(
                    details = state.details,
                    onRequestService = { onRequestService(state.details.id) },
                    onCall = {
                        val dialed = dial(context, state.details.phoneNumber)
                        if (!dialed) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "No dialer app available on this device."
                                )
                            }
                        }
                    },
                )
            }
        }
    }

}


@Composable
private fun DetailsContent(
    details: MechanicDetails,
    onRequestService: () -> Unit,
    onCall: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HeaderCard(details = details)

        SectionCard(
            title = "Address",
            icon = Icons.Filled.LocationOn,
        ) {
            Text(
                text = details.address,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "${formatDistance(details.distanceKm)} away",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

        }


        SectionCard(
            title = "Services",
            icon = Icons.Filled.Build,
        ) {
            if (details.services.isEmpty()) {
                Text(
                    text = "No services listed.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    details.services.forEach { service ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Text(
                                text = service,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(
                                    horizontal = 12.dp,
                                    vertical = 6.dp,
                                ),
                            )
                        }
                    }
                }
            }
        }

        SectionCard(
            title = "Working hours",
            icon = Icons.Filled.DateRange,
        ) {
            details.week.forEach { day -> ScheduleRow(day = day) }
        }

        SectionCard(
            title = "Phone",
            icon = Icons.Filled.Call,
        ) {
            Text(
                text = details.phoneNumber.ifBlank { "Not provided" },
                style = MaterialTheme.typography.bodyLarge,
            )
            if (details.phoneNumber.isNotBlank()) {
            OutlinedButton(
                onClick = onCall,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Filled.Call,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Text("Call garage")
             }
           }
        }



        Button(
            onClick = onRequestService,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        ) {
            Text("Request Service")
        }


    }


}



@Composable
private fun HeaderCard(details: MechanicDetails) {
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = details.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = formatRating(details.rating),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "(${details.reviewCount} reviews)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            OpenStatusLine(status = details.openStatus)
        }
    }
}


@Composable
private fun OpenStatusLine(status: OpenStatus) {
    val text = when (status) {
        is OpenStatus.Open ->
            "Open now · closes ${status.closesAt.format(timeFormatter)}"

        is OpenStatus.Closed -> {
            val at = status.opensAt
            val on = status.opensOn
            when {
                at != null && on != null ->
                    "Closed · opens ${on.shortLabel()} at ${at.format(timeFormatter)}"

                else -> "Closed"
            }
        }
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        color = if (status.isOpen) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
    )
}


@Composable
private fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit,
) {
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            HorizontalDivider()
            content()
        }
    }
}


@Composable
private fun ScheduleRow(day: DaySchedule) {
    val highlight = day.isToday

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = if (highlight) "${day.day.longLabel()} (today)" else day.day.longLabel(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (highlight) FontWeight.SemiBold else FontWeight.Normal,
            color = if (highlight) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
        Text(
            text = day.hoursLabel(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (highlight) FontWeight.SemiBold else FontWeight.Normal,
            color = if (highlight) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}


private fun DaySchedule.hoursLabel(): String =
    if (isClosedAllDay) {
        "Closed"
    } else {
        "${opensAt!!.format(timeFormatter)} – ${closesAt!!.format(timeFormatter)}"
    }

private fun java.time.DayOfWeek.longLabel(): String =
    getDisplayName(TextStyle.FULL, Locale.US)

private fun java.time.DayOfWeek.shortLabel(): String =
    getDisplayName(TextStyle.SHORT, Locale.US)
@Composable
private fun MessageState(
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onAction) { Text(actionLabel) }
    }
}

private fun dial(context: android.content.Context, phoneNumber: String): Boolean {
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
    return try {
        context.startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}