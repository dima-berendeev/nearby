package org.berendeev.nearby.placeslist

import app.cash.turbine.test
import io.ktor.content.*
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.berendeev.nearby.data.model.Coordinates
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(Parameterized::class)
class CoordinatesFlowKtTest(hasLocationCache: Boolean) {

    private lateinit var coordinatesState: MutableSharedFlow<CoordinatesState>
    private val coordinatesFlow get() = coordinatesState.coordinatesFlow { cachedCoordinates }

    private val cachedCoordinates: Coordinates? = if (hasLocationCache) {
        spyk(name = "Cached coordinates", objToCopy = Coordinates(-1.0, -1.0))
    } else {
        null
    }

    @Before
    fun before() {
        coordinatesState = MutableSharedFlow()
    }

    @Test
    fun whenAvailableStateThenNewCoordinates() = runTest {
        val newCoordinates = mockk<Coordinates>(name = "New coordinates")
        coordinatesFlow.test {
            coordinatesState.emit(CoordinatesState.Available(newCoordinates))
            val coordinates = awaitItem()
            assertEquals(newCoordinates, coordinates)
            assertEquals(0L, currentTime)
            advanceToEnd()
            expectNoEvents()
        }
    }

    @Test
    fun whenUnavailableStateThenCachedCoordinates() = runTest {
        coordinatesFlow.test {
            coordinatesState.emit(CoordinatesState.Unavailable)
            val coordinates = awaitItem()
            assertSame(cachedCoordinates, coordinates)
            assertEquals(0L, currentTime)
            advanceToEnd()
            expectNoEvents()
        }
    }

    @Test
    fun expectingSoonToAvailableWithinAwaitingTime() = runTest {
        coordinatesFlow.test {
            coordinatesState.emit(CoordinatesState.ExpectedSoon)
            advanceTimeBy(EXPECTED_SOON_AWAIT_TIME_OUT.inWholeMilliseconds / 2)
            val newCoordinates = mockk<Coordinates>(name = "New coordinates")
            coordinatesState.emit(CoordinatesState.Available(newCoordinates))
            assertEquals(newCoordinates, awaitItem())
            advanceToEnd()
            expectNoEvents()
        }
    }

    @Test
    fun expectingSoonToUnavailableWithinAwaitingTime() = runTest {
        coordinatesFlow.test {
            coordinatesState.emit(CoordinatesState.ExpectedSoon)
            advanceTimeBy(EXPECTED_SOON_AWAIT_TIME_OUT.inWholeMilliseconds / 2)
            coordinatesState.emit(CoordinatesState.Unavailable)
            assertEquals(cachedCoordinates, awaitItem())
            advanceToEnd()
            expectNoEvents()
        }
    }

    @Test
    fun expectingSoonEmitsCachedCoordinatesOnAwaitTimeElapsed() = runTest {
        coordinatesFlow.test {
            coordinatesState.emit(CoordinatesState.ExpectedSoon)
            val coordinates = awaitItem()
            assertEquals(cachedCoordinates, coordinates)
            assertEquals(EXPECTED_SOON_AWAIT_TIME_OUT.inWholeMilliseconds, currentTime)
            advanceToEnd()
            expectNoEvents()
        }
    }

    companion object {

        /**
         * Each test scenario defines a configuration under tests.
         */
        @JvmStatic
        @Parameterized.Parameters(name = "#{index}: hasLocationCache = {0}")
        fun parameters(): List<Boolean> {
            return listOf(true, false)
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun TestScope.advanceToEnd() {
    advanceTimeBy(1.hours)
}
