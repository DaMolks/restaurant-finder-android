package com.example.restaurantfinder.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.restaurantfinder.model.Restaurant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantCard(restaurant: Restaurant) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = restaurant.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = restaurant.cuisine,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Adresse: ${restaurant.address}")
            Text(text = "Note: ${restaurant.rating}/5")
            restaurant.distance?.let {
                Text(text = "Distance: ${String.format("%.1f", it)} km")
            }
            Text(text = "Horaires: ${restaurant.schedule}")
            Text(text = "Téléphone: ${restaurant.phone}")
        }
    }
}