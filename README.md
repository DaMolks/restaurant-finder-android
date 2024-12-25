# Restaurant Finder

Application Android de recherche de restaurants avec géolocalisation.

## Fonctionnalités

- Géolocalisation automatique
- Recherche de restaurants à proximité
- Recherche par ville
- Affichage des détails des restaurants (note, distance, horaires, etc.)

## Configuration requise

- Android Studio Hedgehog | 2023.1.1 ou supérieur
- JDK 17
- Android SDK API 34
- Kotlin 1.9.0

## Installation

1. Clonez le repository
```bash
git clone https://github.com/DaMolks/restaurant-finder-android.git
```

2. Configuration de l'API Google Maps

### Étapes de configuration :

1. Créez un projet dans la Google Cloud Console
2. Activez les API suivantes :
   - Google Maps SDK for Android
   - Places API
   - Geocoding API
   - Geolocation API

3. Générez une clé API avec les restrictions suivantes :
   - Restreindre au package Android : com.example.restaurantfinder
   - Limiter aux API nécessaires

4. Créez ou modifiez le fichier `local.properties` à la racine du projet
```properties
sdk.dir=/path/to/your/Android/Sdk
MAPS_API_KEY=votre_clé_api_ici
```

5. Ouvrez le projet dans Android Studio

6. Synchronisez le projet avec Gradle

7. Lancez l'application

## Résolution des erreurs API

- Erreur 9011 : Vérifiez que toutes les API sont activées et que la clé est correctement configurée
- Vérifiez les restrictions de la clé API dans Google Cloud Console
- Assurez-vous que le package correspond exactement à `com.example.restaurantfinder`

## Dépannage

Si vous rencontrez des problèmes :
- Vérifiez la console Google Cloud
- Régénérez votre clé API
- Assurez-vous que les API requises sont activées