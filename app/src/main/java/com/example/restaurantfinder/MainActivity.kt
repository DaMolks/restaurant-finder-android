package com.example.restaurantfinder

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.example.restaurantfinder.data.PlacesService
import com.example.restaurantfinder.ui.screens.RestaurantSearchScreen
import com.example.restaurantfinder.ui.theme.RestaurantFinderTheme

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient
    private lateinit var placesService: PlacesService
    private var currentLocation: Location? = null

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                getCurrentLocation()
            }
            else -> {
                Toast.makeText(
                    this,
                    "La géolocalisation est nécessaire pour trouver les restaurants proches",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialisation de Places
        try {
            if (!Places.isInitialized()) {
                Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
                Log.d("Places", "API Key: ${BuildConfig.MAPS_API_KEY}")
            }
            placesClient = Places.createClient(this)
            placesService = PlacesService(placesClient)
            Log.d("Places", "Places initialisé avec succès")
        } catch (e: Exception) {
            Log.e("Places", "Erreur lors de l'initialisation de Places", e)
            Toast.makeText(this, "Erreur d'initialisation de Places: ${e.message}", Toast.LENGTH_LONG).show()
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkLocationPermission()
        
        setContent {
            RestaurantFinderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RestaurantSearchScreen(
                        currentLocation = currentLocation,
                        placesClient = placesClient,
                        placesService = placesService
                    )
                }
            }
        }
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation()
            }
            else -> {
                locationPermissionRequest.launch(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ))
            }
        }
    }

    private fun getCurrentLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                currentLocation = location
                Log.d("Location", "Position obtenue: ${location?.latitude}, ${location?.longitude}")
            }.addOnFailureListener { e ->
                Log.e("Location", "Erreur lors de l'obtention de la position", e)
            }
        } catch (e: SecurityException) {
            Log.e("Location", "Erreur de permission", e)
            Toast.makeText(
                this,
                "Erreur lors de l'accès à la localisation",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}