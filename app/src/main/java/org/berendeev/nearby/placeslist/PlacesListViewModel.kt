package org.berendeev.nearby.placeslist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.berendeev.nearby.BuildConfig
import org.berendeev.nearby.data.NetworkMonitor
import org.berendeev.nearby.data.PlacesNearbyRepository
import org.berendeev.nearby.data.model.Coordinates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlacesListViewModel @Inject constructor(
    private val placesNearbyRepository: PlacesNearbyRepository,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    val queryState = MutableStateFlow("")

    val isOnlineState: StateFlow<Boolean?> = networkMonitor.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_AWAIT_MS), null)

    private val coordinatesStateFlow = MutableStateFlow<CoordinatesState>(CoordinatesState.ExpectedSoon)

    private val lastAvailableCoordinates: AtomicReference<Coordinates?> = AtomicReference(null)

    fun setCoordinateState(coordinatesState: CoordinatesState) {
        if (BuildConfig.DEBUG) {
            log("New Coordinates($coordinatesState)")
        }
        if (coordinatesState is CoordinatesState.Available) {
            lastAvailableCoordinates.set(coordinatesState.coordinates)
        }
        coordinatesStateFlow.value = coordinatesState
    }

    val uiState: StateFlow<PlacesListUiState> = networkMonitor.isOnline
        .distinctUntilChanged()
        .flatMapLatest { isOnline ->
            log("IsOnline($isOnline)")
            if (isOnline) {
                onlinePlacesListUiStateFlow(
                    queryState.debounceQueryFlow(),
                    coordinatesStateFlow.coordinatesFlow(lastAvailableCoordinates::get),
                    placesNearbyRepository
                )
            } else {
                offlinePlacesListUiStateFlow(placesNearbyRepository::lastPlaces)
            }
        }
        .onEach {
            log("State(${it::class})")
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_AWAIT_MS), PlacesListUiState.Loading)

    private fun log(message: String) {
        Log.d("PlacesListViewModel", message)
    }

    companion object {
        internal const val SUBSCRIPTION_AWAIT_MS = 5_000L
    }
}
