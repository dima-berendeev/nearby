package org.berendeev.nearby.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.berendeev.nearby.R

@Composable
fun SearchBar(isCoordinatesAvailable: Boolean?, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val focusManager = LocalFocusManager.current
    Card(modifier = modifier, elevation = 4.dp) {
        TextField(
            value = value,
            onValueChange = { text: String -> onValueChange(text) },
            placeholder = { Text(text = "Search", style = MaterialTheme.typography.body1) },
            textStyle = MaterialTheme.typography.body1,
            singleLine = true,
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = Color.Unspecified,
                disabledIndicatorColor = Color.Unspecified,
                unfocusedIndicatorColor = Color.Unspecified,
                backgroundColor = Color.Unspecified,
            ),
            leadingIcon = {
                CoordinatesIcon(isCoordinatesAvailable)
            },
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            trailingIcon = {
                val visible = value != ""
                ClearIcon(visible, onValueChange)
            }
        )
    }
}

@Composable
private fun ClearIcon(visible: Boolean, onValueChange: (String) -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Icon(
            imageVector = Icons.Default.Clear,
            contentDescription = null,
            modifier = Modifier
                .clickable(
                    interactionSource = MutableInteractionSource(),
                    indication = rememberRipple(bounded = false)
                ) {
                    onValueChange("")
                }
        )
    }
}

@Composable
private fun CoordinatesIcon(isCoordinatesAvailable: Boolean?) {
    AnimatedContent(
        targetState = isCoordinatesAvailable,
        transitionSpec = {
            fadeIn(
                animationSpec = tween(500)
            ) togetherWith fadeOut(animationSpec = tween(500))
        },
        label = "Animated location icon"
    ) { targetState ->
        when (targetState) {
            true -> {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_location_on_24),
                    tint = MaterialTheme.colors.primary,
                    contentDescription = null
                )
            }

            false -> {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_location_off_24),
                    tint = MaterialTheme.colors.error,
                    contentDescription = null
                )
            }

            null -> {
                val infiniteTransition = rememberInfiniteTransition(label = "Undefined coordinates animation")
                val color by infiniteTransition.animateColor(
                    initialValue = MaterialTheme.colors.error,
                    targetValue = MaterialTheme.colors.primary,
                    animationSpec = infiniteRepeatable(
                        animation = tween(500),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "Undefined coordinates animation"
                )

                Icon(
                    painter = painterResource(id = R.drawable.baseline_location_on_24),
                    tint = color,
                    contentDescription = null
                )
            }
        }
    }
}

@Preview
@Composable
fun SearchBarCoordinatesAvailablePreview() {
    MaterialTheme {
        SearchBar(
            value = "Amst",
            isCoordinatesAvailable = true,
            onValueChange = {},
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Preview
@Composable
fun SearchBarCoordinatesUnavailablePreview() {
    MaterialTheme {
        SearchBar(
            value = "",
            isCoordinatesAvailable = false,
            onValueChange = {},
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Preview
@Composable
fun SearchBarCoordinatesUndefinedPreview() {
    MaterialTheme {
        SearchBar(
            value = "",
            isCoordinatesAvailable = null,
            onValueChange = {},
            modifier = Modifier.padding(8.dp)
        )
    }
}
