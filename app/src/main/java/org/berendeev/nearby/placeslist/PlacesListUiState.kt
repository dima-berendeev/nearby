package org.berendeev.nearby.placeslist

import org.berendeev.nearby.data.model.Issue
import org.berendeev.nearby.data.model.Place

sealed interface PlacesListUiState {
    data class Success(val places: List<Place>) : PlacesListUiState
    data class Failure(val issue: Issue, val retry: () -> Unit) : PlacesListUiState
    object Loading : PlacesListUiState
}
