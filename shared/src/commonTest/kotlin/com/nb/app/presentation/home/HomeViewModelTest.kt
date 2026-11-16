package com.nb.app.presentation.home

import com.nb.app.core.network.ApiError
import com.nb.app.core.network.ApiException
import com.nb.app.testing.MainDispatcherTest
import com.nb.app.testing.TestData.celtics
import com.nb.app.testing.TestData.clippers
import com.nb.app.testing.TestData.hawks
import com.nb.app.testing.TestData.heat
import com.nb.app.testing.TestData.lakers
import com.nb.app.testing.TestData.mavericks
import com.nb.app.testing.TestData.warriors
import com.nb.app.testing.TestData.zephyrs
import kotlinx.coroutines.CompletableDeferred
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HomeViewModelTest : MainDispatcherTest() {

    private val unsortedTeams = listOf(heat, lakers, zephyrs, warriors, celtics, clippers, mavericks, hawks)

    @Test
    fun init_showsLoadingUntilTeamsArrive() {
        val repository = FakeTeamRepository(Result.success(unsortedTeams))
        val gate = CompletableDeferred<Unit>().also { repository.gate = it }

        val viewModel = HomeViewModel(repository)
        assertTrue(viewModel.uiState.value.isLoading)

        gate.complete(Unit)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(unsortedTeams.size, viewModel.uiState.value.teams.size)
    }

    @Test
    fun init_loadsTeamsSortedByName() {
        val viewModel = HomeViewModel(FakeTeamRepository(Result.success(unsortedTeams)))

        val state = viewModel.uiState.value
        assertEquals(TeamSortOption.NAME, state.sortOption)
        assertEquals(
            listOf(hawks, celtics, mavericks, warriors, clippers, lakers, heat, zephyrs),
            state.teams,
        )
        assertNull(state.error)
    }

    @Test
    fun sortByCity_ordersByCityThenName() {
        val viewModel = HomeViewModel(FakeTeamRepository(Result.success(unsortedTeams)))

        viewModel.onSortOptionSelected(TeamSortOption.CITY)

        val state = viewModel.uiState.value
        assertEquals(TeamSortOption.CITY, state.sortOption)
        // Clippers and Lakers share "Los Angeles", so the full name breaks the tie.
        assertEquals(
            listOf(zephyrs, hawks, celtics, mavericks, warriors, clippers, lakers, heat),
            state.teams,
        )
    }

    @Test
    fun sortByConference_ordersByConferenceThenName() {
        val viewModel = HomeViewModel(FakeTeamRepository(Result.success(unsortedTeams)))

        viewModel.onSortOptionSelected(TeamSortOption.CONFERENCE)

        assertEquals(
            listOf(hawks, celtics, heat, zephyrs, mavericks, warriors, clippers, lakers),
            viewModel.uiState.value.teams,
        )
    }

    @Test
    fun sortDialog_opensAndClosesWithoutChangingOrder() {
        val viewModel = HomeViewModel(FakeTeamRepository(Result.success(unsortedTeams)))

        viewModel.onSortClick()
        assertTrue(viewModel.uiState.value.isSortDialogVisible)

        viewModel.onSortDialogDismiss()
        assertFalse(viewModel.uiState.value.isSortDialogVisible)
        assertEquals(TeamSortOption.NAME, viewModel.uiState.value.sortOption)
    }

    @Test
    fun selectingSortOption_closesDialog() {
        val viewModel = HomeViewModel(FakeTeamRepository(Result.success(unsortedTeams)))
        viewModel.onSortClick()

        viewModel.onSortOptionSelected(TeamSortOption.CONFERENCE)

        assertFalse(viewModel.uiState.value.isSortDialogVisible)
    }

    @Test
    fun loadFailure_exposesError() {
        val repository = FakeTeamRepository(Result.failure(ApiException(ApiError.UNAUTHORIZED)))

        val viewModel = HomeViewModel(repository)

        val state = viewModel.uiState.value
        assertEquals(ApiError.UNAUTHORIZED, state.error)
        assertFalse(state.isLoading)
        assertTrue(state.teams.isEmpty())
    }

    @Test
    fun retry_forcesRefreshAndClearsError() {
        val repository = FakeTeamRepository(Result.failure(ApiException(ApiError.NETWORK)))
        val viewModel = HomeViewModel(repository)

        repository.result = Result.success(unsortedTeams)
        viewModel.onRetryClick()

        assertEquals(2, repository.callCount)
        assertEquals(true, repository.lastForceRefresh)
        assertNull(viewModel.uiState.value.error)
        assertEquals(unsortedTeams.size, viewModel.uiState.value.teams.size)
    }

    @Test
    fun selectedSortOption_isKeptAfterReload() {
        val repository = FakeTeamRepository(Result.failure(ApiException(ApiError.NETWORK)))
        val viewModel = HomeViewModel(repository)
        viewModel.onSortOptionSelected(TeamSortOption.CITY)

        repository.result = Result.success(listOf(hawks, zephyrs))
        viewModel.onRetryClick()

        assertEquals(listOf(zephyrs, hawks), viewModel.uiState.value.teams)
        assertEquals(TeamSortOption.CITY, viewModel.uiState.value.sortOption)
    }
}
