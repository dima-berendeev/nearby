package org.berendeev.nearby.api.model

import kotlinx.serialization.Serializable

@Serializable
data class ResponseWrapper(
    val results: List<Result> = emptyList(),
)
