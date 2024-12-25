package com.example.restaurantfinder.data.search

import android.location.Location
import com.example.restaurantfinder.model.Restaurant
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.example.restaurantfinder.data.mapper.PlaceMapper
import kotlinx.coroutines.tasks.await

class NearbySearcher(private val placesClient: PlacesClient) {
    private val placeMapper = PlaceMapper()
    
    private val placeFields = listOf(
        Place.Field.NAME,
        Place.Field.ADDRESS,
        Place.Field.RATING,
        Place.Field.PHONE_NUMBER,
        Place.Field.OPENING_HOURS,
        Place.Field.LAT_LNG,
        Place.Field.TYPES,
        Place.Field.PRICE_LEVEL
    )

    suspend fun search(location: Location): List<Restaurant> {
        val request = FindCurrentPlaceRequest.builder(placeFields).build()
        
        return try {
            val response = placesClient.findCurrentPlace(request).await()
            response.placeLikelihoods
                .filter { isRestaurant(it.place) }
                .map { placeMapper.convertToRestaurant(it.place, it.place.latLng) }
                .sortedByDescending { it.rating }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun isRestaurant(place: Place): Boolean {
        return place.types?.contains(Place.Type.RESTAURANT) == true ||
               place.types?.contains(Place.Type.CAFE) == true
    }
}