package org.berendeev.nearby.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

data class BannerData(
    val text: String,
    val testTag: String,
    val buttonLabel: String? = null
)

@Composable
fun Banner(data: BannerData?, modifier: Modifier = Modifier, onButtonClicked: (() -> Unit)? = null) {
    AnimatedContent(
        targetState = data,
        label = "Animated issue banner",
        modifier = modifier
    ) { targetState ->
        if (targetState != null) {
            Column(modifier = Modifier.testTag(targetState.testTag)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 10.dp, end = 8.dp, bottom = 8.dp)
                ) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .weight(1f),
                        text = targetState.text,
                        style = MaterialTheme.typography.body2
                    )
                    if (targetState.buttonLabel != null) {
                        Spacer(modifier = Modifier.width(36.dp))
                        TextButton(
                            modifier = Modifier,
                            onClick = {
                                onButtonClicked?.invoke()
                            }) {
                            Text(text = targetState.buttonLabel, style = MaterialTheme.typography.button)
                        }
                    }
                }
                Divider(Modifier.fillMaxWidth())
            }
        }
    }
}

@Preview()
@Composable
private fun BannerPreview() {
    MaterialTheme {
        Banner(
            data = BannerData("No location permissions", "TURN ON"),
            onButtonClicked = {},
            modifier = Modifier.padding(bottom = 32.dp),
        )
    }
}
