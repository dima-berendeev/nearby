package org.berendeev.nearby.ui

import android.Manifest
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.berendeev.nearby.BuildConfig
import org.berendeev.nearby.data.model.Coordinates
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.OnSuccessListener
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

sealed interface LocationState {
    object GettingAvailability : LocationState {
        override fun toString(): String = "GettingAvailability"
    }

    data class LocationAvailable(val coordinates: Coordinates?) : LocationState {
        override fun toString(): String = "LocationAvailable($coordinates)"
    }

    object LocationUnavailable : LocationState {
        override fun toString(): String = "Location Unavailable"
    }
}

@RequiresPermission(
    anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION],
)
@Composable
fun produceLocationState(fineLocation: Boolean): State<LocationState> {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val locationRequest = LocationRequest.Builder(
        if (fineLocation) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY,
        TimeUnit.SECONDS.toMillis(20)
    )
        .setWaitForAccurateLocation(fineLocation)
        .setMinUpdateDistanceMeters(50f)
        .setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(20))
        .build()

    return produceState<LocationState>(initialValue = LocationState.GettingAvailability, key1 = lifecycleOwner) {
        val locationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        val lastCoordinates = AtomicReference<Coordinates?>(null)
        val locationCallback: LocationCallback = object : LocationCallback() {
            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                if (BuildConfig.DEBUG) {
                    log("LocationCallback: onLocationAvailability = ${locationAvailability.isLocationAvailable}")
                }
                value = if (locationAvailability.isLocationAvailable) {
                    if (BuildConfig.DEBUG) {
                        log("LocationCallback: LocationAvailable(${lastCoordinates.get()})")
                    }
                    LocationState.LocationAvailable(lastCoordinates.get())
                } else {
                    LocationState.LocationUnavailable
                }
            }

            override fun onLocationResult(result: LocationResult) {
                if (BuildConfig.DEBUG) {
                    log("LocationCallback: onLocationResult=${result.lastLocation}")
                }
                val lastLocation = result.lastLocation
                val coordinates = lastLocation?.let { Coordinates(lastLocation.latitude, lastLocation.longitude) }
                if (coordinates != null) {
                    lastCoordinates.set(coordinates)
                }
                value = LocationState.LocationAvailable(coordinates)
            }
        }

        val availabilityListener = OnSuccessListener<LocationAvailability> { locationAvailability ->
            if (BuildConfig.DEBUG) {
                log("Availability listener: ${locationAvailability.isLocationAvailable}")
            }
            if (value == LocationState.GettingAvailability) {
                value = if (locationAvailability.isLocationAvailable) {
                    if (BuildConfig.DEBUG) {
                        log("Availability listener: LocationAvailable(${lastCoordinates.get()})")
                    }
                    LocationState.LocationAvailable(lastCoordinates.get())
                } else {
                    LocationState.LocationUnavailable
                }
            }
        }
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                log("Started")
                value = LocationState.GettingAvailability
                locationClient.requestLocationUpdates(
                    locationRequest, locationCallback, Looper.getMainLooper()
                )
                locationClient.locationAvailability.addOnSuccessListener(availabilityListener)
                locationClient.flushLocations()
            } else if (event == Lifecycle.Event.ON_STOP) {
                log("Stopped")
                locationClient.removeLocationUpdates(locationCallback)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        awaitDispose {
            locationClient.removeLocationUpdates(locationCallback)
            lifecycleOwner.lifecycle.removeObserver(observer)
            log("Disposed")
        }
    }
}

fun LocationState.plusAssign(other: LocationState): LocationState {
    val oldStateIsBetter = this is LocationState.LocationAvailable &&
            other is LocationState.LocationAvailable &&
            this.coordinates == null
    return if (oldStateIsBetter) {
        this
    } else {
        other
    }
}

private fun log(message: String) {
    Log.d("LocationState", message)
}
