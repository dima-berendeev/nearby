package org.berendeev.bigtests

import android.util.Log
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.serialization.json.Json
import org.berendeev.nearby.MainActivity
import org.berendeev.nearby.di.NetworkModule
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@UninstallModules(NetworkModule::class)
class ExampleInstrumentedTest {
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    private val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))

    @BindValue
    val client = HttpClient(MockEngine) {
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
        engine {
            addHandler { request ->
                if (request.url.encodedPath == "/v3/places/nearby") {
                    respond(readAsset("places.json"), HttpStatusCode.OK, responseHeaders)
                } else {
                    respond("The Url was not found in ktor mock", HttpStatusCode.NotFound, responseHeaders)
                }
            }
        }
    }

    @Test
    fun showActivity() {
        Thread.sleep(2000)
    }

    private fun readAsset(name: String): ByteReadChannel {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return context.assets.open(name).toByteReadChannel()
    }
}
