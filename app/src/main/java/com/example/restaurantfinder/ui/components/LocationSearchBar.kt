package com.example.restaurantfinder.ui.components

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.model.LocationBias
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSearchBar(
    placesClient: PlacesClient,
    onLocationSelected: (AutocompletePrediction) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var searchJob by remember { mutableStateOf<Job?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column {
        TextField(
            value = searchQuery,
            onValueChange = { newQuery ->
                Log.d("SearchBar", "Nouvelle recherche: $newQuery")
                searchQuery = newQuery
                searchJob?.cancel()
                if (newQuery.length >= 2) {
                    searchJob = coroutineScope.launch {
                        delay(300)
                        try {
                            val bias = RectangularBounds.newInstance(
                                LatLng(42.0, -5.0),  // SW France
                                LatLng(51.0, 8.0)     // NE France
                            )

                            val request = FindAutocompletePredictionsRequest.builder()
                                .setLocationBias(bias)
                                .setTypesFilter(listOf("locality", "postal_code"))
                                .setQuery(newQuery)
                                .setCountries("FR")
                                .build()

                            Log.d("SearchBar", "Envoi requête pour: $newQuery")
                            val response = placesClient.findAutocompletePredictions(request).await()
                            Log.d("SearchBar", "Reçu ${response.autocompletePredictions.size} prédictions")
                            
                            predictions = response.autocompletePredictions
                            isDropdownExpanded = predictions.isNotEmpty()
                        } catch (e: Exception) {
                            Log.e("SearchBar", "Erreur recherche: ${e.message}")
                            e.printStackTrace()
                            Toast.makeText(context, "Erreur de recherche: ${e.message}", Toast.LENGTH_SHORT).show()
                            predictions = emptyList()
                            isDropdownExpanded = false
                        }
                    }
                } else {
                    predictions = emptyList()
                    isDropdownExpanded = false
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Entrez une ville ou un code postal") },
            singleLine = true
        )

        if (isDropdownExpanded) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
            ) {
                items(predictions) { prediction ->
                    ListItem(
                        headlineContent = { Text(prediction.getPrimaryText(null).toString()) },
                        supportingContent = { Text(prediction.getSecondaryText(null).toString()) },
                        modifier = Modifier.clickable {
                            searchQuery = prediction.getPrimaryText(null).toString()
                            isDropdownExpanded = false
                            onLocationSelected(prediction)
                        }
                    )
                }
            }
        }
    }
}