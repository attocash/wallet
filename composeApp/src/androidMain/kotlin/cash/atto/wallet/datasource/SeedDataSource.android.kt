package cash.atto.wallet.datasource

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import cash.atto.wallet.util.SecurityUtil
import cash.atto.wallet.util.dataStore
import kotlinx.coroutines.flow.map

actual class SeedDataSource(
    context: Context
) {
    private val securityUtil = SecurityUtil()
    private val securityKeyAlias = "data-store"
    private val bytesToStringSeparator = "|"
    private val ivToStringSeparator= ":iv:"

    private val dataStore = context.dataStore
    private val key = stringPreferencesKey(SEED_KEY)

    actual val seed = dataStore.data
        .map { preferences ->
            with (preferences[key] ?: return@map null) {
                val (ivString, valueString) = this.split(ivToStringSeparator, limit = 2)

                return@map securityUtil.decryptData(
                    keyAlias = securityKeyAlias,
                    iv = ivString.split(bytesToStringSeparator)
                        .map { it.toByte() }
                        .toByteArray(),
                    encryptedData = valueString.split(bytesToStringSeparator)
                        .map { it.toByte() }
                        .toByteArray()
                )
            }
        }

    actual suspend fun setSeed(seed: String) {
        val (iv, encryptedValue) = securityUtil.encryptData(securityKeyAlias, seed)
        val ivString = iv.joinToString(bytesToStringSeparator)
        val valueString = encryptedValue.joinToString(bytesToStringSeparator)

        dataStore.edit { preferences ->
            preferences[key] = "$ivString$ivToStringSeparator$valueString"
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