package org.berendeev.nearby.data.model

data class Place(
    val name: String,
    val location: Location?,
    val categories: List<Category>,
    val photos: List<Photo>,
    val distance:Int?
)
