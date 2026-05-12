package com.example.hasiru.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager as AndroidLocationManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

class LocationManager(private val context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as AndroidLocationManager
        return locationManager.isProviderEnabled(AndroidLocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(AndroidLocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(onLocationReceived: (Double?, Double?, String?) -> Unit) {
        if (!isLocationEnabled()) {
            onLocationReceived(null, null, "Location services are disabled on your device.")
            return
        }

        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location ->
            if (location != null) {
                onLocationReceived(location.latitude, location.longitude, null)
            } else {
                // Fallback 1: Last Known Location
                fusedLocationClient.lastLocation.addOnSuccessListener { lastKnown ->
                    if (lastKnown != null) {
                        onLocationReceived(lastKnown.latitude, lastKnown.longitude, null)
                    } else {
                        // Fallback 2: Try with Balanced Accuracy (often works better indoors)
                        fusedLocationClient.getCurrentLocation(
                            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                            cancellationTokenSource.token
                        ).addOnSuccessListener { balancedLoc ->
                            if (balancedLoc != null) {
                                onLocationReceived(balancedLoc.latitude, balancedLoc.longitude, null)
                            } else {
                                onLocationReceived(null, null, "Unable to get GPS fix. Try moving to a window.")
                            }
                        }.addOnFailureListener {
                            onLocationReceived(null, null, "GPS request failed.")
                        }
                    }
                }.addOnFailureListener {
                    onLocationReceived(null, null, "Failed to access location hardware.")
                }
            }
        }.addOnFailureListener {
            onLocationReceived(null, null, "GPS connection error.")
        }
    }
}
