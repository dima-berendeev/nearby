package org.berendeev.nearby.api.model

import kotlinx.serialization.Serializable

@Serializable
data class GeoCode(
    val main: Main? = null
)

@Serializable
data class Main(
    val latitude: Double,
    val longitude: Double,
)
