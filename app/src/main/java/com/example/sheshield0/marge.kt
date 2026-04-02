package com.example.sheshield0

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sheshield0.utils.LocationHelper

class marge : AppCompatActivity() {

    private var locationHelper: LocationHelper? = null
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    companion object {
        private const val TAG = "MargeActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_marge)

        // Adjust padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Start live location fetch
        startLiveLocation()
    }

    private fun startLiveLocation() {
        locationHelper = LocationHelper(this).apply {
            startLocationUpdates(object : LocationHelper.LocationListener {
                override fun onLocationChanged(location: Location) {
                    latitude = location.latitude
                    longitude = location.longitude
                    Log.d(TAG, "Live location: $latitude, $longitude")

                    // Update UI
                    findViewById<TextView>(R.id.tvLatitude).text = "Latitude: $latitude"
                    findViewById<TextView>(R.id.tvLongitude).text = "Longitude: $longitude"
                }

                override fun onFailure(error: Exception) {
                    Log.e(TAG, "Location error: ${error.message}")
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationHelper?.cleanup()
    }
}
