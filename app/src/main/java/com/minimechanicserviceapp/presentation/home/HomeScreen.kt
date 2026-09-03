package com.minimechanicserviceapp.presentation.home


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minimechanicserviceapp.core.result.DataError


@Composable
fun HomeRoute(
    onMechanicClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        state = state,
        onMechanicClick = onMechanicClick,
        onQueryChange = viewModel::onQueryChange,
        onSortChange = viewModel::onSortChange,
        onOpenNowToggle = viewModel::onOpenNowToggle,
        onClearFilters = viewModel::onClearFilters,
        onRetry = { viewModel.refresh() },
        onRefreshErrorShown = viewModel::onRefreshErrorShown,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    onMechanicClick: (String) -> Unit,
    onQueryChange: (String) -> Unit,
    onSortChange: (MechanicSort) -> Unit,
    onOpenNowToggle: () -> Unit,
    onClearFilters: () -> Unit,
    onRetry: () -> Unit,
    onRefreshErrorShown: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.refreshError) {
        val error = state.refreshError ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(error.toUserMessage())
        onRefreshErrorShown()
    }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Instant Mechanic") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search garage, area or service") },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            FilterRow(
                sort = state.sort,
                openNowOnly = state.openNowOnly,
                onSortChange = onSortChange,
                onOpenNowToggle = onOpenNowToggle,
            )
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> LoadingState()

                    state.error != null -> ErrorState(
                        message = state.error.toUserMessage(),
                        onRetry = onRetry,
                    )

                    state.isEmptyResult -> EmptyState(
                        hasFilters = state.hasActiveFilters,
                        onClearFilters = onClearFilters,
                    )
                    else -> LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(
                            items = state.mechanics,
                            key = { it.id },
                        ) { item ->
                            MechanicCard(
                                item = item,
                                onClick = { onMechanicClick(item.id) },
                            )
                        }
                    }
                }
            }
        }

    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterRow(
    sort: MechanicSort,
    openNowOnly: Boolean,
    onSortChange: (MechanicSort) -> Unit,
    onOpenNowToggle: () -> Unit,
) {
    androidx.compose.foundation.lazy.LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = openNowOnly,
                onClick = onOpenNowToggle,
                label = { Text("Open now") },
            )
        }
        items(MechanicSort.entries.toList()) { option ->
            FilterChip(
                selected = sort == option,
                onClick = { onSortChange(option) },
                label = { Text(option.label) },
            )
        }
    }
}


@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
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
        Button(onClick = onRetry) { Text("Try again") }
    }
}


@Composable
private fun EmptyState(hasFilters: Boolean, onClearFilters: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = if (hasFilters) {
                "No garages match your search."
            } else {
                "No garages available right now."
            },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        if (hasFilters) {
            Button(onClick = onClearFilters) { Text("Clear filters") }
        }
    }
}



internal fun DataError.toUserMessage(): String = when (this) {
    DataError.NoInternet -> "No internet connection. Check your network and try again."
    DataError.TimeOut -> "The request took too long. Please try again."
    is DataError.Server -> "The server returned an error ($code). Please try again."
    DataError.Serialization -> "We could not read the response from the server."
    DataError.Unknown -> "Something went wrong. Please try again."
}