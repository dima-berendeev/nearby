package org.berendeev.nearby.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.berendeev.nearby.R
import org.berendeev.nearby.data.model.Issue
import org.berendeev.nearby.data.model.NoInternetConnection

object ErrorBlank {
    val testTag = "ErrorBlank"
}

@Composable
fun ErrorBlank(
    issue: Issue,
    retry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .testTag(ErrorBlank.testTag)
            .fillMaxSize()
            .wrapContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (issue) {
            NoInternetConnection -> {
                Icon(
                    modifier = Modifier.size(64.dp),
                    painter = painterResource(id = R.drawable.baseline_wifi_off_24),
                    contentDescription = null
                )
                Text(text = "No internet connection", style = MaterialTheme.typography.h5)
            }

            else -> {
                Text(text = "Something went wrong", style = MaterialTheme.typography.h5)

                Button(onClick = { retry() }) {
                    Text(text = "Retry")
                }
            }
        }
    }
}
