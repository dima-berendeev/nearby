package org.berendeev.nearby.placeslist

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.berendeev.nearby.data.model.Location
import org.berendeev.nearby.data.model.Place

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PlaceItem(place: Place) {
    ListItem(
        modifier = Modifier.fillMaxWidth(),
        icon = {
            val iconSize = 64.dp
            val iconSizePx = with(LocalDensity.current) { iconSize.toPx() }.toInt()
            val modelUrl = place.photos.getOrNull(0)?.getUrl(iconSizePx, iconSizePx)
            AsyncImage(
                modifier = Modifier
                    .size(iconSize)
                    .clip(RoundedCornerShape(4.dp)),
                model = modelUrl,
                placeholder = ColorPainter(Color.LightGray),
                fallback = ColorPainter(Color.LightGray),
                contentDescription = null
            )
        },
        text = {
            Text(
                text = place.name,
            )
        },
        secondaryText = {
            if (place.location != null) {
                Text(
                    text = place.location.name,
                )
            }
        },
        trailing = {
            place.distance?.let {
                Text(formatDistance(place.distance))
            }
        }
    )
}

private fun formatDistance(value: Int): String {
    return if (value < 1000) {
        "${value}m"
    } else {
        "${value / 1000}.${(value % 1000).toString()[0]}km"
    }
}

@Preview()
@Composable
private fun PlaceItemPreview() {
    val place = Place(
        name = "Testpaviljoen Amsterdam Noord",
        location = Location(name = "Buikslotermeerplein 2007"),
        categories = emptyList(),
        photos = emptyList(),
        distance = 15
    )
    val places = (0..10).map { place }
    LazyColumn {
        items(places) {
            PlaceItem(place = it)
        }
    }
}
