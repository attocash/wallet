package cash.atto.wallet.datasource

import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlin.io.encoding.Base64

actual class PreferencesDataSource {
    actual val blob = observe(WALLET)
    actual val termsAndConditionsDate = observe(TERMS)
    actual val work = observe(WORK)

    actual suspend fun setBlob(blob: String) = update(WALLET, recordValue(blob))

    actual suspend fun setTermsAndConditionsDate(date: String) = update(TERMS, JsonPrimitive(date))

    actual suspend fun clearTermsAndConditionsDate() = update(TERMS, null)

    actual suspend fun setWork(work: String) = update(WORK, recordValue(work))

    actual suspend fun migrateStorage() {
        if (BrowserStorage.get(LEGACY_KEY) != null) write(read())
    }

    private fun observe(key: String) = BrowserStorage.updates().map { read()[key]?.storedValue() }.distinctUntilChanged()

    private fun update(
        key: String,
        value: JsonElement?,
    ) {
        val values = read().toMutableMap()
        if (value == null) values.remove(key) else values[key] = value
        write(JsonObject(values))
    }

    private fun write(preferences: JsonObject) {
        BrowserStorage.set(STORAGE_KEY, preferences.toString())
        BrowserStorage.remove(LEGACY_KEY)
    }

    private fun read(): JsonObject {
        val stored = BrowserStorage.get(STORAGE_KEY)
        if (stored != null) return Json.parseToJsonElement(stored).jsonObject
        val legacy = BrowserStorage.get(LEGACY_KEY) ?: return JsonObject(emptyMap())
        // DataStore's web format is Base64-encoded JSON with type-prefixed field names.
        val fields = Json.parseToJsonElement(Base64.decode(legacy).decodeToString()).jsonObject
        return JsonObject(
            fields
                .map { (key, value) ->
                    when (key) {
                        "s/user_preferences_blob" -> WALLET to recordValue(value.storedValue())
                        "s/terms_and_conditions_date" -> TERMS to value
                        "s/work" -> WORK to recordValue(value.storedValue())
                        else -> key to value
                    }
                }.toMap(),
        )
    }

    private fun recordValue(value: String): JsonElement =
        try {
            Json.parseToJsonElement(value).takeIf { it is JsonObject } ?: JsonPrimitive(value)
        } catch (_: IllegalArgumentException) {
            // Preserve unreadable or legacy Base64 records verbatim until they can be migrated.
            JsonPrimitive(value)
        }

    private fun JsonElement.storedValue(): String = if (this is JsonPrimitive && isString) content else toString()

    private companion object {
        const val STORAGE_KEY = "preferences"
        const val LEGACY_KEY = "user-preferences.preferences_pb"
        const val WALLET = "wallet"
        const val TERMS = "termsAcceptedAt"
        const val WORK = "work"
    }
}
