package cash.atto.wallet

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

internal val Context.dataStore by preferencesDataStore(name = "atto-wallet")