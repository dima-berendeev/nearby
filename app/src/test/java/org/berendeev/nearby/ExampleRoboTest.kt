package org.berendeev.nearby

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.github.takahirom.roborazzi.captureRoboImage
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
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.berendeev.nearby.di.NetworkModule
import org.berendeev.nearby.placeslist.PlaceItem
import org.berendeev.nearby.placeslist.PlacesItems
import org.berendeev.nearby.ui.LoadingBlank
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowConnectivityManager
import org.robolectric.shadows.ShadowNetwork
import org.robolectric.shadows.ShadowNetworkCapabilities
import kotlin.test.fail

@HiltAndroidTest
@UninstallModules(NetworkModule::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h914dp-normal-long-notround-any-420dpi-keyshidden-nonav")
class ExampleRoboTest {
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 2)
    val testWatcher = object : TestWatcher() {
        override fun failed(e: Throwable?, description: Description) {
            composeTestRule.onNodeWithTag("PlacesListScreen", useUnmergedTree = true)
                .apply {
                    printToLog("ui-tree")
                    val nodeInteraction = composeTestRule.onRoot()
                    nodeInteraction.printToLog("ui-tree")
                    try {
                        nodeInteraction.captureRoboImage("reports/test-failures/${description.className}/${description.methodName}.png")
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }
        }
    }

    private val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))

    @BindValue
    val client = HttpClient(MockEngine) {
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
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
                try {
                    if (request.url.encodedPath == "/v3/places/nearby") {
                        val content = readJson("places_5.json")
                        respond(content, HttpStatusCode.OK, responseHeaders)
                    } else {
                        respond("The Url was not found in ktor mock", HttpStatusCode.NotFound, responseHeaders)
                    }
                } catch (e: Throwable) {
                    println("Ktor mock error:")
                    e.printStackTrace()
                    respond("Mock error", HttpStatusCode.InternalServerError, responseHeaders)
                }
            }
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun loadAndShowDefaultPlacesPlaces() = runTest {
        enableNetwork()
        composeTestRule
            .onNode(hasTestTag(LoadingBlank.testTag))
            .assertExists("No progress")
        composeTestRule
            .waitUntilDoesNotExist(hasTestTag(LoadingBlank.testTag), 5000)
        composeTestRule
            .onNode(hasPlacesTestTag())
            .assertExists("No places shown")

        composeTestRule.onAllNodes(hasTestTag(PlaceItem.testTag))
            .assertCountEquals(5)
    }

    private fun enableNetwork() {
        // todo find a solution for the connectivity manager issue
        // https://github.com/robolectric/robolectric/issues/5586
        val connectivityManager = getApplicationContext<Context>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = ShadowNetworkCapabilities.newInstance()
        shadowOf(networkCapabilities).addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        val shadowConnectivityManager: ShadowConnectivityManager = shadowOf(connectivityManager)
        shadowConnectivityManager.setNetworkCapabilities(connectivityManager.activeNetwork, networkCapabilities)
        shadowConnectivityManager.setDefaultNetworkActive(true)
        shadowConnectivityManager.networkCallbacks.forEach {
            val networkType1 = ShadowNetwork.newInstance(1)
            it.onAvailable(networkType1)
        }
    }

    @Test
    fun testAsset() = runTest {
        val str = readJson("places_5.json").readUTF8Line() ?: fail()
        assertTrue(str.isNotEmpty())
    }

    private fun readJson(name: String): ByteReadChannel {
        return this.javaClass.classLoader!!.getResourceAsStream(name).toByteReadChannel()
    }
}

// todo make it fixture
fun hasPlacesTestTag(): SemanticsMatcher {
    return hasTestTag(PlacesItems.testTag)
}
