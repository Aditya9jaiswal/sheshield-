package com.example.sheshield0.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*

class LocationHelper(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private var locationCallback: LocationCallback? = null

    companion object {
        private const val TAG = "LocationHelper"
        private const val UPDATE_INTERVAL = 5000L // 5 seconds
        private const val FASTEST_INTERVAL = 2000L
    }

    interface LocationListener {
        fun onLocationChanged(location: Location)
        fun onFailure(error: Exception)
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(listener: LocationListener) {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            UPDATE_INTERVAL
        ).setMinUpdateIntervalMillis(FASTEST_INTERVAL)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    listener.onLocationChanged(location)
                    Log.d(TAG, "Live location: ${location.latitude}, ${location.longitude}")
                }
            }

            override fun onLocationAvailability(availability: LocationAvailability) {
                if (!availability.isLocationAvailable) {
                    listener.onFailure(Exception("Location unavailable"))
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(onSuccess: (location: Location) -> Unit, onFailure: (Exception) -> Unit) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    onSuccess(location)
                } else {
                    onFailure(Exception("Last location not available"))
                }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            Log.d(TAG, "Live location updates stopped")
        }
    }

    fun cleanup() {
        stopLocationUpdates()
    }
}
