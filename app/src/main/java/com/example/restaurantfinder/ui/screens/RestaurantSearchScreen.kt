package com.example.restaurantfinder.ui.screens

import android.location.Location
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantSearchScreen(
    currentLocation: Location?,
    placesClient: PlacesClient,
    placesService: PlacesService
) {
    var restaurants by remember { mutableStateOf<List<Restaurant>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showResults by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val defaultLocation = LatLng(48.8566, 2.3522)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            currentLocation?.let { LatLng(it.latitude, it.longitude) } ?: defaultLocation, 
            12f
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                compassEnabled = true,
                myLocationButtonEnabled = false
            )
        ) {
            restaurants.forEach { restaurant ->
                Marker(
                    state = MarkerState(LatLng(restaurant.latitude, restaurant.longitude)),
                    title = restaurant.name
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            LocationSearchBar(
                placesClient = placesClient,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.9f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.8f)
                ),
                onLocationSelected = { prediction ->
                    coroutineScope.launch {
                        try {
                            isLoading = true
                            val placeFields = listOf(Place.Field.LAT_LNG)
                            val request = FetchPlaceRequest.newInstance(prediction.placeId, placeFields)
                            val placeResponse = withContext(Dispatchers.IO) {
                                placesClient.fetchPlace(request).await()
                            }
                            val latLng = placeResponse.place.latLng

                            if (latLng != null) {
                                restaurants = withContext(Dispatchers.IO) {
                                    placesService.searchRestaurantsByLocation(prediction)
                                }.sortedByDescending { it.rating }
                                showResults = true
                                cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 14f)
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false
                        }
                    }
                }
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .padding(bottom = if (showResults) 240.dp else 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (showResults) {
                FloatingActionButton(
                    onClick = { showResults = false },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(Icons.Filled.KeyboardArrowDown, "Masquer les résultats")
                }
            }
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        currentLocation?.let { location ->
                            isLoading = true
                            try {
                                restaurants = withContext(Dispatchers.IO) {
                                    placesService.searchNearbyRestaurants(location)
                                }.sortedByDescending { it.rating }
                                showResults = true
                            } catch (e: Exception) {
                                Toast.makeText(context, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isLoading = false
                            }
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                LatLng(location.latitude, location.longitude), 
                                14f
                            )
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Filled.LocationOn, "Ma position")
            }
        }

        AnimatedVisibility(
            visible = showResults,
            modifier = Modifier
                .align(Alignment.BottomCenter),
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "${restaurants.size} restaurants trouvés",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(restaurants) { restaurant ->
                                RestaurantCard(restaurant = restaurant)
                            }
                        }
                    }
                }
            }
        }
    }
}