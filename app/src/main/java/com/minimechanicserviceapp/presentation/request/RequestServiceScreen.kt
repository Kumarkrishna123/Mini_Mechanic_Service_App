package com.minimechanicserviceapp.presentation.request

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minimechanicserviceapp.domain.validation.ServiceRequestValidator
import com.minimechanicserviceapp.presentation.home.toUserMessage

@Composable
fun RequestServiceRoute(
    onBack: () -> Unit,
    viewModel: RequestViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    RequestServiceScreen(
        state = state,
        onBack = onBack,
        onNameChange = viewModel::onNameChange,
        onPhoneChange = viewModel::onPhoneChange,
        onVehicleChange = viewModel::onVehicleChange,
        onServiceChange = viewModel::onServiceChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onNameBlur = viewModel::onNameBlur,
        onPhoneBlur = viewModel::onPhoneBlur,
        onVehicleBlur = viewModel::onVehicleBlur,
        onDescriptionBlur = viewModel::onDescriptionBlur,
        onSubmit = viewModel::onSubmit,
        onSubmitErrorShown = viewModel::onSubmitErrorShown,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestServiceScreen(
    state: RequestUiState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onVehicleChange: (String) -> Unit,
    onServiceChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onNameBlur: () -> Unit,
    onPhoneBlur: () -> Unit,
    onVehicleBlur: () -> Unit,
    onDescriptionBlur: () -> Unit,
    onSubmit: () -> Unit,
    onSubmitErrorShown: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.submitError) {
        val error = state.submitError ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(error.toUserMessage())
        onSubmitErrorShown()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (state.isSubmitted) "Request sent" else "Request Service")
                },
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
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.isSubmitted) {
                ConfirmationContent(
                    requestId = state.receiptId.orEmpty(),
                    mechanicName = state.mechanicName,
                    serviceType = state.selectedService,
                    onDone = onBack,
                )
            } else {
                FormContent(
                    state = state,
                    onNameChange = onNameChange,
                    onPhoneChange = onPhoneChange,
                    onVehicleChange = onVehicleChange,
                    onServiceChange = onServiceChange,
                    onDescriptionChange = onDescriptionChange,
                    onNameBlur = onNameBlur,
                    onPhoneBlur = onPhoneBlur,
                    onVehicleBlur = onVehicleBlur,
                    onDescriptionBlur = onDescriptionBlur,
                    onSubmit = onSubmit,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormContent(
    state: RequestUiState,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onVehicleChange: (String) -> Unit,
    onServiceChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onNameBlur: () -> Unit,
    onPhoneBlur: () -> Unit,
    onVehicleBlur: () -> Unit,
    onDescriptionBlur: () -> Unit,
    onSubmit: () -> Unit,
) {
    var serviceMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (state.mechanicName.isNotBlank()) {
            Text(
                text = "Requesting service at ${state.mechanicName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        OutlinedTextField(
            value = state.customerName,
            onValueChange = onNameChange,
            label = { Text("Your name") },
            leadingIcon = {
                Icon(imageVector = Icons.Filled.Person, contentDescription = null)
            },
            isError = state.nameError != null,
            supportingText = state.nameError?.let { error -> { Text(error.message) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { if (!it.isFocused) onNameBlur() },
        )

        OutlinedTextField(
            value = state.phoneNumber,
            onValueChange = onPhoneChange,
            label = { Text("Phone number") },
            leadingIcon = {
                Icon(imageVector = Icons.Filled.Phone, contentDescription = null)
            },
            isError = state.phoneError != null,
            supportingText = state.phoneError?.let { error -> { Text(error.message) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { if (!it.isFocused) onPhoneBlur() },
        )

        OutlinedTextField(
            value = state.vehicleNumber,
            onValueChange = onVehicleChange,
            label = { Text("Vehicle number") },
            placeholder = { Text("KA01AB1234") },
            isError = state.vehicleError != null,
            supportingText = state.vehicleError?.let { error -> { Text(error.message) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { if (!it.isFocused) onVehicleBlur() },
        )

        ExposedDropdownMenuBox(
            expanded = serviceMenuExpanded,
            onExpandedChange = { serviceMenuExpanded = it },
        ) {
            OutlinedTextField(
                value = state.selectedService,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select service") },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Build, contentDescription = null)
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = serviceMenuExpanded)
                },
                isError = state.serviceError != null,
                supportingText = state.serviceError?.let { error -> { Text(error.message) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            )

            ExposedDropdownMenu(
                expanded = serviceMenuExpanded,
                onDismissRequest = { serviceMenuExpanded = false },
            ) {
                state.availableServices.forEach { service ->
                    DropdownMenuItem(
                        text = { Text(service) },
                        onClick = {
                            onServiceChange(service)
                            serviceMenuExpanded = false
                        },
                    )
                }
            }
        }

        OutlinedTextField(
            value = state.problemDescription,
            onValueChange = onDescriptionChange,
            label = { Text("Problem description") },
            placeholder = { Text("Describe what is wrong with the vehicle") },
            isError = state.descriptionError != null,
            supportingText = {
                val error = state.descriptionError
                if (error != null) {
                    Text(error.message)
                } else {
                    Text(
                        "${state.problemDescription.length} / " +
                            "${ServiceRequestValidator.DESCRIPTION_MAX_LENGTH}"
                    )
                }
            },
            minLines = 4,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Default,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp)
                .onFocusChanged { if (!it.isFocused) onDescriptionBlur() },
        )

        Button(
            onClick = onSubmit,
            enabled = state.canSubmit,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Text("Submit request")
            }
        }
    }
}

@Composable
private fun ConfirmationContent(
    requestId: String,
    mechanicName: String,
    serviceType: String,
    onDone: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp),
        )

        Text(
            text = "Request submitted",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )

        Text(
            text = "$mechanicName will contact you shortly to confirm your booking.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ConfirmationRow(label = "Reference", value = "#$requestId")
                ConfirmationRow(label = "Service", value = serviceType)
            }
        }

        Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
            Text("Done")
        }
    }
}

@Composable
private fun ConfirmationRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
        )
    }
}
