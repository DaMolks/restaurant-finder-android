package com.example.restaurantfinder.utils

import android.location.Location
import com.example.restaurantfinder.model.Restaurant

fun getNearbyRestaurants(latitude: Double, longitude: Double): List<Restaurant> {
    return listOf(
        Restaurant(
            name = "Le Gourmet Local",
            cuisine = "Cuisine française",
            address = "123 rue Principale",
            rating = 4.5,
            schedule = "12h-23h",
            phone = "01 23 45 67 89",
            latitude = latitude + 0.001,
            longitude = longitude + 0.001,
            distance = 0.3
        ),
        Restaurant(
            name = "L'Italien du Coin",
            cuisine = "Cuisine italienne",
            address = "45 avenue des Restaurants",
            rating = 4.3,
            schedule = "11h30-22h30",
            phone = "01 23 45 67 90",
            latitude = latitude + 0.002,
            longitude = longitude + 0.002,
            distance = 0.5
        )
    )
}

fun searchRestaurantsByCity(city: String): List<Restaurant> {
    return listOf(
        Restaurant(
            name = "Le Bistrot de $city",
            cuisine = "Cuisine traditionnelle",
            address = "1 place centrale, $city",
            rating = 4.4,
            schedule = "12h-22h",
            phone = "01 23 45 67 91",
            latitude = 48.8566,
            longitude = 2.3522,
            distance = null
        )
    )
}