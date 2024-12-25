package com.example.restaurantfinder.data

import android.location.Location
import com.example.restaurantfinder.model.Restaurant
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceLikelihood
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.tasks.await

class PlacesService(private val placesClient: PlacesClient) {

    private val placeFields = listOf(
        Place.Field.NAME,
        Place.Field.ADDRESS,
        Place.Field.RATING,
        Place.Field.PHONE_NUMBER,
        Place.Field.OPENING_HOURS,
        Place.Field.LAT_LNG,
        Place.Field.TYPES
    )

    suspend fun searchNearbyRestaurants(location: Location): List<Restaurant> {
        val request = FindCurrentPlaceRequest.builder(placeFields).build()
        
        return try {
            val response = placesClient.findCurrentPlace(request).await()
            response.placeLikelihoods
                .filter { isRestaurant(it.place) }
                .map { placeLikelihood -> convertToRestaurant(placeLikelihood) }
                .sortedByDescending { it.rating }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun isRestaurant(place: Place): Boolean {
        return place.types?.contains(Place.Type.RESTAURANT) == true ||
               place.types?.contains(Place.Type.CAFE) == true
    }

    private fun convertToRestaurant(placeLikelihood: PlaceLikelihood): Restaurant {
        val place = placeLikelihood.place
        return Restaurant(
            name = place.name ?: "",
            cuisine = place.types?.firstOrNull()?.name ?: "Restaurant",
            address = place.address ?: "",
            rating = place.rating?.toDouble() ?: 0.0,
            schedule = place.openingHours?.weekdayText?.joinToString("\n") ?: "Horaires non disponibles",
            phone = place.phoneNumber ?: "Non disponible",
            latitude = place.latLng?.latitude ?: 0.0,
            longitude = place.latLng?.longitude ?: 0.0,
            distance = placeLikelihood.likelihood * 100 // Convertion de la probabilité en distance approximative
        )
    }
}