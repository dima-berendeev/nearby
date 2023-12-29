package org.berendeev.nearby.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val address: String? = null,
    val country: String? = null,
    val locality: String? = null,
    val neighbourhood: List<String>? = null,
    val postcode: String? = null,
    val region: String? = null,
)
