package org.berendeev.nearby.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.berendeev.nearby.BuildConfig
import org.berendeev.nearby.api.model.ResponseWrapper
import org.berendeev.nearby.data.model.Coordinates
import org.berendeev.nearby.di.AppDispatchers
import org.berendeev.nearby.di.Dispatcher
import javax.inject.Inject

class PlacesNearbyDatasource @Inject constructor(
    private val httpClient: HttpClient,
    @Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) {
    /**
     * Get venue recommendations.
     *
     * See [the docs](https://developer.foursquare.com/reference/places-nearby)
     */
    suspend fun fetch(coordinates: Coordinates? = null, query: String = ""): ResponseWrapper {
        return withContext(ioDispatcher) {
            httpClient.get("https://api.foursquare.com/v3/places/nearby") {
                headers {
                    append("Authorization", BuildConfig.API_KEY)
                }
                url {
                    parameters.apply {
                        append("fields", "fsq_id,name,location,categories,distance,photos,popularity")
                        append("limit", "50")
                        if (coordinates != null) {
                            append("ll", "${coordinates.latitude},${coordinates.longitude}")
                        }
                        if (query.isNotBlank()) {
                            append("query", query)
                        }
                    }
                }
            }.body()
        }
    }
}
