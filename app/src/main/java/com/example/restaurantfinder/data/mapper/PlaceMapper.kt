package com.example.restaurantfinder.data.mapper

import com.example.restaurantfinder.model.Restaurant
import com.example.restaurantfinder.utils.DistanceCalculator
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place

class PlaceMapper {
    fun convertToRestaurant(place: Place, center: LatLng?): Restaurant {
        val location = place.latLng
        val distance = if (location != null && center != null) {
            DistanceCalculator.calculate(
                center.latitude,
                center.longitude,
                location.latitude,
                location.longitude
            )
        } else null

        return Restaurant(
            name = place.name ?: "",
            cuisine = getCuisineType(place),
            address = place.address ?: "",
            rating = place.rating?.toDouble() ?: 0.0,
            schedule = place.openingHours?.weekdayText?.joinToString("\n") 
                      ?: "Horaires non disponibles",
            phone = place.phoneNumber ?: "Non disponible",
            latitude = location?.latitude ?: 0.0,
            longitude = location?.longitude ?: 0.0,
            distance = distance,
            priceLevel = place.priceLevel
        )
    }

    private fun getCuisineType(place: Place): String {
        return place.types?.firstOrNull { type ->
            type.contains("cuisine") || 
            type.contains("food") || 
            type == "restaurant"
        }?.replace("_", " ")?.capitalize() ?: "Restaurant"
    }
}