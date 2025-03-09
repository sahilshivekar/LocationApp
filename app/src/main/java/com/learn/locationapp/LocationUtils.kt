package com.learn.locationapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

class LocationUtils(private val context: Context) {

    private val _fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    val requiredLocationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    fun isGPSEnabled(context: Context): Boolean {
        val locationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun launchPromptForGPS(context: Context) {
        val intent: Intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast
                .makeText(
                    context,
                    "Unable to open settings, Please turn on the GPS location for this feature to work",
                    Toast.LENGTH_LONG
                )
                .show()
        }
    }

    fun isLocationPermissionGranted(): Boolean {
        return requiredLocationPermissions.all { permission ->
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }


    @SuppressLint("MissingPermission")
    fun requestLocationUpdates(viewModel: LocationViewModel) {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.lastLocation?.let { location ->
                    viewModel.updateLocationData(
                        LocationData(
                            latitude = location.latitude,
                            longitude = location.longitude
                        )
                    )
                }
            }
        }

        val locationRequest = LocationRequest
            .Builder(1000L)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateIntervalMillis(1000L)
            .build()

        _fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun getAddressFromCoordinates(locationData: LocationData): String {
        var combinedAddress: String = ""
        val coordinates = LatLng(locationData.latitude, locationData.longitude)
        val geocoder = Geocoder(context, Locale.ENGLISH)
        val addressesList = geocoder.getFromLocation(
            coordinates.latitude,
            coordinates.longitude,
            5
        )
        addressesList?.let {
            if (addressesList.isNotEmpty()) {
                addressesList.forEach { address ->
                    address?.let {
                        for (i in 0..address.maxAddressLineIndex) {
                            combinedAddress += address.getAddressLine(i) + ""
                        }
                    }
                    combinedAddress += "\n"
                }
            }
        }
        return combinedAddress
    }


}