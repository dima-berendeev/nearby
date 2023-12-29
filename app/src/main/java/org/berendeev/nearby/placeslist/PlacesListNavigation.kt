package org.berendeev.nearby.placeslist

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

const val placesListNavigationRoute = "places-list"

fun NavController.navigatePlacesList(navOptions: NavOptions? = null) {
    this.navigate(placesListNavigationRoute, navOptions)
}

fun NavGraphBuilder.placesListScreen() {
    composable(placesListNavigationRoute) {
        PlacesListRoute()
    }
}
