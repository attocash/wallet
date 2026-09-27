package cash.atto.wallet.support

import androidx.datastore.preferences.core.PreferencesSerializer
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.browser.localStorage
import okio.Buffer
import kotlin.io.encoding.Base64

internal const val LEGACY_PREFERENCES_KEY = "user-preferences.preferences_pb"

internal suspend fun installLegacyPreferences(
    blob: String?,
    terms: String = "2026-09-14",
    work: String = """{"source":"LOCAL","future":"preserve"}""",
): String {
    val values =
        mutablePreferencesOf(
            stringPreferencesKey("terms_and_conditions_date") to terms,
            stringPreferencesKey("work") to work,
            booleanPreferencesKey("future_setting") to true,
        )
    if (blob != null) values[stringPreferencesKey("user_preferences_blob")] = blob
    val buffer = Buffer()
    PreferencesSerializer.writeTo(values, buffer)
    val encoded = Base64.encode(buffer.readByteArray())
    localStorage.removeItem("preferences")
    localStorage.setItem(LEGACY_PREFERENCES_KEY, encoded)
    return encoded
}
