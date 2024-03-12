package org.berendeev.nearby.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.berendeev.nearby.ui.LoadingBlank.testTag

@Composable
fun LoadingBlank(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        modifier = modifier
            .testTag(testTag)
            .fillMaxSize()
            .wrapContentSize()
    )
}

object LoadingBlank {
    val testTag = "LoadingBlank"
}
