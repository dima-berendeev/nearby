package org.berendeev.nearby.placeslist

import org.berendeev.nearby.data.model.NoInternetConnection
import org.berendeev.nearby.data.model.Place
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

fun offlinePlacesListUiStateFlow(lastPlacesProvider: () -> List<Place>?): Flow<PlacesListUiState> {
    val lastPlaces = lastPlacesProvider()
    return if (lastPlaces != null) {
        flowOf(PlacesListUiState.Success(lastPlaces))
    } else {
        flowOf(PlacesListUiState.Failure(NoInternetConnection) {})
    }
}
