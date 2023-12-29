package org.berendeev.nearby.api.model

import kotlinx.serialization.Serializable
import org.berendeev.nearby.data.model.Photo

@Serializable
data class PhotoApiModel(val prefix: String, val suffix: String)

fun PhotoApiModel.toPhoto(): Photo {
    return Photo(prefix = prefix, suffix = suffix)
}
