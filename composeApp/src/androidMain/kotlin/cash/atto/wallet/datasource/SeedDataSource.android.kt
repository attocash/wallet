package cash.atto.wallet.datasource

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import cash.atto.wallet.dataStore
import kotlinx.coroutines.flow.map

actual class SeedDataSource(
    private val context: Context
) {
    private val dataStore = context.dataStore
    private val key = stringPreferencesKey(SEED_KEY)

    actual val seed = dataStore.data
        .map { preferences ->
            preferences[key]
        }

    actual suspend fun setSeed(seed: String) {
        dataStore.edit { preferences ->
            preferences[key] = seed
        }
    }

    actual suspend fun clearSeed() {
        dataStore.edit { preferences ->
            preferences.remove(key)
        }
    }

    companion object {
        private const val SEED_KEY = "seed"
    }
}