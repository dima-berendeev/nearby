package org.berendeev.nearby.placeslist

import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.printToLog
import androidx.test.core.graphics.writeToTestStorage
import org.berendeev.nearby.data.model.Place
import org.berendeev.nearby.data.model.UnknownDataLayerIssue
import org.berendeev.nearby.ui.CoordinatesIcon
import org.berendeev.nearby.ui.ErrorBlank
import org.berendeev.nearby.ui.LoadingBlank
import org.berendeev.nearby.ui.LocationPermissionsState
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class PlacesListScreenKtTest {
    @get:Rule(order = 0)
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val loadingBlankMatcher = hasTestTag(LoadingBlank.testTag)

    @get:Rule(order = 1)
    val testWatcher = object : TestWatcher() {
        override fun failed(e: Throwable?, description: Description) {
            composeTestRule.onNodeWithTag("PlacesListScreen", useUnmergedTree = true)
                .apply {
                    printToLog("ui-tree")

                    captureToImage()
                        .asAndroidBitmap()
                        .writeToTestStorage("${description.className}/${description.methodName}")
                }
        }
    }

    // Data related
    @Test
    fun givenStateLoading_thenProgressIsVisible() {
        composeTestRule.setContent {
            TestPlacesListScreen(uiState = PlacesListUiState.Loading)
        }
        composeTestRule
            .onNode(loadingBlankMatcher)
            .assertIsDisplayed()
    }

    @Test
    fun givenSuccessUiStateAndItemsNotEmpty_thenItemsVisibleAndProgressInvisible() {
        composeTestRule.setContent {
            TestPlacesListScreen(
                uiState = PlacesListUiState.Success(
                    listOf(Place("Place1", null, emptyList(), emptyList(), 100))
                )
            )
        }

        composeTestRule
            .onNode(loadingBlankMatcher)
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithTag(PlacesItems.testTag)
            .assertIsDisplayed()
    }

    @Test
    fun givenSuccessUiStateAndItemsEmpty_thenNoResultsBlank() {
        composeTestRule.setContent {
            TestPlacesListScreen(
                uiState = PlacesListUiState.Success(
                    emptyList()
                )
            )
        }

        composeTestRule
            .onNodeWithTag(NoResultBlank.testTag)
            .assertIsDisplayed()
    }

    @Test
    fun givenFailure_thenErrorBlank() {
        composeTestRule.setContent {
            TestPlacesListScreen(
                uiState = PlacesListUiState.Failure(UnknownDataLayerIssue, {})
            )
        }

        composeTestRule
            .onNodeWithTag(ErrorBlank.testTag)
            .assertIsDisplayed()
    }

    // Banner
    @Test
    fun givenOffline_thenOfflineBannerVisible() {
        composeTestRule.setContent {
            TestPlacesListScreen(isOnline = false)
        }
        composeTestRule.onNodeWithTag(PlacesListScreen.Banner.Offline.testTag)
            .assertExists()
    }

    @Test
    fun givenOfflineAndLocationPermissionDenied_thenOfflineBannerVisible() {
        composeTestRule.setContent {
            TestPlacesListScreen(
                isOnline = false,
                permissionsState = DeniedLocationPermissionsState
            )
        }
        composeTestRule.onNodeWithTag(PlacesListScreen.Banner.Offline.testTag)
            .assertExists()
    }

    @Test
    fun givenOnlineAndLocationPermissionDenied_thenLocationPermissionDeniedBannerVisible() {
        composeTestRule.setContent {
            TestPlacesListScreen(
                isOnline = true,
                permissionsState = DeniedLocationPermissionsState
            )
        }
        composeTestRule.onNodeWithTag(PlacesListScreen.Banner.LocationPermissionDenied.testTag)
            .assertExists()
    }

    @Test
    fun givenOnlineAndCoarseLocationPermissionAllowed_thenFineLocationPermissionDeniedBannerVisible() {
        composeTestRule.setContent {
            TestPlacesListScreen(
                isOnline = true,
                permissionsState = ApproximateOnlyLocationPermissionsState
            )
        }
        composeTestRule.onNodeWithTag(PlacesListScreen.Banner.FineLocationDisabled.testTag)
            .assertExists()
    }

    @Test
    fun givenOnlineAndLocationPermissionGranted_thenNoBanner() {
        composeTestRule.setContent {
            TestPlacesListScreen(isOnline = true)
        }
        composeTestRule.onNodeWithTag(PlacesListScreen.Banner.Offline.testTag)
            .assertDoesNotExist()
    }

//    Coordinates icon
    @Test
    fun givenCoordinatesAvailable_thenIconIsAvailable() {
        composeTestRule.setContent {
            TestPlacesListScreen(isCoordinatesAvailable = true)
        }
        composeTestRule.onNodeWithTag(CoordinatesIcon.Available.testTag, useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun givenCoordinatesUnavailable_thenIconIsUnavailable() {
        composeTestRule.setContent {
            TestPlacesListScreen(isCoordinatesAvailable = false)
        }
        composeTestRule.onNodeWithTag(CoordinatesIcon.Unavailable.testTag, useUnmergedTree = true)
            .assertExists()
    }



    @Test
    fun givenCoordinatesNull_thenIconIsUndefined() {
        composeTestRule.setContent {
            TestPlacesListScreen(isCoordinatesAvailable = null)
        }
        composeTestRule.onNodeWithTag(CoordinatesIcon.Undefined.testTag, useUnmergedTree = true)
            .assertExists()
    }

    companion object {
        val DeniedLocationPermissionsState = object : LocationPermissionsState.Denied() {
            override fun requestPermissions() {
            }
        }

        val ApproximateOnlyLocationPermissionsState = object : LocationPermissionsState.ApproximateOnly() {
            override fun requestPermissions() {
            }
        }
    }
}
