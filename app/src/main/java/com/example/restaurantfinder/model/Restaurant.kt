package com.example.restaurantfinder.model

data class Restaurant(
    val name: String,
    val cuisine: String,
    val address: String,
    val rating: Double,
    val schedule: String,
    val phone: String,
    val latitude: Double,
    val longitude: Double,
    val distance: Double? = null
)