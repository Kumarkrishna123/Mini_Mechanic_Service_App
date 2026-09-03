package com.minimechanicserviceapp.presentation

import app.cash.turbine.test
import com.minimechanicserviceapp.core.result.AppResult
import com.minimechanicserviceapp.core.result.DataError
import com.minimechanicserviceapp.domain.usecase.GetMechanicsUseCase
import com.minimechanicserviceapp.domain.usecase.RefreshMechanicsUseCase
import com.minimechanicserviceapp.presentation.home.HomeViewModel
import com.minimechanicserviceapp.presentation.home.MechanicSort
import com.minimechanicserviceapp.util.FAR_LAT
import com.minimechanicserviceapp.util.FAR_LNG
import com.minimechanicserviceapp.util.FakeMechanicRepository
import com.minimechanicserviceapp.util.MainDispatcherRule
import com.minimechanicserviceapp.util.NEAR_LAT
import com.minimechanicserviceapp.util.NEAR_LNG
import com.minimechanicserviceapp.util.mechanic
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(repository: FakeMechanicRepository) = HomeViewModel(
        getMechanics = GetMechanicsUseCase(repository),
        refreshMechanics = RefreshMechanicsUseCase(repository),
    )

    private val nearby = mechanic(
        id = "1",
        name = "Sharma Auto Works",
        rating = 4.6,
        locality = "Indiranagar",
        services = listOf("Brake Repair", "AC Service"),
        latitude = NEAR_LAT,
        longitude = NEAR_LNG,
    )

    private val faraway = mechanic(
        id = "2",
        name = "Nova Garage",
        rating = 4.9,
        locality = "Yelahanka",
        services = listOf("Tyre Replacement"),
        latitude = FAR_LAT,
        longitude = FAR_LNG,
    )

    /** No working hours at all, so the resolver always reports Closed. */
    private val closed = mechanic(
        id = "3",
        name = "Shuttered Motors",
        rating = 3.2,
        locality = "Hebbal",
        workingHours = emptyList(),
    )

    @Test
    fun `starts in a loading state`() = runTest {
        val vm = viewModel(FakeMechanicRepository())

        vm.uiState.test {
            assertTrue(awaitItem().isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a successful refresh publishes the cached mechanics`() = runTest {
        val repository = FakeMechanicRepository()
        repository.refreshPayload = listOf(nearby, faraway)
        val vm = viewModel(repository)

        vm.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertFalse(state.isLoading)
            assertEquals(2, state.mechanics.size)
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a failure with an empty cache blocks the screen and shows no snackbar`() = runTest {
        val repository = FakeMechanicRepository()
        repository.refreshResult = AppResult.Failure(DataError.NoInternet)
        val vm = viewModel(repository)

        vm.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertEquals(DataError.NoInternet, state.error)
            assertNull("snackbar must not duplicate the error screen", state.refreshError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a failure with cached rows shows a snackbar instead of an error screen`() = runTest {
        val repository = FakeMechanicRepository(initial = listOf(nearby))
        repository.refreshResult = AppResult.Failure(DataError.TimeOut)
        val vm = viewModel(repository)

        vm.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertNull("cached content must stay on screen", state.error)
            assertEquals(DataError.TimeOut, state.refreshError)
            assertEquals(1, state.mechanics.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `retiring the snackbar clears it without dropping the rows`() = runTest {
        val repository = FakeMechanicRepository(initial = listOf(nearby))
        repository.refreshResult = AppResult.Failure(DataError.Unknown)
        val vm = viewModel(repository)

        vm.uiState.test {
            advanceUntilIdle()
            assertEquals(DataError.Unknown, expectMostRecentItem().refreshError)

            vm.onRefreshErrorShown()
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertNull(state.refreshError)
            assertEquals(1, state.mechanics.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search matches name, locality and service`() = runTest {
        val repository = FakeMechanicRepository(initial = listOf(nearby, faraway))
        val vm = viewModel(repository)

        vm.uiState.test {
            advanceUntilIdle()

            vm.onQueryChange("sharma")
            advanceUntilIdle()
            assertEquals(listOf("1"), expectMostRecentItem().mechanics.map { it.id })

            vm.onQueryChange("yelahanka")
            advanceUntilIdle()
            assertEquals(listOf("2"), expectMostRecentItem().mechanics.map { it.id })

            vm.onQueryChange("tyre")
            advanceUntilIdle()
            assertEquals(listOf("2"), expectMostRecentItem().mechanics.map { it.id })

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a query matching nothing reports an empty result with filters active`() = runTest {
        val repository = FakeMechanicRepository(initial = listOf(nearby, faraway))
        val vm = viewModel(repository)

        vm.uiState.test {
            advanceUntilIdle()

            vm.onQueryChange("no such garage")
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertTrue(state.mechanics.isEmpty())
            assertTrue(state.isEmptyResult)
            assertTrue(state.hasActiveFilters)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search ignores case and surrounding whitespace`() = runTest {
        val repository = FakeMechanicRepository(initial = listOf(nearby, faraway))
        val vm = viewModel(repository)

        vm.uiState.test {
            advanceUntilIdle()

            vm.onQueryChange("  SHARMA  ")
            advanceUntilIdle()
            assertEquals(listOf("1"), expectMostRecentItem().mechanics.map { it.id })

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `open-now filter hides closed garages`() = runTest {
        val repository = FakeMechanicRepository(initial = listOf(nearby, closed))
        val vm = viewModel(repository)

        vm.uiState.test {
            advanceUntilIdle()
            assertEquals(2, expectMostRecentItem().mechanics.size)

            vm.onOpenNowToggle()
            advanceUntilIdle()
            val filtered = expectMostRecentItem()
            assertEquals(listOf("1"), filtered.mechanics.map { it.id })
            assertTrue(filtered.openNowOnly)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `sorting switches between distance, rating and name`() = runTest {
        // deliberately seeded out of order so a passing test cannot be luck
        val repository = FakeMechanicRepository(initial = listOf(faraway, nearby))
        val vm = viewModel(repository)

        vm.uiState.test {
            advanceUntilIdle()
            assertEquals(
                "nearest first is the default",
                listOf("1", "2"),
                expectMostRecentItem().mechanics.map { it.id },
            )

            vm.onSortChange(MechanicSort.RATING)
            advanceUntilIdle()
            assertEquals(listOf("2", "1"), expectMostRecentItem().mechanics.map { it.id })

            vm.onSortChange(MechanicSort.NAME)
            advanceUntilIdle()
            assertEquals(
                listOf("Nova Garage", "Sharma Auto Works"),
                expectMostRecentItem().mechanics.map { it.name },
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearing filters resets query and open-now but keeps the chosen sort`() = runTest {
        val repository = FakeMechanicRepository(initial = listOf(nearby, closed))
        val vm = viewModel(repository)

        vm.uiState.test {
            advanceUntilIdle()
            vm.onQueryChange("sharma")
            vm.onOpenNowToggle()
            vm.onSortChange(MechanicSort.RATING)
            advanceUntilIdle()

            vm.onClearFilters()
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertEquals("", state.query)
            assertFalse(state.openNowOnly)
            assertEquals("sort is not a filter", MechanicSort.RATING, state.sort)
            assertFalse(state.hasActiveFilters)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `overlapping refreshes are dropped while one is still running`() = runTest {
        val repository = FakeMechanicRepository()
        val vm = viewModel(repository)

        // init already launched a refresh; these land while that job is active
        vm.refresh()
        vm.refresh()
        advanceUntilIdle()

        assertEquals(1, repository.refreshCount)
    }

    @Test
    fun `a refresh is allowed once the previous one has finished`() = runTest {
        val repository = FakeMechanicRepository()
        val vm = viewModel(repository)
        advanceUntilIdle()

        vm.refresh(isPullToRefresh = true)
        advanceUntilIdle()

        assertEquals(2, repository.refreshCount)
    }
}
