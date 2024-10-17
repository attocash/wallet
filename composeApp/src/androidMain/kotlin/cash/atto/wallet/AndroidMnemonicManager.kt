package cash.atto.wallet

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import cash.atto.commons.AttoMnemonic
import cash.atto.wallet.util.dataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class AndroidMnemonicManager(private val context: Context) : MnemonicManager {
    private val WORDS_KEY = stringPreferencesKey("mnemonic")


     override suspend fun save(mnemonic: AttoMnemonic) {
        val words = mnemonic.words.joinToString(" ")
        val encryptedWords = AndroidEncrypter.encrypt(words)
        context.dataStore.edit { preferences ->
            preferences[WORDS_KEY] = encryptedWords
        }
    }

     override suspend fun find(): AttoMnemonic? {
        val encryptedWords = context.dataStore.data.map { preferences ->
            preferences[WORDS_KEY]
        }.firstOrNull()

        return encryptedWords?.let {
            val decryptedWords = AndroidEncrypter.decrypt(it)
            AttoMnemonic(decryptedWords.split(" "))
        }
    }
}

@Composable
actual fun mnemonicManager(): MnemonicManager {
    return AndroidMnemonicManager(context = LocalContext.current)
}
