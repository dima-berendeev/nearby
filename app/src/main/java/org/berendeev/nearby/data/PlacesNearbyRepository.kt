package org.berendeev.nearby.data

import android.util.Log
import org.berendeev.nearby.api.PlacesNearbyDatasource
import org.berendeev.nearby.api.model.Result
import org.berendeev.nearby.api.model.toPlace
import org.berendeev.nearby.data.model.Coordinates
import org.berendeev.nearby.data.model.Issue
import org.berendeev.nearby.data.model.Place
import org.berendeev.nearby.data.model.UnknownDataLayerIssue
import org.berendeev.nearby.di.AppDispatchers
import org.berendeev.nearby.di.Dispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ensureActive
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

interface PlacesNearbyRepository {

    val lastPlaces: List<Place>?

    fun cleanLast()

    suspend fun fetch(request: Request): Response

    data class Request(
        val coordinates: Coordinates? = null,
        val query: String = "",
    )

    sealed interface Response
    data class Success(val places: List<Place>) : Response
    data class Failure(val issue: Issue) : Response
}

class PlacesNearbyRepositoryImpl @Inject constructor(
    private val placesNearbyDatasource: PlacesNearbyDatasource,
    @Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : PlacesNearbyRepository {

    private val atomicLastPlaces = AtomicReference<List<Place>>(null)
    override val lastPlaces: List<Place>?
        get() = atomicLastPlaces.get()

    override fun cleanLast() {
        atomicLastPlaces.set(null)
    }

    override suspend fun fetch(request: PlacesNearbyRepository.Request): PlacesNearbyRepository.Response {
        log("Fetching($request)")
        return try {
            with(ioDispatcher) {
                val responseWrapper = placesNearbyDatasource.fetch(
                    request.coordinates,
                    request.query
                )
                log("Success")
                val places = responseWrapper.results.map(Result::toPlace)
                atomicLastPlaces.set(places)
                PlacesNearbyRepository.Success(
                    places = places,
                )
            }
        } catch (e: Throwable) {
            coroutineContext.ensureActive()
            log("Failure($e)")
            PlacesNearbyRepository.Failure(
                UnknownDataLayerIssue
            )
        }
    }

    private fun log(message: String) {
        Log.d("PlacesNearbyRepository", message)
    }
}
