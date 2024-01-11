package org.berendeev.nearby.placeslist

import app.cash.turbine.test
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.berendeev.nearby.data.model.NoInternetConnection
import org.berendeev.nearby.data.model.Place
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class OfflinePlacesListUiStateFlowKtTest {
    @Test
    fun whenHasCachedPlaces_thenEmmitCachedPlaces() = runTest {
        val lastPlacesProvider: () -> List<Place>? = { mockk() }
        offlinePlacesListUiStateFlow(lastPlacesProvider)
            .test {
                val item: PlacesListUiState = awaitItem()
                assertIs<PlacesListUiState.Success>(item)
                awaitComplete()
            }
    }

    @Test
    fun whenHasNoCachedPlaces_thenEmmitFailure() = runTest {
        val lastPlacesProvider: () -> List<Place>? = { null }
        offlinePlacesListUiStateFlow(lastPlacesProvider)
            .test {
                val item: PlacesListUiState = awaitItem()
                assertIs<PlacesListUiState.Failure>(item)
                assertEquals(NoInternetConnection, item.issue)
                awaitComplete()
            }
    }
}
