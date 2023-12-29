@file:OptIn(ExperimentalCoroutinesApi::class)

package org.berendeev.nearby.placeslist

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.berendeev.nearby.data.NetworkMonitor
import org.berendeev.nearby.data.PlacesNearbyRepository
import org.berendeev.nearby.data.model.Coordinates
import org.berendeev.nearby.data.model.NoInternetConnection
import org.berendeev.nearby.data.model.Place
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class PlacesListViewModelTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    lateinit var placesNearbyRepository: PlacesNearbyRepository

    @MockK
    lateinit var networkMonitor: NetworkMonitor


    private val networkMonitorState: MutableStateFlow<Boolean?> = MutableStateFlow(null)
    private var viewModel: PlacesListViewModel? = null
    private var uiStateFlow: StateFlow<PlacesListUiState>? = null
    private var isOnlineState: StateFlow<Boolean?>? = null

    @Before
    fun initDefaultResponses() {
        every { networkMonitor.isOnline } returns networkMonitorState.filterNotNull()
        whenNextFetchSucceeds()
        whenOnlineUndefined()
        whenRepoHasNoCache()
        coEvery { placesNearbyRepository.fetch(any()) } returns PlacesNearbyRepository.Success(emptyList())
        every { placesNearbyRepository.cleanLast() } returns Unit
    }

    @After
    fun cleanState() {
        networkMonitorState.value = null
        viewModel = null
        uiStateFlow = null
        isOnlineState = null
        Dispatchers.resetMain()
    }

    @Test
    fun initStateIsLoading() = runTest {
        givenNewViewModel()
        whenGoesOnline()
        startUiSubscription()
        thenStateIs<PlacesListUiState.Loading>()

        givenNewViewModel()
        whenOnlineUndefined()
        startUiSubscription()
        thenStateIs<PlacesListUiState.Loading>()

        givenNewViewModel()
        whenGoesOffline()
        startUiSubscription()
        thenStateIs<PlacesListUiState.Loading>()
    }

    @Test
    fun online_coordinatesNotAvailable_fetchFetchWithNoCoordinates() = runTest {
        givenNewViewModel()
        whenCoordinatesUnavailable()
        whenGoesOnline()
        startUiSubscription()
        advanceToEnd()
        coVerify(exactly = 1) {
            placesNearbyRepository.fetch(PlacesNearbyRepository.Request(coordinates = null))
        }
    }

    @Test
    fun online_coordinatesNotAvailable_statusUpdated() = runTest {
        givenNewViewModel()
        whenCoordinatesUnavailable()
        whenGoesOnline()
        whenNextFetchSucceeds()
        startUiSubscription()
        advanceToEnd()
        thenStateIs<PlacesListUiState.Success>()

        givenNewViewModel()
        whenCoordinatesUnavailable()
        whenGoesOnline()
        whenNextFetchFails()
        startUiSubscription()
        advanceToEnd()
        thenStateIs<PlacesListUiState.Failure>()
    }

    @Test
    fun online_coordinatesExpectedSoon_fetchIsNotFired_untilTimeout() = runTest {
        givenNewViewModel()
        whenCoordinatesExpectedSoon()
        whenGoesOnline()
        startUiSubscription()
        advanceTimeBy(EXPECTED_SOON_AWAIT_TIME_OUT / 2)
        coVerify(exactly = 0) {
            placesNearbyRepository.fetch(any())
        }
        advanceToEnd()
        coVerify(exactly = 1) {
            placesNearbyRepository.fetch(any())
        }
    }

    @Test
    fun online_coordinatesAvailable_fetchFetchWithCoordinates() = runTest {
        givenNewViewModel()
        whenGoesOnline()
        val coordinates = Coordinates(0.0, 0.0)
        whenCoordinatesAvailable(coordinates)
        startUiSubscription()
        advanceToEnd()
        coVerify(exactly = 1) {
            placesNearbyRepository.fetch(PlacesNearbyRepository.Request(coordinates = coordinates))
        }
    }

    @Test
    fun online_coordinatesAvailable_statusUpdated() = runTest {
        givenNewViewModel()
        whenGoesOnline()
        whenCoordinatesAvailable()
        whenNextFetchSucceeds()
        startUiSubscription()
        advanceToEnd()
        thenStateIs<PlacesListUiState.Success>()

        givenNewViewModel()
        whenGoesOnline()
        whenCoordinatesAvailable()
        whenNextFetchFails()
        startUiSubscription()
        advanceToEnd()
    }

    @Test
    fun offline_repoHasNoCachedResult_uiStateFailure() = runTest {
        givenNewViewModel()
        whenGoesOffline()
        whenRepoHasNoCache()
        startUiSubscription()
        advanceToEnd()
        val uiState = requireUiStateFlow().value
        assertIs<PlacesListUiState.Failure>(uiState)
        assertIs<NoInternetConnection>(uiState.issue)
    }

    @Test
    fun goesOnlineAfterNoInternetConnectionError_causesLoading() = runTest {
        givenNewViewModel()
        whenGoesOffline()
        whenCoordinatesAvailable()
        whenRepoHasNoCache()
        startUiSubscription()
        advanceToEnd()
        val uiState = requireUiStateFlow().value
        assertIs<PlacesListUiState.Failure>(uiState)
        assertIs<NoInternetConnection>(uiState.issue)

        whenNextFetchSucceeds(duration = 2.milliseconds)
        whenGoesOnline()
        advanceTimeBy(1.milliseconds)
        thenStateIs<PlacesListUiState.Loading>()
    }

    @Test
    fun offline_repoHasCachedItem_uiStateSuccess() = runTest {
        givenNewViewModel()
        whenGoesOffline()
        whenRepoHasCache()
        startUiSubscription()
        advanceToEnd()
        thenStateIs<PlacesListUiState.Success>()
    }

    @Test
    fun firstFetchIgnoresQueryDebounce() = runTest {
        givenNewViewModel()
        whenGoesOnline()
        whenCoordinatesAvailable()
        whenNextFetchSucceeds()
        startUiSubscription()
        advanceLessThenQueryDebounce()
        coVerify(exactly = 1) {
            placesNearbyRepository.fetch(any())
        }
    }

    @Test
    fun onNetworkStateUpdate_isOnlineStateUpdate() = runTest {
        givenNewViewModel()
        whenGoesOnline()
        startUiSubscription()
        advanceToEnd()
        whenGoesOffline()
        advanceToEnd()
        assertEquals(false, isOnlineState!!.value)
        whenGoesOnline()
        advanceToEnd()
        assertEquals(true, isOnlineState!!.value)
    }

    @Test
    fun networkEnabled_fetchNewResultsFromRepo() = runTest {
        givenNewViewModel()
        whenGoesOffline()
        whenCoordinatesAvailable()
        startUiSubscription()
        advanceToEnd()
        val newPlaces = listOf(Place("Name", null, emptyList(), emptyList(), null))
        whenNextFetchSucceeds(places = newPlaces)
        whenGoesOnline()
        advanceToEnd()
        thenStateEquals(PlacesListUiState.Success(newPlaces))
    }

    @Test
    fun onNetworkDisabled_ongoingFetchCancellation() = runTest {
        givenNewViewModel()
        whenGoesOnline()
        whenCoordinatesAvailable()
        val isFetchCanceled = CompletableDeferred<Boolean>()
        whenNextFetchSucceeds(duration = 2.seconds, isCanceledDeferred = isFetchCanceled)
        startUiSubscription()
        advanceTimeBy(1.seconds)
        whenGoesOffline()
        advanceToEnd()
        assertTrue(isFetchCanceled.await())
    }

    @Test
    fun queryChangingTooFast_skipUnnecessaryFetches() = runTest {
        givenNewViewModel()
        whenGoesOnline()
        whenCoordinatesUnavailable()
        startUiSubscription()
        advanceToEnd()
        whenQueryUpdated("first")
        advanceLessThenQueryDebounce()
        whenQueryUpdated("second")
        advanceMoreThenQueryDebounce()
        coVerify(exactly = 0) {
            placesNearbyRepository.fetch(PlacesNearbyRepository.Request(query = "first"))
        }
        coVerify(exactly = 1) {
            placesNearbyRepository.fetch(PlacesNearbyRepository.Request(query = "second"))
        }
    }

    @Test
    fun newQuery_ongoingFetchCancellation() = runTest {
        givenNewViewModel()
        whenGoesOnline()
        whenCoordinatesAvailable()
        startUiSubscription()
        advanceToEnd()
        val isFirstCanceled = CompletableDeferred<Boolean>()
        whenNextFetchSucceeds(duration = 2.seconds, isCanceledDeferred = isFirstCanceled)
        whenQueryUpdated("first")
        advanceTimeBy(1.seconds)
        whenQueryUpdated("second")
        advanceToEnd()
        assertTrue(isFirstCanceled.await())
    }

    @Test
    fun onCoordinatesChange_ignoredForOffline() = runTest {
        givenNewViewModel()
        whenGoesOffline()
        startUiSubscription()
        advanceToEnd()
        whenCoordinatesAvailable()
        coVerify(exactly = 0) {
            placesNearbyRepository.fetch(any())
        }
    }

    @Test
    fun onCoordinatesChange_startPlacesFetch() = runTest {
        givenNewViewModel()
        whenGoesOnline()
        startUiSubscription()
        advanceToEnd()
        val newCoordinates = Coordinates(2.0, 2.0)
        requireViewModel().setCoordinateState(CoordinatesState.Available(newCoordinates))
        advanceToEnd()
        coVerify(exactly = 1) {
            placesNearbyRepository.fetch(PlacesNearbyRepository.Request(coordinates = newCoordinates))
        }
    }

    @Test
    fun onCoordinatesChange_uiStateUpdated() = runTest {
        givenNewViewModel()
        whenGoesOnline()
        startUiSubscription()
        advanceToEnd()
        val newPlaces = listOf(Place("Name", null, emptyList(), emptyList(), null))
        whenNextFetchSucceeds(places = newPlaces)
        whenCoordinatesAvailable()
        advanceToEnd()
        thenStateEquals(PlacesListUiState.Success(newPlaces))
    }


    private fun whenOnlineUndefined() {
        networkMonitorState.value = null
    }

    private fun whenGoesOnline() {
        networkMonitorState.value = true
    }

    private fun whenGoesOffline() {
        networkMonitorState.value = false
    }

    private fun whenQueryUpdated(query: String) {
        requireViewModel().queryState.value = query
    }

    private fun whenCoordinatesAvailable(coordinates: Coordinates = Coordinates(1000.0, 1000.0)) {
        requireViewModel().setCoordinateState(CoordinatesState.Available(coordinates))
    }

    private fun whenCoordinatesUnavailable() {
        requireViewModel().setCoordinateState(CoordinatesState.Unavailable)
    }

    private fun whenCoordinatesExpectedSoon() {
        requireViewModel().setCoordinateState(CoordinatesState.ExpectedSoon)
    }

    private fun whenRepoHasCache() {
        every { placesNearbyRepository.lastPlaces } returns emptyList()
    }

    private fun whenRepoHasNoCache() {
        every { placesNearbyRepository.lastPlaces } returns null
    }

    private fun whenNextFetchSucceeds(places: List<Place> = emptyList(), duration: Duration? = null, isCanceledDeferred: CompletableDeferred<Boolean>? = null) {
        coEvery { placesNearbyRepository.fetch(any()) } coAnswers {
            try {
                duration?.let { delay(duration) }
            } catch (e: CancellationException) {
                isCanceledDeferred?.complete(true)
            }
            isCanceledDeferred?.complete(false)
            PlacesNearbyRepository.Success(places)
        }
    }

    private fun whenNextFetchFails() {
        coEvery { placesNearbyRepository.fetch(any()) } coAnswers {
            PlacesNearbyRepository.Failure(mockk())
        }
    }

    private fun TestScope.advanceMoreThenQueryDebounce() {
        advanceTimeBy(QUERY_DEBOUNCE_TIME * 2)
    }

    private fun TestScope.advanceLessThenQueryDebounce() {
        advanceTimeBy(QUERY_DEBOUNCE_TIME / 2)
    }

    private fun TestScope.advanceToEnd() {
        advanceTimeBy(1.hours)
    }

    private inline fun <reified T : PlacesListUiState> thenStateIs() {
        assertIs<T>(requireUiStateFlow().value)
    }

    private fun thenStateEquals(uiState: PlacesListUiState) {
        assertEquals(uiState, requireUiStateFlow().value)
    }

    private suspend fun TestScope.startUiSubscription() {
        uiStateFlow = requireViewModel().uiState.stateIn(backgroundScope)
    }

    private suspend fun TestScope.givenNewViewModel() {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        viewModel = PlacesListViewModel(placesNearbyRepository, networkMonitor)
        isOnlineState = requireViewModel().isOnlineState.stateIn(backgroundScope)
    }

    private fun requireUiStateFlow(): StateFlow<PlacesListUiState> {
        return uiStateFlow!!
    }

    private fun requireViewModel(): PlacesListViewModel {
        return viewModel!!
    }
}
