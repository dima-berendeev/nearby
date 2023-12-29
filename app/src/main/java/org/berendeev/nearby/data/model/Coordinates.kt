package org.berendeev.nearby.data.model

data class Coordinates(val latitude: Double, val longitude: Double) {
    override fun toString(): String = "Coordinates($latitude, $longitude)"
}
