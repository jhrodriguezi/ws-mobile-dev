package lab.jhrodriguezi.nearbyspots.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferencesRepository constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val SEARCH_RADIUS_KEY = floatPreferencesKey("search_radius")
    }

    fun getSearchRadius(): Flow<Float> = dataStore.data
        .map { preferences ->
            preferences[SEARCH_RADIUS_KEY] ?: 5f
        }

    suspend fun saveSearchRadius(radius: Float) {
        dataStore.edit { preferences ->
            preferences[SEARCH_RADIUS_KEY] = radius
        }
    }
}