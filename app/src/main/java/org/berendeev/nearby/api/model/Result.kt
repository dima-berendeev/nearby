package org.berendeev.nearby.api.model

import kotlinx.serialization.Serializable
import org.berendeev.nearby.data.model.Place


@Serializable
data class Result(
    val categories: List<Category> = emptyList(),
    val distance: Int? = null,
    val geocode: GeoCode? = null,
    val location: Location? = null,
    val name: String? = null,
    val timezone: String? = null,
    val photos: List<PhotoApiModel> = emptyList(),
)

fun Result.toPlace(): Place {
    return Place(
        name = name ?: "Unknown",
        location = location?.address?.let { org.berendeev.nearby.data.model.Location(it) },
        categories = emptyList(),
        distance = distance,
        photos = photos.map { it.toPhoto() }
    )
}
