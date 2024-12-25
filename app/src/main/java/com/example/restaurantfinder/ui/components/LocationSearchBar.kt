package com.example.restaurantfinder.ui.components

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.zIndex
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

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(10f) // Assurer que la barre de recherche est au-dessus de tout
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { newValue ->
                    searchQuery = newValue
                    searchJob?.cancel()
                    if (newValue.text.length >= 2) {
                        searchJob = coroutineScope.launch {
                            Log.d("LocationSearchBar", "Recherche de: ${newValue.text}")
                            delay(300)
                            val request = FindAutocompletePredictionsRequest.builder()
                                .setQuery(newValue.text)
                                .build()

                            try {
                                val response = placesClient.findAutocompletePredictions(request).await()
                                Log.d("LocationSearchBar", "Réponse reçue: ${response.autocompletePredictions.size} prédictions")
                                predictions = response.autocompletePredictions
                                isDropdownExpanded = predictions.isNotEmpty()
                            } catch (e: Exception) {
                                Log.e("LocationSearchBar", "Erreur de recherche", e)
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
                    .height(56.dp),
                placeholder = { Text("Rechercher un lieu, un restaurant...") },
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
                    onSearch = { keyboardController?.hide() }
                )
            )
        }

        // Liste déroulante des prédictions
        if (isDropdownExpanded) {
            Log.d("LocationSearchBar", "Affichage de ${predictions.size} prédictions")
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .zIndex(9f)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = MaterialTheme.shapes.medium
                    )
                    .background(Color.White)
            ) {
                LazyColumn {
                    items(predictions) { prediction ->
                        ListItem(
                            headlineContent = { Text(prediction.getPrimaryText(null).toString()) },
                            supportingContent = { Text(prediction.getSecondaryText(null).toString()) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    searchQuery = TextFieldValue(prediction.getPrimaryText(null).toString())
                                    isDropdownExpanded = false
                                    onLocationSelected(prediction)
                                    keyboardController?.hide()
                                }
                                .background(Color.White)
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}