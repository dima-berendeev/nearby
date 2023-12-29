package org.berendeev.nearby.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Icon(
    val prefix: String,
    val suffix: String
)
