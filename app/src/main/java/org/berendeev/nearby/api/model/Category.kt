package org.berendeev.nearby.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val icon: Icon,
    val id: Long,
    val name: String,
)
