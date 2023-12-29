package org.berendeev.nearby

import android.util.Log
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import io.mockk.InternalPlatformDsl.toStr
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.berendeev.nearby.api.PlacesNearbyDatasource
import org.berendeev.nearby.data.model.Coordinates
import org.junit.Test

class PlacesUnitTest {
    @Test
    fun testResponseCode() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val placesNearbyDatasource = PlacesNearbyDatasource(httpClient, dispatcher)
        val response = placesNearbyDatasource.fetch(Coordinates(52.376510, 4.905890))
        println(response.toStr())
    }


    companion object {
        val httpClient = HttpClient {
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("ktor", message)
                    }

                }
                level = LogLevel.ALL
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
    }
}
