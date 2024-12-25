package com.example.restaurantfinder.ui.components

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
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
    onLocationSelected: (AutocompletePrediction) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    colors: TextFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White
    )
) {
    var searchQuery by remember { mutableStateOf(TextFieldValue()) }
    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var searchJob by remember { mutableStateOf<Job?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = modifier) {
        TextField(
            value = searchQuery,
            onValueChange = { newValue ->
                searchQuery = newValue
                searchJob?.cancel()
                if (newValue.text.length >= 2) {
                    searchJob = coroutineScope.launch {
                        Log.d("SearchBar", "Recherche pour: ${newValue.text}")
                        delay(300) // Délai pour éviter trop de requêtes
                        val request = FindAutocompletePredictionsRequest.builder()
                            .setTypeFilter(TypeFilter.CITIES)
                            .setQuery(newValue.text)
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
                .height(56.dp),
            placeholder = { Text("Entrez une ville ou un code postal") },
            singleLine = true,
            shape = shape,
            colors = colors,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Rechercher"
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                    // Vous pouvez ajouter une action de recherche ici si nécessaire
                }
            ),
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface)
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
                            searchQuery = TextFieldValue(prediction.getPrimaryText(null).toString())
                            isDropdownExpanded = false
                            onLocationSelected(prediction)
                            keyboardController?.hide()
                        }
                    )
                }
            }
        }
    }
}