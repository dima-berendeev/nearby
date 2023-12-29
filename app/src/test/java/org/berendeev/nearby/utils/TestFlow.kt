package org.berendeev.nearby.utils

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.currentTime

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun <T> TestScope.testFlow(flow: Flow<T>, block: suspend TestFlowScope<T>.() -> Unit) {
    val mutex = Mutex()
    val values = mutableListOf<Pair<Long, T>>()

    flow.onEach {
        mutex.withLock {
            values.add(currentTime to it)
        }
    }.launchIn(this.backgroundScope)

    val scope = object : TestFlowScope<T> {
        override suspend fun consumeEvents(): List<T> {
            return mutex.withLock {
                values.toList().map { it.second }.also {
                    values.clear()
                }
            }

        }

        override suspend fun consumeEventsWithTime(): List<Pair<Long, T>> {
            return mutex.withLock {
                values.toList().also {
                    values.clear()
                }
            }
        }
    }
    scope.block()
}

interface TestFlowScope<T> {
    suspend fun consumeEvents(): List<T>
    suspend fun consumeEventsWithTime(): List<Pair<Long, T>>
}
