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
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
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
                searchQuery = newQuery
                searchJob?.cancel()
                if (newQuery.length >= 2) {
                    searchJob = coroutineScope.launch {
                        Log.d("SearchBar", "Recherche pour: $newQuery")
                        delay(300) // Délai pour éviter trop de requêtes
                        val request = FindAutocompletePredictionsRequest.builder()
                            .setTypeFilter(TypeFilter.CITIES)
                            .setQuery(newQuery)
                            .build()

                        try {
                            Log.d("SearchBar", "Envoi de la requête d'autocomplétion")
                            val response = placesClient.findAutocompletePredictions(request).await()
                            Log.d("SearchBar", "Réponse reçue: ${response.autocompletePredictions.size} prédictions")
                            predictions = response.autocompletePredictions
                            isDropdownExpanded = predictions.isNotEmpty()
                        } catch (e: Exception) {
                            Log.e("SearchBar", "Erreur lors de la recherche", e)
                            Toast.makeText(context, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
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
                            Log.d("SearchBar", "Ville sélectionnée: ${prediction.getFullText(null)}")
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