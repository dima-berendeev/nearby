package org.berendeev.nearby.placeslist

import android.annotation.SuppressLint
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.berendeev.nearby.BuildConfig
import org.berendeev.nearby.R
import org.berendeev.nearby.data.model.Place
import org.berendeev.nearby.ui.Banner
import org.berendeev.nearby.ui.BannerData
import org.berendeev.nearby.ui.ErrorBlank
import org.berendeev.nearby.ui.LoadingBlank
import org.berendeev.nearby.ui.LocationAware
import org.berendeev.nearby.ui.LocationPermissionsState
import org.berendeev.nearby.ui.LocationState
import org.berendeev.nearby.ui.SearchBar

@SuppressLint("MissingPermission")
@Composable
internal fun PlacesListRoute(viewModel: PlacesListViewModel = hiltViewModel()) {
    LocationAware { permissionsState: LocationPermissionsState, locationState: LocationState? ->
        val coordinatesState = remember(key1 = permissionsState, key2 = locationState) {
            when (permissionsState) {
                is LocationPermissionsState.ApproximateOnly, LocationPermissionsState.Fine -> {
                    when (locationState) {
                        LocationState.GettingAvailability, null -> {
                            CoordinatesState.ExpectedSoon
                        }

                        is LocationState.LocationAvailable -> {
                            if (locationState.coordinates != null) {
                                CoordinatesState.Available(locationState.coordinates)
                            } else {
                                CoordinatesState.ExpectedSoon
                            }
                        }

                        is LocationState.LocationUnavailable -> CoordinatesState.Unavailable
                    }
                }

                is LocationPermissionsState.Denied -> CoordinatesState.Unavailable
            }
        }
        LaunchedEffect(key1 = coordinatesState) {
            viewModel.setCoordinateState(coordinatesState)
        }
        val isCoordinatesAvailable = when (coordinatesState) {
            CoordinatesState.ExpectedSoon -> null
            is CoordinatesState.Available -> true
            CoordinatesState.Unavailable -> false
        }
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val query by viewModel.queryState.collectAsStateWithLifecycle()
        val isOnline by viewModel.isOnlineState.collectAsStateWithLifecycle()
        val onQueryChanged = { newQuery: String -> viewModel.queryState.value = newQuery }
        PlacesListScreen(uiState, query, onQueryChanged, isOnline, isCoordinatesAvailable, permissionsState)
    }
}

@Composable
internal fun PlacesListScreen(
    uiState: PlacesListUiState,
    query: String,
    onQueryChanged: (String) -> Unit,
    isOnline: Boolean?,
    isCoordinatesAvailable: Boolean?,
    permissionsState: LocationPermissionsState,
) {
    Box(
        modifier = Modifier
            .testTag("PlacesListScreen")
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column {
            SearchBar(
                isCoordinatesAvailable = isCoordinatesAvailable,
                value = query,
                onValueChange = { onQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 8.dp, end = 8.dp)
            )

            // `isOnline` is available with small delay, just wait it before showing any banner
            if (isOnline != null) {
                IssueBanner(permissionsState, isOnline)
            }

            when (uiState) {
                PlacesListUiState.Loading -> LoadingBlank()
                is PlacesListUiState.Success -> PlacesItems(uiState.places)
                is PlacesListUiState.Failure -> ErrorBlank(uiState.issue, uiState.retry)
            }
        }

        if (BuildConfig.DEBUG) {
            val context = LocalContext.current
            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                onClick = { context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) },
            ) {
                Icon(Icons.Outlined.Settings, "Location Settings")
            }
        }
    }
}

object PlacesListScreen {
    enum class Banner(val testTag: String) {
        Offline("NetworkUnavailableBanner"),
        LocationPermissionDenied("LocationPermissionDeniedBanner"),
        FineLocationDisabled("FineLocationDisabledBanner")
    }
}

@Composable
private fun IssueBanner(permissionsState: LocationPermissionsState, isOnline: Boolean) {
    when {
        !isOnline -> {
            Banner(
                BannerData(
                    text = "Offline mode",
                    testTag = PlacesListScreen.Banner.Offline.testTag
                )
            )
        }

        permissionsState is LocationPermissionsState.Denied -> {
            Banner(
                BannerData(
                    text = "Location permissions are disabled",
                    testTag = PlacesListScreen.Banner.LocationPermissionDenied.testTag,
                    buttonLabel = "TURN ON"
                ),
                onButtonClicked = { permissionsState.requestPermissions() }
            )
        }

        permissionsState is LocationPermissionsState.ApproximateOnly -> {
            Banner(
                data = BannerData(
                    text = "Fine location disabled",
                    testTag = PlacesListScreen.Banner.FineLocationDisabled.testTag,
                    "TURN ON"
                ),
                onButtonClicked = { permissionsState.requestPermissions() }
            )
        }
    }
}

internal object PlacesItems {
    val testTag = "PlacesItems"
}

internal object NoResultBlank {
    val testTag = "NoResultBlank"
}

@Composable
private fun PlacesItems(places: List<Place>, modifier: Modifier = Modifier) {
    if (places.isNotEmpty()) {
        LazyColumn(
            modifier = modifier
                .testTag(PlacesItems.testTag)
                .fillMaxSize()
        ) {
            items(places) { place ->
                PlaceItem(place)
            }
        }
    } else {
        Column(
            modifier = modifier
                .testTag(NoResultBlank.testTag)
                .fillMaxSize()
                .wrapContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier.size(64.dp),
                painter = painterResource(id = R.drawable.baseline_search_24),
                contentDescription = null
            )
            Text(text = "No results", style = MaterialTheme.typography.h5)
        }
    }
}
