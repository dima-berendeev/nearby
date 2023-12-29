package org.berendeev.nearby.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import org.berendeev.nearby.BuildConfig
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationAware(content: @Composable (permissionsState: LocationPermissionsState, LocationState?) -> Unit) {
    val permissionState: MultiplePermissionsState = rememberMultiplePermissionsState(
        listOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    )

    var permissionsSettingsDialog by remember {
        mutableStateOf(false)
    }

    if (permissionsSettingsDialog) {
        LocationPermissionSettingDialog { permissionsSettingsDialog = false }
    }

    val coarseGranted = permissionState.permissions
        .firstOrNull { it.permission == Manifest.permission.ACCESS_COARSE_LOCATION }
        ?.status?.isGranted == true

    val permissionsMode = when {
        permissionState.allPermissionsGranted -> {
            LocationPermissionsState.Fine
        }

        coarseGranted -> {
            val multiplePermissionsLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { result: Map<String, Boolean> ->
                val fineWasGranted = result[Manifest.permission.ACCESS_FINE_LOCATION]!!
                if (!fineWasGranted) {
                    permissionsSettingsDialog = true
                }
            }
            remember {
                object : LocationPermissionsState.ApproximateOnly() {
                    override fun requestPermissions() {
                        multiplePermissionsLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        )
                    }
                }
            }
        }
        // no permissions granted
        else -> {
            val multiplePermissionsLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { result: Map<String, Boolean> ->
                val nonePermissionsWasGranted = result.values.all { !it }
                if (nonePermissionsWasGranted) {
                    permissionsSettingsDialog = true
                }
            }
            remember {
                object : LocationPermissionsState.Denied() {
                    override fun requestPermissions() {
                        multiplePermissionsLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        )
                    }
                }
            }
        }
    }

    val locationState = when (permissionsMode) {
        is LocationPermissionsState.ApproximateOnly -> produceLocationState(fineLocation = false).value
        LocationPermissionsState.Fine -> produceLocationState(fineLocation = true).value
        is LocationPermissionsState.Denied -> null
    }

    LaunchedEffect(key1 = permissionsMode, key2 = locationState) {
        if(BuildConfig.DEBUG){
            Log.d("LocationAware", "$permissionsMode, $locationState")
        }
    }
    content(permissionsMode, locationState)
}

@Composable
private fun LocationPermissionSettingDialog(onCloseRequest: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = { onCloseRequest() },
        title = {
            Text(text = "Location is not allowed")
        },
        text = {
            Text(text = "For better experience enable permissions in settings")
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onCloseRequest()
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            ) {
                Text("CONTINUE")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onCloseRequest()
                }
            ) {
                Text("DISMISS")
            }
        }
    )
}

sealed interface LocationPermissionsState {
    abstract class Denied : LocationPermissionsState {
        abstract fun requestPermissions()
        override fun toString(): String = "Location permissions denied"
    }

    abstract class ApproximateOnly : LocationPermissionsState {
        abstract fun requestPermissions()
        override fun toString() = "Location permissions: ApproximateOnly"
    }

    object Fine : LocationPermissionsState {
        override fun toString() = "Location permissions: Fine"
    }
}
