package com.learn.locationapp

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.learn.locationapp.ui.theme.LocationAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LocationAppTheme {
                Scaffold { paddingValues ->
                    LocationScreen(paddingValues)
                }
            }
        }
    }
}


@Composable
fun LocationScreen(paddingValues: PaddingValues) {

    val context = LocalContext.current
    val locationUtils = LocationUtils(context)
    val viewModel: LocationViewModel = viewModel<LocationViewModel>()
    val locationData = viewModel.locationData.value


    val locationPermissionsDialog = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = {
            val isAllPermissionsGranted = it.all { permission ->
                permission.value   // checking if the value is of the permission is true for the permissions in the map
            }

            when (isAllPermissionsGranted) {
                true -> {
                    locationUtils.requestLocationUpdates(viewModel)
                }

                false -> {

                    val shouldShowRationale =
                        locationUtils.requiredLocationPermissions.any { permission ->
                            ActivityCompat.shouldShowRequestPermissionRationale(
                                context as Activity,
                                permission
                            )
                        }

                    when (shouldShowRationale) {
                        true -> {
                            Toast.makeText(
                                context,
                                "This is permission is required for the location feature to work",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        false -> {
                            Toast.makeText(
                                context,
                                "Please go to settings and grant the location permission",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }

    )


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        when (locationData) {
            null -> {
                Text(
                    text = "Location not available",
                    color = MaterialTheme.colorScheme.error
                )
            }

            else -> {
                Text(text = "Longitude: ${locationData.longitude}")
                Text(text = "Latitude: ${locationData.latitude}")
                Text(
                    textAlign = TextAlign.Justify,
                    text = locationUtils.getAddressFromCoordinates(locationData),
                )
            }
        }
        Button(
            onClick = {
                when (locationUtils.isLocationPermissionGranted()) {
                    true -> {
                        when (locationUtils.isGPSEnabled(context)) {
                            true -> {
                                locationUtils.requestLocationUpdates(viewModel)
                            }

                            false -> {
                                locationUtils.launchPromptForGPS(context)
                            }
                        }
                    }

                    false -> {
                        locationPermissionsDialog.launch(locationUtils.requiredLocationPermissions)
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = "Get Latitude and Longitude",
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }













}


@Preview(showSystemUi = true, device = Devices.PIXEL_7_PRO)
@Composable
fun LocationScreenPreview() {
    LocationAppTheme {
        Scaffold { paddingValues ->
            LocationScreen(paddingValues)
        }
    }
}