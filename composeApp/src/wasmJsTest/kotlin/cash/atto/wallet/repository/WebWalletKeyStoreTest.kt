package cash.atto.wallet.repository

import cash.atto.commons.AttoMnemonic
import cash.atto.wallet.datasource.PasswordDataSource
import cash.atto.wallet.datasource.SaltDataSource
import cash.atto.wallet.datasource.SeedDataSource
import cash.atto.wallet.datasource.TempSeedDataSource
import cash.atto.wallet.interactor.SeedAESInteractor
import cash.atto.wallet.state.AppState.AuthState
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WebWalletKeyStoreTest {
    @Test
    fun `web wallet can unlock from encrypted storage after clearing session memory`() =
        runTest {
            // Given
            val seedStorage = SeedDataSource()
            seedStorage.clearSeed()
            val keyStore = PlatformWalletKeyStore(seedStorage, PasswordDataSource(), SeedAESInteractor(SaltDataSource()))
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
                assertFalse(restarted.submitPassword("wrong"))
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
}
