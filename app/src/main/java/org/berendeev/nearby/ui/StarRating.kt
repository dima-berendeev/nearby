package org.berendeev.nearby.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun StarRating(rating: Float, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        for (i in 1..5)
            Box(modifier = Modifier.size(24.dp)) {
                val icon = if (i <= rating.roundToInt()) {
                    Icons.Outlined.Star
                } else {
                    Icons.Outlined.StarOutline
                }
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colors.secondaryVariant)
            }
    }
}

@Preview
@Composable
fun StarRatingPreview() {
    MaterialTheme {
        Column {
            StarRatingItem(rating = 1.0f)
            StarRatingItem(rating = 2.0f)
            StarRatingItem(rating = 2.3f)
            StarRatingItem(rating = 2.6f)
            StarRatingItem(rating = 3.0f)
            StarRatingItem(rating = 4.0f)
            StarRatingItem(rating = 5.0f)
        }
    }
}

@Composable
fun StarRatingItem(rating: Float) {
    Text("Rating: $rating")
    StarRating(rating = rating, modifier = Modifier.wrapContentSize())
    Spacer(modifier = Modifier.height(12.dp))
}
