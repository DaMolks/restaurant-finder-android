package com.example.restaurantfinder.ui.screens

import android.location.Location
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.restaurantfinder.data.PlacesService
import com.example.restaurantfinder.model.Restaurant
import com.example.restaurantfinder.ui.components.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, MapsComposeExperimentalApi::class)
@Composable
fun RestaurantSearchScreen(
    currentLocation: Location?,
    placesClient: PlacesClient,
    placesService: PlacesService
) {
    var restaurants by remember { mutableStateOf<List<Restaurant>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Position par défaut (Paris)
    val defaultLocation = LatLng(48.8566, 2.3522)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            currentLocation?.let { LatLng(it.latitude, it.longitude) } ?: defaultLocation, 
            12f
        )
    }

    LaunchedEffect(currentLocation) {
        if (currentLocation != null) {
            isLoading = true
            try {
                restaurants = placesService.searchNearbyRestaurants(currentLocation)
                cameraPositionState.position = CameraPosition.fromLatLngZoom(
                    LatLng(currentLocation.latitude, currentLocation.longitude), 
                    12f
                )
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Erreur lors de la recherche des restaurants",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Fond de carte Google Maps
        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = true
            )
        )

        // Conteneur semi-transparent pour le contenu
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                LocationSearchBar(
                    placesClient = placesClient,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large, // Arrondit les coins
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.9f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.8f)
                    ),
                    onLocationSelected = { prediction ->
                        scope.launch {
                            isLoading = true
                            try {
                                // Récupérer les détails du lieu
                                val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
                                val request = FetchPlaceRequest.newInstance(prediction.placeId, placeFields)
                                val placeResponse = placesClient.fetchPlace(request).await()
                                val place = placeResponse.place

                                // Rechercher les restaurants
                                val results = placesService.searchRestaurantsByLocation(prediction)
                                restaurants = results

                                // Mettre à jour la position de la caméra
                                place.latLng?.let { latLng ->
                                    cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                        latLng, 
                                        12f
                                    )
                                } ?: run {
                                    // Utiliser la position par défaut si aucune coordonnée n'est trouvée
                                    cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                        defaultLocation, 
                                        12f
                                    )
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Erreur lors de la recherche: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                // Liste des restaurants
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(restaurants) { restaurant ->
                        RestaurantCard(
                            restaurant = restaurant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}