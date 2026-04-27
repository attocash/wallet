package cash.atto.wallet.datasource

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath
import java.io.File

actual class PreferencesDataSource {
    private val dataStore =
        PreferenceDataStoreFactory.createWithPath(
            produceFile = {
                val homeDir = System.getProperty("user.home")
                val preferencesFile = File(homeDir, ".atto/user-preferences.preferences_pb")
                preferencesFile.parentFile?.mkdirs()
                preferencesFile.absolutePath.toPath()
            },
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
        val blobKey = stringPreferencesKey("user_preferences_blob")
    }
}
