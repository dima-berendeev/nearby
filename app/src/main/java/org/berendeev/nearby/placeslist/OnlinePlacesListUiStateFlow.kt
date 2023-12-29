package org.berendeev.nearby.placeslist

import org.berendeev.nearby.data.PlacesNearbyRepository
import org.berendeev.nearby.data.model.Coordinates
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

@OptIn(ExperimentalCoroutinesApi::class)
fun onlinePlacesListUiStateFlow(queryFlow: Flow<String>, coordinatesFlow: Flow<Coordinates?>, placesNearbyRepository: PlacesNearbyRepository): Flow<PlacesListUiState> = combine(
    queryFlow,
    coordinatesFlow
) { query, coordinates -> onlinePlacesListUiStateFlow(coordinates, query, placesNearbyRepository) }
    .flatMapLatest { it }

private fun onlinePlacesListUiStateFlow(coordinates: Coordinates?, query: String, placesNearbyRepository: PlacesNearbyRepository) = flow {
    do {
        val lastPlaces = placesNearbyRepository.lastPlaces
        val awaitFetchUiState = if (lastPlaces != null) {
            PlacesListUiState.Success(lastPlaces)
        } else {
            PlacesListUiState.Loading
        }
        emit(awaitFetchUiState)

        var retryDeferred: CompletableDeferred<Unit>? = null
        val request = PlacesNearbyRepository.Request(coordinates, query)

        val result = when (val state = placesNearbyRepository.fetch(request)) {
            is PlacesNearbyRepository.Success -> PlacesListUiState.Success(
                state.places,
            )

            is PlacesNearbyRepository.Failure -> {
                retryDeferred = CompletableDeferred()
                PlacesListUiState.Failure(state.issue) {
                    retryDeferred.complete(Unit)
                }
            }
        }
        emit(result)
        retryDeferred?.run {
            placesNearbyRepository.cleanLast()
            await()
        }
    } while (retryDeferred != null)
}
