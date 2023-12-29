package org.berendeev.nearby.placeslist

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalCoroutinesApi::class)
class DebounceQueryFlowKtTest {
    private lateinit var querySharedFlow: MutableSharedFlow<String>
    private val debouncedFlow get() = querySharedFlow.debounceQueryFlow()

    @Before
    fun before() {
        querySharedFlow = MutableSharedFlow()
    }

    @Test
    fun fistQueryWithoutDebounceDelay() = runTest {
        debouncedFlow.test {
            querySharedFlow.emit("First")
            assertEquals("First", awaitItem())
            assertEquals(0, currentTime)
            advanceToEnd()
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun secondQueryWithDebounceDelay() = runTest {
        debouncedFlow.test {
            querySharedFlow.emit("First")
            querySharedFlow.emit("Second")
            skipItems(1)
            assertEquals("Second", awaitItem())
            assertEquals(QUERY_DEBOUNCE_TIME.inWholeMilliseconds, currentTime)
            advanceToEnd()
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun newQueryCancelsPreviousWithinDebounceDelay() = runTest {
        debouncedFlow.test {
            querySharedFlow.emit("First")
            skipItems(1)
            querySharedFlow.emit("Second")
            querySharedFlow.emit("Third")

            assertEquals("Third", awaitItem())
            advanceToEnd()
            ensureAllEventsConsumed()
        }
    }

    private fun TestScope.advanceToEnd() {
        advanceTimeBy(1.hours)
    }
}
