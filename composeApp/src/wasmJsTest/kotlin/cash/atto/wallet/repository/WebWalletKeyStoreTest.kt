package cash.atto.wallet.repository

import cash.atto.commons.AttoMnemonic
import cash.atto.wallet.datasource.PasswordDataSource
import cash.atto.wallet.datasource.PreferencesDataSource
import cash.atto.wallet.datasource.SaltDataSource
import cash.atto.wallet.datasource.SeedDataSource
import cash.atto.wallet.datasource.TempSeedDataSource
import cash.atto.wallet.interactor.CheckPasswordInteractor
import cash.atto.wallet.interactor.EncryptedDataPurpose
import cash.atto.wallet.interactor.SeedAESInteractor
import cash.atto.wallet.model.TermsAndConditions
import cash.atto.wallet.state.AppState.AuthState
import cash.atto.wallet.support.ARGON2_MNEMONIC
import cash.atto.wallet.support.ARGON2_MNEMONIC_RECORD
import cash.atto.wallet.support.ARGON2_PREFERENCES_RECORD
import cash.atto.wallet.support.LEGACY_PREFERENCES_KEY
import cash.atto.wallet.support.LEGACY_SALT
import cash.atto.wallet.support.decryptEnvelopeIndependently
import cash.atto.wallet.support.encryptLegacy
import cash.atto.wallet.support.encryptPbkdf2Envelope
import cash.atto.wallet.support.failStorageWritesTo
import cash.atto.wallet.support.installLegacyPreferences
import cash.atto.wallet.support.observeArgon2Workers
import cash.atto.wallet.support.restoreArgon2Workers
import cash.atto.wallet.support.restoreStorageWrites
import cash.atto.wallet.viewmodel.AppViewModel
import cash.atto.wallet.viewmodel.CreatePasswordViewModel
import kotlinx.browser.localStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WebWalletKeyStoreTest {
    @Test
    fun `unlocking records with different persisted parameters does not write either record`() =
        runTest {
            // Given
            localStorage.clear()
            val mnemonic = AttoMnemonic.fromPhrase(ARGON2_MNEMONIC)
            SeedDataSource().setSeed(ARGON2_MNEMONIC_RECORD)
            preferencesStorage.setBlob(ARGON2_PREFERENCES_RECORD)
            val preferences = localStorage.getItem("preferences")

            for (key in listOf("mnemonic", "preferences")) {
                failStorageWritesTo(key)
                try {
                    // When: a fresh key store simulates repeated unlocks after restarting the app.
                    val unlocked = keyStore().unlock(PASSWORD)

                    // Then: unlock succeeds even when any write to this record would fail.
                    assertEquals(mnemonic, unlocked)
                    assertEquals(ARGON2_MNEMONIC_RECORD, localStorage.getItem("mnemonic"))
                    assertEquals(preferences, localStorage.getItem("preferences"))
                } finally {
                    restoreStorageWrites()
                }
            }
        }

    @Test
    fun `web wallet can unlock from encrypted storage after clearing session memory`() =
        runTest {
            // Given
            val seedStorage = SeedDataSource()
            seedStorage.clearSeed()
            preferencesStorage.setBlob("")
            val keyStore =
                PlatformWalletKeyStore(seedStorage, PasswordDataSource(), SeedAESInteractor(SaltDataSource()), preferencesStorage)
            val temporary = TempSeedDataSource()
            val repository = AppStateRepository(keyStore, temporary, backgroundScope)
            val mnemonic = AttoMnemonic.fromEntropy(ByteArray(33))
            val password = "Synthetic7!"
            try {
                repository.importSecret(mnemonic.words)
                repository.savePassword(password)
                val encryptedSeed = assertNotNull(seedStorage.getSeed())
                assertNotEquals(mnemonic.phrase, encryptedSeed)

                // When
                repository.lock()
                val restarted = AppStateRepository(keyStore, TempSeedDataSource(), backgroundScope)

                // Then
                assertNull(temporary.seed)
                assertNull(repository.state.value.mnemonic)
                assertFalse(restarted.submitPassword("WrongPassword7!"))
                assertTrue(restarted.submitPassword(password))
                assertEquals(mnemonic, restarted.state.value.mnemonic)
                restarted.deleteKeys()
                assertEquals(AuthState.NO_SEED, restarted.state.value.authState)
                assertNull(seedStorage.getSeed())
                assertFalse(restarted.submitPassword(password))
            } finally {
                repository.lock()
                seedStorage.clearSeed()
            }
        }

    @Test
    fun `wrong password does not change either legacy record or the legacy salt`() =
        runTest {
            // Given
            installLegacyWallet()
            val seed = SeedDataSource().getSeed()
            val preferences = preferencesStorage.blob.first()

            // When
            val unlocked = keyStore().unlock("WrongPassword7!")

            // Then
            assertNull(unlocked)
            assertEquals(seed, SeedDataSource().getSeed())
            assertEquals(preferences, preferencesStorage.blob.first())
            assertEquals(LEGACY_SALT, localStorage.getItem("salt"))
        }

    @Test
    fun `successful unlock migrates both legacy records once and preserves all plaintext`() =
        runTest {
            // Given
            val mnemonic = installLegacyWallet()

            // When
            assertEquals(mnemonic, keyStore().unlock(PASSWORD))
            val seed = assertNotNull(SeedDataSource().getSeed())
            val preferences = assertNotNull(preferencesStorage.blob.first())

            // Then
            assertTrue(seed.startsWith("{"))
            assertTrue(preferences.startsWith("{"))
            assertEquals(mnemonic.phrase, decryptEnvelopeIndependently(seed, PASSWORD))
            assertEquals(PREFERENCES, decryptEnvelopeIndependently(preferences, PASSWORD))
            assertEquals(mnemonic, keyStore().unlock(PASSWORD))
            assertEquals(seed, SeedDataSource().getSeed())
            assertEquals(preferences, preferencesStorage.blob.first())
        }

    @Test
    fun `failure of the first migration write preserves both legacy records and can retry`() =
        runTest {
            // Given
            val mnemonic = installLegacyWallet()
            val seed = SeedDataSource().getSeed()
            val preferences = preferencesStorage.blob.first()
            failStorageWritesTo("preferences")
            try {
                // When
                assertFailsWith<WalletStorageException> { keyStore().unlock(PASSWORD) }

                // Then
                assertEquals(seed, SeedDataSource().getSeed())
                assertEquals(preferences, preferencesStorage.blob.first())
            } finally {
                restoreStorageWrites()
            }
            assertEquals(mnemonic, keyStore().unlock(PASSWORD))
        }

    @Test
    fun `interrupted migration resumes with a legacy seed and already upgraded preferences`() =
        runTest {
            // Given
            val mnemonic = installLegacyWallet()
            val seed = SeedDataSource().getSeed()
            failStorageWritesTo("mnemonic")
            try {
                // When
                assertFailsWith<WalletStorageException> { keyStore().unlock(PASSWORD) }

                // Then
                assertEquals(seed, SeedDataSource().getSeed())
                assertTrue(assertNotNull(preferencesStorage.blob.first()).startsWith("{"))
            } finally {
                restoreStorageWrites()
            }
            val preferences = preferencesStorage.blob.first()
            assertEquals(mnemonic, keyStore().unlock(PASSWORD))
            assertEquals(preferences, preferencesStorage.blob.first())
            assertEquals(mnemonic.phrase, decryptEnvelopeIndependently(assertNotNull(SeedDataSource().getSeed()), PASSWORD))
        }

    @Test
    fun `migration storage failure produces a login error without unlocking`() =
        runTest {
            // Given
            installLegacyWallet()
            val seed = SeedDataSource().getSeed()
            val repository = AppStateRepository(keyStore(), TempSeedDataSource(), backgroundScope)
            val viewModel = AppViewModel(repository, CheckPasswordInteractor(), TermsAndConditionsRepository(preferencesStorage))
            failStorageWritesTo("mnemonic")
            try {
                // When
                val accepted = viewModel.enterPassword(PASSWORD)

                // Then
                assertFalse(accepted)
                val state = viewModel.state.first { it.authenticationError != null }
                assertTrue(state.authenticationError.orEmpty().contains("update wallet storage"))
                assertNull(repository.state.value.mnemonic)
                assertEquals(seed, SeedDataSource().getSeed())
            } finally {
                restoreStorageWrites()
            }
        }

    @Test
    fun `unreadable preferences are preserved while the legacy seed is upgraded`() =
        runTest {
            // Given
            val mnemonic = installLegacyWallet()
            preferencesStorage.setBlob("broken preferences")

            // When
            val unlocked = keyStore().unlock(PASSWORD)

            // Then
            assertEquals(mnemonic, unlocked)
            assertEquals(mnemonic.phrase, decryptEnvelopeIndependently(assertNotNull(SeedDataSource().getSeed()), PASSWORD))
            assertEquals("broken preferences", preferencesStorage.blob.first())
        }

    @Test
    fun `replacing a wallet with a different password does not make old preferences block unlock`() =
        runTest {
            // Given
            installLegacyWallet()
            val oldPreferences = preferencesStorage.blob.first()
            val replacement = AttoMnemonic.fromEntropy(ByteArray(33) { 1 })
            val replacementPassword = "Replacement8!"
            keyStore().clear()
            keyStore().savePassword(replacement.phrase, replacementPassword)

            // When
            val unlocked = keyStore().unlock(replacementPassword)

            // Then
            assertEquals(replacement, unlocked)
            assertEquals(oldPreferences, preferencesStorage.blob.first())
        }

    @Test
    fun `PBKDF2 envelopes migrate to Argon2 without changing the password or plaintext`() =
        runTest {
            // Given
            val mnemonic = AttoMnemonic.fromEntropy(ByteArray(33))
            val oldSeed = encryptPbkdf2Envelope(mnemonic.phrase, PASSWORD, "seed")
            val oldPreferences = encryptPbkdf2Envelope(PREFERENCES, PASSWORD, "preferences")
            SeedDataSource().setSeed(oldSeed)
            preferencesStorage.setBlob(oldPreferences)

            // When / Then
            assertNull(keyStore().unlock("WrongPassword7!"))
            assertEquals(oldSeed, SeedDataSource().getSeed())
            assertEquals(oldPreferences, preferencesStorage.blob.first())
            assertEquals(mnemonic, keyStore().unlock(PASSWORD))
            val seed = assertNotNull(SeedDataSource().getSeed())
            val preferences = assertNotNull(preferencesStorage.blob.first())
            assertTrue(seed.contains("Argon2id"))
            assertTrue(preferences.contains("Argon2id"))
            assertEquals(mnemonic.phrase, decryptEnvelopeIndependently(seed, PASSWORD))
            assertEquals(PREFERENCES, decryptEnvelopeIndependently(preferences, PASSWORD))
            assertEquals(seed, localStorage.getItem("mnemonic"))
            assertNotNull(localStorage.getItem("preferences"))
            assertNull(localStorage.getItem("seed"))
            assertNull(localStorage.getItem(LEGACY_PREFERENCES_KEY))
            assertEquals(mnemonic, keyStore().unlock(PASSWORD))
            assertEquals(seed, SeedDataSource().getSeed())
            assertEquals(preferences, preferencesStorage.blob.first())
        }

    @Test
    fun `already upgraded mnemonic moves to the new key without reencryption and migrates public settings`() =
        runTest {
            // Given
            val mnemonic = AttoMnemonic.fromEntropy(ByteArray(33))
            val encrypted = SeedAESInteractor(SaltDataSource()).encryptSeed(mnemonic.phrase, PASSWORD, EncryptedDataPurpose.SEED)
            localStorage.removeItem("mnemonic")
            localStorage.setItem("seed", encrypted)
            installLegacyPreferences(null, work = "{}")

            // When
            assertEquals(mnemonic, keyStore().unlock(PASSWORD))

            // Then
            assertEquals(encrypted, localStorage.getItem("mnemonic"))
            assertNull(localStorage.getItem("seed"))
            assertNull(localStorage.getItem(LEGACY_PREFERENCES_KEY))
            assertNull(preferencesStorage.blob.first())
            assertEquals("2026-09-14", preferencesStorage.termsAndConditionsDate.first())
            assertEquals("{}", preferencesStorage.work.first())
            assertEquals(mnemonic, keyStore().unlock(PASSWORD))
            assertEquals(encrypted, localStorage.getItem("mnemonic"))
        }

    @Test
    fun `Argon2 worker failure keeps an existing wallet locked without changing its records`() =
        runTest {
            // Given
            installLegacyWallet()
            keyStore().unlock(PASSWORD)
            val seed = SeedDataSource().getSeed()
            val preferences = preferencesStorage.blob.first()
            val repository = AppStateRepository(keyStore(), TempSeedDataSource(), backgroundScope)
            val viewModel = AppViewModel(repository, CheckPasswordInteractor(), TermsAndConditionsRepository(preferencesStorage))
            observeArgon2Workers("constructor")
            try {
                // When
                val accepted = viewModel.enterPassword(PASSWORD)

                // Then
                assertFalse(accepted)
                assertNotNull(viewModel.state.first { it.authenticationError != null }.authenticationError)
                assertNull(repository.state.value.mnemonic)
                assertEquals(seed, SeedDataSource().getSeed())
                assertEquals(preferences, preferencesStorage.blob.first())
            } finally {
                restoreArgon2Workers()
            }
        }

    @Test
    fun `Argon2 failure during password creation preserves the pending phrase and permits retry`() =
        runTest {
            // Given
            val mnemonic = installLegacyWallet()
            val seed = SeedDataSource().getSeed()
            val temporary = TempSeedDataSource()
            val repository =
                AppStateRepository(
                    keyStore(),
                    temporary,
                    CoroutineScope(backgroundScope.coroutineContext + Dispatchers.Default),
                )
            repository.importSecret(mnemonic.words)
            val viewModel = CreatePasswordViewModel(repository, CheckPasswordInteractor(), TermsAndConditionsRepository(preferencesStorage))
            viewModel.setPassword(PASSWORD)
            viewModel.setPasswordConfirm(PASSWORD)
            observeArgon2Workers("constructor")
            try {
                // When
                val accepted = viewModel.savePassword()

                // Then
                assertFalse(accepted)
                assertNotNull(viewModel.state.value.storageError)
                assertEquals(seed, SeedDataSource().getSeed())
                assertEquals(mnemonic.phrase, temporary.seed)
                assertNull(repository.state.value.mnemonic)
            } finally {
                restoreArgon2Workers()
            }
            try {
                assertTrue(viewModel.savePassword())
                assertNull(viewModel.state.value.storageError)
                assertNull(temporary.seed)
            } finally {
                repository.lock()
            }
        }

    @Test
    fun `preference writes after migration use the new format and remain readable after lock`() =
        runTest {
            // Given
            installLegacyWallet()
            val scope = CoroutineScope(backgroundScope.coroutineContext + Dispatchers.Default)
            val repository = AppStateRepository(keyStore(), TempSeedDataSource(), scope)
            val crypto = SeedAESInteractor(SaltDataSource())
            val preferences = PreferencesRepository(preferencesStorage, repository, crypto)
            assertTrue(repository.submitPassword(PASSWORD))
            preferences.state.first { it.addressLabel("synthetic-address") == "Savings" }
            val before = preferencesStorage.blob.first()

            // When
            preferences.saveAddressLabel("synthetic-address", "Updated")
            val after = assertNotNull(preferencesStorage.blob.first())
            repository.lock()

            // Then
            assertNotEquals(before, after)
            val data = assertNotNull(crypto.decryptSeed(after, PASSWORD, EncryptedDataPurpose.PREFERENCES))
            assertFalse(data.needsMigration)
            assertTrue(data.plaintext.contains("Updated"))
            assertNull(crypto.decryptSeed(after, PASSWORD, EncryptedDataPurpose.SEED))
            assertTrue(repository.submitPassword(PASSWORD))
            assertEquals(after, preferencesStorage.blob.first())
            repository.lock()
        }

    private suspend fun installLegacyWallet(): AttoMnemonic {
        restoreStorageWrites()
        localStorage.setItem("salt", LEGACY_SALT)
        val mnemonic = AttoMnemonic.fromEntropy(ByteArray(33))
        localStorage.removeItem("mnemonic")
        localStorage.setItem("seed", encryptLegacy(mnemonic.phrase, PASSWORD))
        installLegacyPreferences(encryptLegacy(PREFERENCES, PASSWORD), terms = TermsAndConditions.EFFECTIVE_DATE, work = "{}")
        return mnemonic
    }

    private fun keyStore() =
        PlatformWalletKeyStore(
            SeedDataSource(),
            PasswordDataSource(),
            SeedAESInteractor(SaltDataSource()),
            preferencesStorage,
        )

    private companion object {
        val preferencesStorage = PreferencesDataSource()
        const val PASSWORD = "Synthetic7!"
        const val PREFERENCES = """{"addresses":[{"value":"synthetic-address","label":"Savings"}],"future":"preserve me"}"""
    }
}
