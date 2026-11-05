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
import cash.atto.wallet.support.ARGON2_MNEMONIC
import cash.atto.wallet.support.ARGON2_MNEMONIC_RECORD
import cash.atto.wallet.support.ARGON2_PREFERENCES_RECORD
import cash.atto.wallet.support.failStorageWritesTo
import cash.atto.wallet.support.restoreStorageWrites
import kotlinx.browser.localStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WebPasswordRotationTest {
    @Test
    fun `rotation atomically preserves the phrase labels accounts and public settings across restart`() =
        runTest {
            // Given
            installWallet()
            val scope = CoroutineScope(backgroundScope.coroutineContext + Dispatchers.Default)
            val repository = repository(scope)
            val crypto = SeedAESInteractor(SaltDataSource())
            val preferences = PreferencesRepository(storage, repository, crypto)
            assertTrue(repository.submitPassword(PASSWORD))
            preferences.state.first { it.addressLabel("synthetic-address") == "Savings" }
            preferences.setAccountStatus(2U, AccountPreferenceStatus.ACTIVATED)
            val before = preferences.state.value
            val plaintext = crypto.decryptSeed(assertNotNull(storage.blob.first()), PASSWORD, EncryptedDataPurpose.PREFERENCES)?.plaintext
            var emptied = false
            val observation = backgroundScope.launch(Dispatchers.Unconfined) {
                preferences.state.collect { if (it.addressLabel("synthetic-address") == null) emptied = true }
            }

            // When
            assertTrue(repository.changePassword(PASSWORD, NEW_PASSWORD))
            val canonical = Json.parseToJsonElement(assertNotNull(localStorage.getItem("preferences"))).jsonObject
            val encryptedSeed = assertNotNull(SeedDataSource().getSeed())
            val encryptedPreferences = assertNotNull(storage.blob.first())

            // Then
            assertTrue("mnemonic" in canonical)
            assertNull(localStorage.getItem("mnemonic"))
            assertNull(localStorage.getItem("seed"))
            assertEquals("2026-09-14", storage.termsAndConditionsDate.first())
            assertEquals("{\"source\":\"LOCAL\"}", storage.work.first())
            assertEquals(ARGON2_MNEMONIC, crypto.decryptSeed(encryptedSeed, NEW_PASSWORD, EncryptedDataPurpose.SEED)?.plaintext)
            assertEquals(plaintext, crypto.decryptSeed(encryptedPreferences, NEW_PASSWORD, EncryptedDataPurpose.PREFERENCES)?.plaintext)
            assertNull(crypto.decryptSeed(encryptedSeed, PASSWORD, EncryptedDataPurpose.SEED))
            assertNull(crypto.decryptSeed(encryptedPreferences, PASSWORD, EncryptedDataPurpose.PREFERENCES))
            preferences.state.first { it == before }
            assertFalse(emptied)
            observation.cancel()
            repository.lock()
            val restarted = repository(scope)
            assertFalse(restarted.submitPassword(PASSWORD))
            assertTrue(restarted.submitPassword(NEW_PASSWORD))
            assertEquals(AttoMnemonic.fromPhrase(ARGON2_MNEMONIC), restarted.state.value.mnemonic)
            val restored = PreferencesRepository(storage, restarted, crypto)
            assertEquals(before, restored.state.first { it.accountStatus(2U) == AccountPreferenceStatus.ACTIVATED })
            restarted.lock()
        }

    @Test
    fun `failed atomic rotation leaves both records and the current session usable`() =
        runTest {
            // Given
            installWallet()
            val scope = CoroutineScope(backgroundScope.coroutineContext + Dispatchers.Default)
            val repository = repository(scope)
            assertTrue(repository.submitPassword(PASSWORD))
            val seed = SeedDataSource().getSeed()
            val preferences = localStorage.getItem("preferences")
            val session = repository.state.value.unlockedWallet
            failStorageWritesTo("preferences")
            try {
                // When
                assertFailsWith<WalletStorageException> { repository.changePassword(PASSWORD, NEW_PASSWORD) }

                // Then
                assertTrue(session === repository.state.value.unlockedWallet)
                assertEquals(PASSWORD, repository.state.value.password)
                assertEquals(seed, SeedDataSource().getSeed())
                assertEquals(preferences, localStorage.getItem("preferences"))
            } finally {
                restoreStorageWrites()
            }
            repository.lock()
            val restarted = repository(scope)
            assertFalse(restarted.submitPassword(NEW_PASSWORD))
            assertTrue(restarted.submitPassword(PASSWORD))
            assertTrue(restarted.changePassword(PASSWORD, NEW_PASSWORD))
            restarted.lock()
        }

    @Test
    fun `concurrent preference writes and rotation preserve all changes under the new password`() =
        runTest {
            // Given
            installWallet()
            val scope = CoroutineScope(backgroundScope.coroutineContext + Dispatchers.Default)
            val repository = repository(scope)
            assertTrue(repository.submitPassword(PASSWORD))
            val crypto = SeedAESInteractor(SaltDataSource())
            val preferences = PreferencesRepository(storage, repository, crypto)
            preferences.state.first { it.addressLabel("synthetic-address") == "Savings" }

            // When
            val rotation = async { repository.changePassword(PASSWORD, NEW_PASSWORD) }
            val address = async { preferences.saveAddressLabel("second-address", "Spending") }
            val hash = async { preferences.saveHashLabel("synthetic-hash", "Rent") }
            assertTrue(rotation.await())
            address.await()
            hash.await()

            // Then
            repository.lock()
            val restarted = repository(scope)
            assertTrue(restarted.submitPassword(NEW_PASSWORD))
            val restored = PreferencesRepository(storage, restarted, crypto)
            val saved = restored.state.first { it.hashLabel("synthetic-hash") == "Rent" }
            assertEquals("Savings", saved.addressLabel("synthetic-address"))
            assertEquals("Spending", saved.addressLabel("second-address"))
            restarted.lock()
        }

    @Test
    fun `a stale unlocked session cannot overwrite preferences or rotate the newer wallet`() =
        runTest {
            // Given
            installWallet()
            val scope = CoroutineScope(backgroundScope.coroutineContext + Dispatchers.Default)
            val stale = repository(scope)
            val current = repository(scope)
            assertTrue(stale.submitPassword(PASSWORD))
            assertTrue(current.submitPassword(PASSWORD))
            val stalePreferences = PreferencesRepository(storage, stale, SeedAESInteractor(SaltDataSource()))
            assertTrue(current.changePassword(PASSWORD, NEW_PASSWORD))
            val committed = localStorage.getItem("preferences")

            // When
            assertFailsWith<WalletStorageException> { stalePreferences.saveAddressLabel("stale-address", "Stale") }
            assertFailsWith<WalletStorageException> { stale.changePassword(PASSWORD, "hN4y8Wp3cS9k6Jz2") }

            // Then
            assertEquals(committed, localStorage.getItem("preferences"))
            stale.lock()
            current.lock()
            assertTrue(repository(scope).submitPassword(NEW_PASSWORD))
        }

    @Test
    fun `prepared rotation and preference writes reject a changed storage snapshot`() =
        runTest {
            // Given
            installWallet()
            val keyStore = PlatformWalletKeyStore(SeedDataSource(), PasswordDataSource(), SeedAESInteractor(SaltDataSource()), storage)
            val mnemonic = AttoMnemonic.fromPhrase(ARGON2_MNEMONIC)
            val snapshot = storage.walletSnapshot()
            val commit = keyStore.preparePasswordChange(mnemonic, PASSWORD, NEW_PASSWORD)
            val crypto = SeedAESInteractor(SaltDataSource())
            val updated = crypto.encryptSeed("{\"addresses\":[]}", PASSWORD, EncryptedDataPurpose.PREFERENCES)
            storage.setBlob(updated)
            val committed = localStorage.getItem("preferences")

            // When
            assertFailsWith<WalletStorageException> { commit() }
            assertFailsWith<WalletStorageException> { storage.setWalletBlob(ARGON2_PREFERENCES_RECORD, snapshot) }

            // Then
            assertEquals(committed, localStorage.getItem("preferences"))
            assertEquals(mnemonic, keyStore.unlock(PASSWORD))
        }

    @Test
    fun `failure after canonicalizing old credentials keeps the old password recoverable`() =
        runTest {
            // Given
            installWallet()
            val scope = CoroutineScope(backgroundScope.coroutineContext + Dispatchers.Default)
            val repository = repository(scope)
            assertTrue(repository.submitPassword(PASSWORD))
            val oldSeed = Json.parseToJsonElement(assertNotNull(SeedDataSource().getSeed()))
            val oldPreferences = storage.blob.first()
            failSecondPreferencesWrite()
            try {
                // When
                assertFailsWith<WalletStorageException> { repository.changePassword(PASSWORD, NEW_PASSWORD) }

                // Then
                assertEquals(oldSeed, Json.parseToJsonElement(assertNotNull(SeedDataSource().getSeed())))
                assertEquals(oldPreferences, storage.blob.first())
                assertEquals(PASSWORD, repository.state.value.password)
            } finally {
                restoreStorageWrites()
            }
            repository.lock()
            val restarted = repository(scope)
            assertFalse(restarted.submitPassword(NEW_PASSWORD))
            assertTrue(restarted.submitPassword(PASSWORD))
            restarted.lock()
        }

    @Test
    fun `failed legacy alias removal aborts password rotation while retaining old credentials`() =
        runTest {
            // Given
            installWallet()
            val scope = CoroutineScope(backgroundScope.coroutineContext + Dispatchers.Default)
            val repository = repository(scope)
            assertTrue(repository.submitPassword(PASSWORD))
            val oldSeed = Json.parseToJsonElement(assertNotNull(SeedDataSource().getSeed()))
            failMnemonicRemoval()
            try {
                // When
                assertFailsWith<WalletStorageException> { repository.changePassword(PASSWORD, NEW_PASSWORD) }

                // Then
                assertEquals(oldSeed, Json.parseToJsonElement(assertNotNull(SeedDataSource().getSeed())))
                assertEquals(PASSWORD, repository.state.value.password)
                assertNotNull(localStorage.getItem("mnemonic"))
            } finally {
                restoreStorageRemovals()
            }
            repository.lock()
            assertTrue(repository.submitPassword(PASSWORD))
            assertTrue(repository.changePassword(PASSWORD, NEW_PASSWORD))
            assertNull(localStorage.getItem("mnemonic"))
            repository.lock()
        }

    private suspend fun installWallet() {
        restoreStorageWrites()
        localStorage.clear()
        SeedDataSource().setSeed(ARGON2_MNEMONIC_RECORD)
        storage.setBlob(ARGON2_PREFERENCES_RECORD)
        storage.setTermsAndConditionsDate("2026-09-14")
        storage.setWork("{\"source\":\"LOCAL\"}")
    }

    private fun repository(scope: CoroutineScope) =
        AppStateRepository(
            PlatformWalletKeyStore(SeedDataSource(), PasswordDataSource(), SeedAESInteractor(SaltDataSource()), storage),
            TempSeedDataSource(),
            scope,
        )

    private companion object {
        val storage = PreferencesDataSource()
        const val PASSWORD = "Synthetic7!"
        const val NEW_PASSWORD = "fK7q9Vm2xT6n4Rb8"
    }
}

private fun failSecondPreferencesWrite(): Unit =
    js(
        """
        {
            const original = Storage.prototype.setItem;
            let count = 0;
            window.__walletTestRestoreStorage = () => { Storage.prototype.setItem = original; };
            Storage.prototype.setItem = function(name, value) {
                if (name === 'preferences' && ++count === 2) throw new DOMException('Synthetic storage failure', 'QuotaExceededError');
                return original.call(this, name, value);
            };
        }
        """,
    )

private fun failMnemonicRemoval(): Unit =
    js(
        """
        {
            const original = Storage.prototype.removeItem;
            window.__walletTestRestoreRemoval = () => { Storage.prototype.removeItem = original; };
            Storage.prototype.removeItem = function(name) {
                if (name === 'mnemonic') throw new DOMException('Synthetic removal failure', 'SecurityError');
                return original.call(this, name);
            };
        }
        """,
    )

private fun restoreStorageRemovals(): Unit =
    js(
        """
        {
            if (window.__walletTestRestoreRemoval) {
                window.__walletTestRestoreRemoval();
                delete window.__walletTestRestoreRemoval;
            }
        }
        """,
    )
