// ... imports identiques

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantSearchScreen(
    currentLocation: Location?,
    placesClient: PlacesClient,
    placesService: PlacesService
) {
    // ... reste du code identique jusqu'à la Row des boutons

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .padding(bottom = if (showResults) 240.dp else 16.dp),  // Modifié ici
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (showResults) {
                FloatingActionButton(
                    onClick = { showResults = false },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(Icons.Filled.KeyboardArrowDown, "Masquer les résultats")
                }
            }
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {  // Ajout ici
                        currentLocation?.let { location ->
                            isLoading = true
                            try {
                                restaurants = withContext(Dispatchers.IO) {
                                    placesService.searchNearbyRestaurants(location)
                                }.sortedByDescending { it.rating }
                                showResults = true
                            } catch (e: Exception) {
                                Toast.makeText(context, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isLoading = false
                            }
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                LatLng(location.latitude, location.longitude), 
                                14f
                            )
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Filled.LocationOn, "Ma position")
            }
        }

        // ... reste du code identique
    }
}