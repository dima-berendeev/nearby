package org.berendeev.nearby.placeslist

import androidx.compose.runtime.Composable
import org.berendeev.nearby.ui.LocationPermissionsState

@Composable
fun TestPlacesListScreen(
    uiState: PlacesListUiState = PlacesListUiState.Success(emptyList()),
    query: String = "",
    onQueryChanged: (String) -> Unit = {},
    isOnline: Boolean = true,
    isCoordinatesAvailable: Boolean? = true,
    permissionsState: LocationPermissionsState = LocationPermissionsState.Fine
) {
    PlacesListScreen(
        uiState = uiState,
        query = query,
        onQueryChanged = onQueryChanged,
        isOnline = isOnline,
        isCoordinatesAvailable = isCoordinatesAvailable,
        permissionsState = permissionsState
    )
}
