package com.example.restaurantfinder.ui.screens

import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.restaurantfinder.data.PlacesService
import com.example.restaurantfinder.model.Restaurant
import com.example.restaurantfinder.ui.components.*
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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

    LaunchedEffect(currentLocation) {
        if (currentLocation != null) {
            isLoading = true
            try {
                restaurants = placesService.searchNearbyRestaurants(currentLocation)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (currentLocation != null) {
            Text(
                text = "Restaurants près de vous",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        LocationSearchBar(
            placesClient = placesClient,
            onLocationSelected = { prediction ->
                scope.launch {
                    isLoading = true
                    try {
                        val results = placesService.searchRestaurantsByLocation(prediction)
                        restaurants = results
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            "Erreur lors de la recherche",
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

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(restaurants) { restaurant ->
                RestaurantCard(restaurant = restaurant)
            }
        }
    }
}