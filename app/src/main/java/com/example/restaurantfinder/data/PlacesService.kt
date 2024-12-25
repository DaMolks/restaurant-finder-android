package com.example.restaurantfinder.data

import android.location.Location
import android.util.Log
import com.example.restaurantfinder.model.Restaurant
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.tasks.await

class PlacesService(private val placesClient: PlacesClient) {

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

    suspend fun searchNearbyRestaurants(location: Location): List<Restaurant> {
        try {
            Log.d("PlacesService", "Recherche restaurants près de: ${location.latitude}, ${location.longitude}")
            
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery("restaurant")
                .setOrigin(LatLng(location.latitude, location.longitude))
                .setCountries("FR")
                .build()

            val response = placesClient.findAutocompletePredictions(request).await()
            Log.d("PlacesService", "Trouvé ${response.autocompletePredictions.size} prédictions")

            return response.autocompletePredictions.mapNotNull { prediction ->
                try {
                    val placeRequest = FetchPlaceRequest.newInstance(prediction.placeId, placeFields)
                    val placeResponse = placesClient.fetchPlace(placeRequest).await()
                    val place = placeResponse.place
                    
                    if (place.latLng != null) {
                        Restaurant(
                            name = place.name ?: "",
                            cuisine = getCuisineType(place),
                            address = place.address ?: "",
                            rating = place.rating?.toDouble() ?: 0.0,
                            schedule = place.openingHours?.weekdayText?.joinToString("\n") 
                                      ?: "Horaires non disponibles",
                            phone = place.phoneNumber ?: "Non disponible",
                            latitude = place.latLng!!.latitude,
                            longitude = place.latLng!!.longitude
                        )
                    } else null
                } catch (e: Exception) {
                    Log.e("PlacesService", "Erreur lors de la récupération des détails: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("PlacesService", "Erreur lors de la recherche: ${e.message}")
            throw e
        }
    }

    suspend fun searchRestaurantsByLocation(prediction: AutocompletePrediction): List<Restaurant> {
        try {
            val placeRequest = FetchPlaceRequest.newInstance(prediction.placeId, listOf(Place.Field.LAT_LNG))
            val placeResponse = placesClient.fetchPlace(placeRequest).await()
            val location = placeResponse.place.latLng

            if (location != null) {
                val request = FindAutocompletePredictionsRequest.builder()
                    .setQuery("restaurant")
                    .setOrigin(location)
                    .setCountries("FR")
                    .build()

                val response = placesClient.findAutocompletePredictions(request).await()
                return response.autocompletePredictions.mapNotNull { restaurantPrediction ->
                    try {
                        val detailsRequest = FetchPlaceRequest.newInstance(restaurantPrediction.placeId, placeFields)
                        val detailsResponse = placesClient.fetchPlace(detailsRequest).await()
                        val place = detailsResponse.place

                        if (place.latLng != null) {
                            Restaurant(
                                name = place.name ?: "",
                                cuisine = getCuisineType(place),
                                address = place.address ?: "",
                                rating = place.rating?.toDouble() ?: 0.0,
                                schedule = place.openingHours?.weekdayText?.joinToString("\n") 
                                          ?: "Horaires non disponibles",
                                phone = place.phoneNumber ?: "Non disponible",
                                latitude = place.latLng!!.latitude,
                                longitude = place.latLng!!.longitude
                            )
                        } else null
                    } catch (e: Exception) {
                        Log.e("PlacesService", "Erreur détails restaurant: ${e.message}")
                        null
                    }
                }
            }
            return emptyList()
        } catch (e: Exception) {
            Log.e("PlacesService", "Erreur recherche par lieu: ${e.message}")
            return emptyList()
        }
    }

    private fun getCuisineType(place: Place): String {
        return place.getTypes()?.firstOrNull { type -> 
            type.toString().contains("RESTAURANT") || 
            type.toString().contains("FOOD") || 
            type.toString().contains("CAFE")
        }?.toString()?.replace("_", " ")?.replaceFirstChar { it.uppercase() } ?: "Restaurant"
    }
}