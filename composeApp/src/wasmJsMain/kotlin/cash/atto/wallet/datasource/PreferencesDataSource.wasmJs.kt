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

    actual val termsAndConditionsDate =
        dataStore.data.map { preferences ->
            preferences[termsAndConditionsDateKey]
        }

    actual val work =
        dataStore.data.map { preferences ->
            preferences[workKey]
        }

    actual suspend fun setBlob(blob: String) {
        dataStore.edit { preferences ->
            preferences[blobKey] = blob
        }
    }

    actual suspend fun setTermsAndConditionsDate(date: String) {
        dataStore.edit { preferences ->
            preferences[termsAndConditionsDateKey] = date
        }
    }

    actual suspend fun clearTermsAndConditionsDate() {
        dataStore.edit { preferences ->
            preferences.remove(termsAndConditionsDateKey)
        }
    }

    actual suspend fun setWork(work: String) {
        dataStore.edit { preferences ->
            preferences[workKey] = work
        }
    }

    private companion object {
        const val STORAGE_NAME = "user-preferences.preferences_pb"
        val blobKey = stringPreferencesKey("user_preferences_blob")
        val termsAndConditionsDateKey = stringPreferencesKey("terms_and_conditions_date")
        val workKey = stringPreferencesKey("work")
    }
}
