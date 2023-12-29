package org.berendeev.nearby.placeslist

import org.berendeev.nearby.data.model.Coordinates
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
fun Flow<CoordinatesState>.coordinatesFlow(
    cachedCoordinatesProvider: () -> Coordinates?
): Flow<Coordinates?> = mapLatest { coordinatesState ->
    when (coordinatesState) {
        is CoordinatesState.Available -> coordinatesState.coordinates

        CoordinatesState.ExpectedSoon -> {
            delay(EXPECTED_SOON_AWAIT_TIME_OUT)
            cachedCoordinatesProvider()
        }

        CoordinatesState.Unavailable -> cachedCoordinatesProvider()
    }
}.distinctUntilChanged()

internal val EXPECTED_SOON_AWAIT_TIME_OUT = 10.seconds
