package cash.atto.wallet.datasource

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import cash.atto.wallet.util.SecurityUtil
import cash.atto.wallet.util.dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

actual class PasswordDataSource(
    context: Context
) {
    private val securityUtil = SecurityUtil()
    private val securityKeyAlias = "password-store"
    private val bytesToStringSeparator = "|"
    private val ivToStringSeparator= ":iv:"

    private val dataStore = context.dataStore

    private val dataSourceScope = CoroutineScope(Dispatchers.IO)
    private var dataSourceJob: Job? = null

    actual suspend fun getPassword(seed: String): String? {
        val key = stringPreferencesKey("$PASSWORD_KEY${encryptSeed(seed)}")
        val passwordFlow = dataStore.data
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

        val passwordChannel = Channel<String?>()

        dataSourceJob?.cancel()
        dataSourceJob = dataSourceScope.launch {
            passwordFlow.collect {
                passwordChannel.send(it)
            }
        }

        return passwordChannel.receive()
    }

    actual suspend fun setPassword(seed: String, password: String) {
        val key = stringPreferencesKey("$PASSWORD_KEY${encryptSeed(seed)}")
        val (iv, encryptedValue) = securityUtil.encryptData(securityKeyAlias, password)
        val ivString = iv.joinToString(bytesToStringSeparator)
        val valueString = encryptedValue.joinToString(bytesToStringSeparator)

        dataStore.edit { preferences ->
            preferences[key] = "$ivString$ivToStringSeparator$valueString"
        }
    }

    private fun encryptSeed(seed: String): String {
        return seed.hashCode().toString()
    }

    companion object {
        private const val PASSWORD_KEY = "password"
    }
}