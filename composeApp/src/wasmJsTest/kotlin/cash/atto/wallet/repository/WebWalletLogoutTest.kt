package cash.atto.wallet.repository

import cash.atto.commons.AttoMnemonic
import cash.atto.wallet.datasource.PasswordDataSource
import cash.atto.wallet.datasource.PreferencesDataSource
import cash.atto.wallet.datasource.SaltDataSource
import cash.atto.wallet.datasource.SeedDataSource
import cash.atto.wallet.datasource.TempSeedDataSource
import cash.atto.wallet.interactor.EncryptedDataPurpose
import cash.atto.wallet.interactor.SeedAESInteractor
import cash.atto.wallet.model.AccountPreferenceStatus
import cash.atto.wallet.model.TermsAndConditions
import cash.atto.wallet.state.AppState.AuthState
import cash.atto.wallet.support.ARGON2_MNEMONIC
import cash.atto.wallet.support.ARGON2_MNEMONIC_RECORD
import cash.atto.wallet.support.LEGACY_PREFERENCES_KEY
import cash.atto.wallet.support.LEGACY_SALT
import cash.atto.wallet.support.encryptLegacy
import cash.atto.wallet.support.failStorageWritesTo
import cash.atto.wallet.support.installLegacyPreferences
import cash.atto.wallet.support.restoreStorageWrites
import kotlinx.browser.localStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WebWalletLogoutTest {
    @Test
    fun `logout removes canonical and legacy wallet data and terms while preserving device settings`() =
        runTest {
            for (legacy in listOf(false, true)) {
                // Given
                installWallet(legacy)
                assertNotNull(storage.blob.first())
                assertTrue(TermsAndConditionsRepository(storage).accepted.first())

                // When
                keyStore().clear()

                // Then
                assertNull(SeedDataSource().getSeed())
                assertNull(storage.blob.first())
                assertFalse(TermsAndConditionsRepository(storage).accepted.first())
                assertEquals(WORK, storage.work.first())
                assertNull(localStorage.getItem("seed"))
                assertNull(localStorage.getItem("mnemonic"))
                assertNull(localStorage.getItem(LEGACY_PREFERENCES_KEY))
                val canonical = Json.parseToJsonElement(assertNotNull(localStorage.getItem("preferences"))).jsonObject
                assertEquals(JsonNull, canonical["mnemonic"])
                if (legacy) {
                    assertEquals(JsonPrimitive(true), canonical["b/future_setting"])
                } else {
                    assertEquals(JsonPrimitive("preserve me"), canonical["unrelated"])
                }

                // An old client restoring an alias must not resurrect the removed wallet.
                localStorage.setItem("seed", ARGON2_MNEMONIC_RECORD)
                assertNull(SeedDataSource().getSeed())
                assertEquals(AuthState.NO_SEED, keyStore().authState())
            }
        }

    @Test
    fun `fresh and reimported wallets can write preferences rotate passwords and reload after logout`() =
        runTest {
            for (reimport in listOf(false, true)) {
                // Given
                installWallet(legacy = true)
                val scope = CoroutineScope(backgroundScope.coroutineContext + Dispatchers.Default)
                val repository = repository(scope)
                repository.deleteKeys()
                val terms = TermsAndConditionsRepository(storage)
                assertFalse(terms.accepted.first())
                if (reimport) repository.importSecret(AttoMnemonic.fromPhrase(ARGON2_MNEMONIC).words) else repository.generateNewSecret()
                repository.savePassword(NEW_PASSWORD)
                val phrase = assertNotNull(repository.state.value.mnemonic)
                val preferences = PreferencesRepository(storage, repository, crypto())

                // When
                preferences.saveAddressLabel("new-address", "Spending")
                preferences.saveHashLabel("new-hash", "Groceries")
                preferences.setAccountStatus(3U, AccountPreferenceStatus.ACTIVATED)
                assertTrue(repository.changePassword(NEW_PASSWORD, ROTATED_PASSWORD))
                repository.lock()
                val restarted = repository(scope)
                assertTrue(restarted.submitPassword(ROTATED_PASSWORD))
                val restored = PreferencesRepository(storage, restarted, crypto())
                val saved = restored.state.first { it.hashLabel("new-hash") == "Groceries" }

                // Then
                assertEquals(phrase, restarted.state.value.mnemonic)
                assertEquals("Spending", saved.addressLabel("new-address"))
                assertEquals(AccountPreferenceStatus.ACTIVATED, saved.accountStatus(3U))
                assertNull(saved.addressLabel("old-address"))
                assertNull(saved.hashLabel("old-hash"))
                assertEquals(AccountPreferenceStatus.DEACTIVATED, saved.accountStatus(2U))
                assertEquals(WORK, storage.work.first())
                assertFalse(terms.accepted.first())
                terms.setCurrentTermsAccepted(true)
                assertTrue(terms.accepted.first())
                restarted.lock()
            }
        }

    @Test
    fun `failed first and final logout writes preserve the old wallet and terms for recovery`() =
        runTest {
            for (legacy in listOf(false, true)) {
                for (write in if (legacy) listOf(1, 2) else listOf(1)) {
                    // Given
                    installWallet(legacy)
                    val seed = SeedDataSource().getSeed()
                    val blob = storage.blob.first()
                    failLogoutWrite(write)
                    try {
                        // When
                        assertFailsWith<WalletStorageException> { keyStore().clear() }

                        // Then
                        assertEquals(seed, SeedDataSource().getSeed())
                        assertEquals(blob, storage.blob.first())
                        assertTrue(TermsAndConditionsRepository(storage).accepted.first())
                        assertEquals(WORK, storage.work.first())
                    } finally {
                        restoreStorageWrites()
                    }
                    assertEquals(ARGON2_MNEMONIC, crypto().decryptSeed(assertNotNull(seed), PASSWORD, EncryptedDataPurpose.SEED)?.plaintext)
                    keyStore().clear()
                    assertNull(SeedDataSource().getSeed())
                    assertNull(storage.blob.first())
                }
            }
        }

    @Test
    fun `failed removal of every legacy alias preserves old credentials preferences and terms`() =
        runTest {
            for (alias in listOf(LEGACY_PREFERENCES_KEY, "seed", "mnemonic")) {
                // Given
                installWallet(legacy = true)
                val seed = SeedDataSource().getSeed()
                val blob = storage.blob.first()
                failLogoutRemoval(alias)
                try {
                    // When
                    assertFailsWith<WalletStorageException> { keyStore().clear() }

                    // Then
                    assertEquals(seed, SeedDataSource().getSeed())
                    assertEquals(blob, storage.blob.first())
                    assertTrue(TermsAndConditionsRepository(storage).accepted.first())
                    assertNotNull(localStorage.getItem(alias))
                    assertEquals(WORK, storage.work.first())
                } finally {
                    restoreLogoutRemovals()
                }
                assertEquals(ARGON2_MNEMONIC, crypto().decryptSeed(assertNotNull(seed), PASSWORD, EncryptedDataPurpose.SEED)?.plaintext)
                keyStore().clear()
                assertNull(SeedDataSource().getSeed())
            }
        }

    @Test
    fun `prepared writes and old unlocked sessions cannot recreate preferences after logout or replacement`() =
        runTest {
            // Given
            installWallet(legacy = false)
            val scope = CoroutineScope(backgroundScope.coroutineContext + Dispatchers.Default)
            val stale = repository(scope)
            assertTrue(stale.submitPassword(PASSWORD))
            val oldPreferences = PreferencesRepository(storage, stale, crypto())
            val snapshot = storage.walletSnapshot()
            val commit = keyStore().preparePasswordChange(AttoMnemonic.fromPhrase(ARGON2_MNEMONIC), PASSWORD, NEW_PASSWORD)
            keyStore().clear()
            val deleted = localStorage.getItem("preferences")

            // When / Then
            assertFailsWith<WalletStorageException> { storage.setWalletBlob(assertNotNull(snapshot.blob), snapshot) }
            assertFailsWith<WalletStorageException> { commit() }
            assertFailsWith<WalletStorageException> { oldPreferences.saveAddressLabel("stale-address", "Stale") }
            assertEquals(deleted, localStorage.getItem("preferences"))
            val replacement = AttoMnemonic.fromEntropy(ByteArray(AttoMnemonic.ENTROPY_SIZE) { 1 })
            keyStore().savePassword(replacement.phrase, NEW_PASSWORD)
            assertNull(storage.blob.first())
            val replaced = localStorage.getItem("preferences")
            assertFailsWith<WalletStorageException> { oldPreferences.saveHashLabel("stale-hash", "Stale") }
            assertEquals(replaced, localStorage.getItem("preferences"))
            assertNull(storage.blob.first())
            assertEquals(replacement, keyStore().unlock(NEW_PASSWORD))
            stale.lock()
        }

    @Test
    fun `failed logout revokes its session while keeping persisted wallet available`() =
        runTest {
            // Given
            installWallet(legacy = false)
            val repository = repository(CoroutineScope(backgroundScope.coroutineContext + Dispatchers.Default))
            assertTrue(repository.submitPassword(PASSWORD))
            val session = repository.state.value
            val seed = SeedDataSource().getSeed()
            val blob = storage.blob.first()
            failStorageWritesTo("preferences")
            try {
                // When
                assertFailsWith<WalletStorageException> { repository.deleteKeys() }

                // Then
                assertEquals(AuthState.SESSION_INVALID, repository.state.value.authState)
                assertNull(session.mnemonic)
                assertNull(session.password)
                assertEquals(seed, SeedDataSource().getSeed())
                assertEquals(blob, storage.blob.first())
                assertTrue(TermsAndConditionsRepository(storage).accepted.first())
            } finally {
                restoreStorageWrites()
            }
            assertTrue(repository.submitPassword(PASSWORD))
            repository.lock()
        }

    private suspend fun installWallet(legacy: Boolean) {
        restoreStorageWrites()
        restoreLogoutRemovals()
        localStorage.clear()
        localStorage.setItem("salt", LEGACY_SALT)
        if (legacy) {
            val seed = encryptLegacy(ARGON2_MNEMONIC, PASSWORD)
            localStorage.setItem("seed", seed)
            localStorage.setItem("mnemonic", seed)
            installLegacyPreferences(encryptLegacy(PREFERENCES, PASSWORD), terms = TermsAndConditions.EFFECTIVE_DATE, work = WORK)
        } else {
            val fields = mutableMapOf(
                "mnemonic" to Json.parseToJsonElement(ARGON2_MNEMONIC_RECORD),
                "wallet" to Json.parseToJsonElement(crypto().encryptSeed(PREFERENCES, PASSWORD, EncryptedDataPurpose.PREFERENCES)),
                "termsAcceptedAt" to JsonPrimitive(TermsAndConditions.EFFECTIVE_DATE),
                "work" to Json.parseToJsonElement(WORK),
                "unrelated" to JsonPrimitive("preserve me"),
            )
            localStorage.setItem("preferences", JsonObject(fields).toString())
        }
    }

    private fun keyStore() = PlatformWalletKeyStore(SeedDataSource(), PasswordDataSource(), crypto(), storage)

    private fun repository(scope: CoroutineScope) = AppStateRepository(keyStore(), TempSeedDataSource(), scope)

    private fun crypto() = SeedAESInteractor(SaltDataSource())

    private companion object {
        val storage = PreferencesDataSource()
        const val PASSWORD = "Synthetic7!"
        const val NEW_PASSWORD = "fK7q9Vm2xT6n4Rb8"
        const val ROTATED_PASSWORD = "hN4y8Wp3cS9k6Jz2"
        const val WORK = """{"source":"LOCAL"}"""
        const val PREFERENCES = """{"addresses":[{"value":"old-address","label":"Savings"}],"hashes":[{"value":"old-hash","label":"Rent"}],"accounts":{"0":{"status":"ACTIVATED"},"2":{"status":"ACTIVATED"}}}"""
    }
}

private fun failLogoutWrite(failureNumber: Int): Unit =
    js(
        """
        {
            const original = Storage.prototype.setItem;
            let count = 0;
            window.__walletTestRestoreStorage = () => { Storage.prototype.setItem = original; };
            Storage.prototype.setItem = function(name, value) {
                if (name === 'preferences' && ++count === failureNumber) throw new DOMException('Synthetic storage failure', 'QuotaExceededError');
                return original.call(this, name, value);
            };
        }
        """,
    )

private fun failLogoutRemoval(alias: String): Unit =
    js(
        """
        {
            const original = Storage.prototype.removeItem;
            window.__walletLogoutRestoreRemoval = () => { Storage.prototype.removeItem = original; };
            Storage.prototype.removeItem = function(name) {
                if (name === alias) throw new DOMException('Synthetic removal failure', 'SecurityError');
                return original.call(this, name);
            };
        }
        """,
    )

private fun restoreLogoutRemovals(): Unit =
    js(
        """
        {
            if (window.__walletLogoutRestoreRemoval) {
                window.__walletLogoutRestoreRemoval();
                delete window.__walletLogoutRestoreRemoval;
            }
        }
        """,
    )
