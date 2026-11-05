package cash.atto.wallet.datasource

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath

actual class PreferencesDataSource(
    context: Context,
) {
    private val dataStore =
        PreferenceDataStoreFactory.createWithPath(
            produceFile = {
                context.filesDir
                    .resolve("user-preferences.preferences_pb")
                    .absolutePath
                    .toPath()
            },
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

    internal actual suspend fun walletSnapshot() = WalletStorageSnapshot(null, blob.first())

    internal actual suspend fun setWalletBlob(blob: String, expected: WalletStorageSnapshot) = setBlob(blob)

    internal actual suspend fun migrateEncryptedWallet(seed: String, blob: String?, expected: WalletStorageSnapshot): Unit =
        error("Encrypted wallet storage is only used on web")

    internal actual suspend fun replaceEncryptedWallet(seed: String, blob: String, expected: WalletStorageSnapshot): Unit =
        error("Encrypted wallet storage is only used on web")

    actual suspend fun migrateStorage() = Unit

    private companion object {
        val blobKey = stringPreferencesKey("user_preferences_blob")
        val termsAndConditionsDateKey = stringPreferencesKey("terms_and_conditions_date")
        val workKey = stringPreferencesKey("work")
    }
}
