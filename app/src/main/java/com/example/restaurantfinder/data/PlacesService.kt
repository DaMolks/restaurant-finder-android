package com.example.restaurantfinder.data

import android.location.Location
import com.example.restaurantfinder.model.Restaurant
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.example.restaurantfinder.data.mapper.PlaceMapper
import com.example.restaurantfinder.data.search.NearbySearcher
import com.example.restaurantfinder.data.search.LocationSearcher

class PlacesService(
    private val placesClient: PlacesClient,
    private val nearbySearcher: NearbySearcher = NearbySearcher(placesClient),
    private val locationSearcher: LocationSearcher = LocationSearcher(placesClient),
    private val placeMapper: PlaceMapper = PlaceMapper()
) {
    suspend fun searchNearbyRestaurants(location: Location): List<Restaurant> {
        return nearbySearcher.search(location)
    }

    suspend fun searchRestaurantsByLocation(prediction: AutocompletePrediction): List<Restaurant> {
        return locationSearcher.search(prediction)
    }
}