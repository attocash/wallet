package cash.atto.wallet.datasource

import cash.atto.wallet.repository.WalletStorageException
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlin.io.encoding.Base64

actual class PreferencesDataSource {
    actual val blob = observe(WALLET)
    actual val termsAndConditionsDate = observe(TERMS)
    actual val work = observe(WORK)

    actual suspend fun setBlob(blob: String) = BrowserStorage.withWalletLock { update(WALLET, recordValue(blob)) }

    actual suspend fun setTermsAndConditionsDate(date: String) = BrowserStorage.withWalletLock { update(TERMS, JsonPrimitive(date)) }

    actual suspend fun clearTermsAndConditionsDate() = BrowserStorage.withWalletLock { update(TERMS, null) }

    actual suspend fun setWork(work: String) = BrowserStorage.withWalletLock { update(WORK, recordValue(work)) }

    internal actual suspend fun walletSnapshot(): WalletStorageSnapshot = snapshot()

    internal actual suspend fun setWalletBlob(blob: String, expected: WalletStorageSnapshot) =
        BrowserStorage.withWalletLock {
            requireCurrent(expected)
            update(WALLET, recordValue(blob))
        }

    internal actual suspend fun migrateEncryptedWallet(seed: String, blob: String?, expected: WalletStorageSnapshot) =
        BrowserStorage.withWalletLock {
            requireCurrent(expected)
            if (blob != expected.blob && blob != null) update(WALLET, recordValue(blob))
            migrateLegacyStorage()
            setMnemonicWithoutLock(seed)
        }

    internal actual suspend fun replaceEncryptedWallet(seed: String, blob: String, expected: WalletStorageSnapshot) =
        BrowserStorage.withWalletLock {
            requireCurrent(expected)
            val original = read().toMutableMap()
            if (MNEMONIC !in original) {
                // Preserve the old credentials before requiring removal of their standalone aliases.
                original[MNEMONIC] = expected.seed?.let(::recordValue) ?: JsonNull
                BrowserStorage.set(STORAGE_KEY, JsonObject(original).toString())
            }
            for (key in listOf(LEGACY_KEY, "seed", "mnemonic")) {
                if (BrowserStorage.get(key) != null) BrowserStorage.remove(key)
            }
            original[MNEMONIC] = recordValue(seed)
            original[WALLET] = recordValue(blob)
            // No fallible cleanup follows the one replacement that changes the password.
            BrowserStorage.set(STORAGE_KEY, JsonObject(original).toString())
        }

    internal fun containsMnemonic(): Boolean = MNEMONIC in read()

    internal fun getMnemonic(): String? = read()[MNEMONIC]?.takeUnless { it == JsonNull }?.storedValue()

    internal fun setMnemonicWithoutLock(seed: String?) {
        if (containsMnemonic()) {
            update(MNEMONIC, seed?.let(::recordValue) ?: JsonNull)
        } else if (seed != null) {
            BrowserStorage.set("mnemonic", seed)
        }
        BrowserStorage.remove("seed")
    }

    internal fun clearMnemonicWithoutLock() {
        if (containsMnemonic()) update(MNEMONIC, JsonNull)
        BrowserStorage.remove("seed")
        BrowserStorage.remove("mnemonic")
    }

    actual suspend fun migrateStorage() = BrowserStorage.withWalletLock { migrateLegacyStorage() }

    private fun migrateLegacyStorage() {
        if (BrowserStorage.get(LEGACY_KEY) != null) write(read())
    }

    private fun snapshot(): WalletStorageSnapshot {
        val values = read()
        val seed =
            if (MNEMONIC in values) {
                values[MNEMONIC]?.takeUnless { it == JsonNull }?.storedValue()
            } else {
                BrowserStorage.get("mnemonic") ?: BrowserStorage.get("seed")
            }
        return WalletStorageSnapshot(seed, values[WALLET]?.storedValue())
    }

    private fun requireCurrent(expected: WalletStorageSnapshot) {
        if (snapshot() != expected) throw WalletStorageException("Wallet data changed in another tab. Lock and unlock this wallet, then try again.")
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
        const val MNEMONIC = "mnemonic"
        const val TERMS = "termsAcceptedAt"
        const val WORK = "work"
    }
}
