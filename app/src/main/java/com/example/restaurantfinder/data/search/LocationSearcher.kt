package com.example.restaurantfinder.data.search

import com.example.restaurantfinder.data.mapper.PlaceMapper
import com.example.restaurantfinder.model.Restaurant
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.*
import kotlinx.coroutines.tasks.await

class LocationSearcher(private val placesClient: PlacesClient) {
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

    suspend fun search(prediction: AutocompletePrediction): List<Restaurant> {
        try {
            val placeRequest = FetchPlaceRequest.builder(
                prediction.placeId,
                listOf(Place.Field.LAT_LNG)
            ).build()
            
            val placeResponse = placesClient.fetchPlace(placeRequest).await()
            val location = placeResponse.place.latLng

            if (location != null) {
                return findNearbyRestaurants(location)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }

    private suspend fun findNearbyRestaurants(location: LatLng): List<Restaurant> {
        val bounds = RectangularBounds.newInstance(
            LatLng(location.latitude - 0.02, location.longitude - 0.02),
            LatLng(location.latitude + 0.02, location.longitude + 0.02)
        )

        val request = FindAutocompletePredictionsRequest.builder()
            .setLocationBias(bounds)
            .setTypesFilter(listOf("restaurant", "cafe"))
            .build()

        val response = placesClient.findAutocompletePredictions(request).await()
        
        return response.autocompletePredictions
            .take(20)
            .mapNotNull { prediction ->
                try {
                    val detailsRequest = FetchPlaceRequest.builder(
                        prediction.placeId,
                        placeFields
                    ).build()
                    
                    val detailsResponse = placesClient.fetchPlace(detailsRequest).await()
                    PlaceMapper().convertToRestaurant(detailsResponse.place, location)
                } catch (e: Exception) {
                    null
                }
            }
            .sortedByDescending { it.rating }
    }
}