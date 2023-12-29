package org.berendeev.nearby.placeslist

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.withIndex
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(FlowPreview::class)
fun Flow<String>.debounceQueryFlow() = withIndex()
    .debounce { if (it.index == 0) 0.seconds else QUERY_DEBOUNCE_TIME }
    .map { it.value }
    .distinctUntilChanged()

internal val QUERY_DEBOUNCE_TIME = 300.milliseconds
