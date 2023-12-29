package org.berendeev.nearby.placeslist

import org.berendeev.nearby.data.model.Coordinates

sealed interface CoordinatesState {
    object Unavailable : CoordinatesState {
        override fun toString(): String {
            return "CoordinatesState: Unavailable"
        }
    }

    object ExpectedSoon : CoordinatesState {
        override fun toString(): String {
            return "CoordinatesState: ExpectedSoon"
        }
    }

    data class Available(val coordinates: Coordinates) : CoordinatesState
}
