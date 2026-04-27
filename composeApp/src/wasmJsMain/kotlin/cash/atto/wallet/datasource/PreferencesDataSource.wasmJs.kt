package cash.atto.wallet.datasource

import androidx.datastore.core.okio.WebLocalStorage
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.PreferencesSerializer
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.map

actual class PreferencesDataSource {
    private val dataStore =
        PreferenceDataStoreFactory.create(
            storage =
                WebLocalStorage(
                    serializer = PreferencesSerializer,
                    name = STORAGE_NAME,
                ),
        )

    actual val blob =
        dataStore.data.map { preferences ->
            preferences[blobKey]
        }

    actual suspend fun setBlob(blob: String) {
        dataStore.edit { preferences ->
            preferences[blobKey] = blob
        }
    }

    private companion object {
        const val STORAGE_NAME = "user-preferences.preferences_pb"
        val blobKey = stringPreferencesKey("user_preferences_blob")
    }
}
