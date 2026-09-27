package cash.atto.wallet.interactor

import cash.atto.commons.toByteArray
import cash.atto.wallet.datasource.SaltDataSource
import cash.atto.wallet.repository.WalletStorageException
import cash.atto.wallet.support.activeArgon2Workers
import cash.atto.wallet.support.maximumArgon2Workers
import cash.atto.wallet.support.observeArgon2Workers
import cash.atto.wallet.support.restoreArgon2Workers
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class Argon2KeyDerivationTest {
    @Test
    fun `worker output matches an independent native Argon2id vector`() =
        runTest {
            // Given: Node 24 crypto.argon2Sync, Argon2id v19, 64 MiB, 3 passes, one lane, 32-byte output.
            val derivation = Argon2KeyDerivation()

            // When
            val key = derivation.derive(PASSWORD, SALT, 3, 65_536, 1)

            // Then
            assertEquals(EXPECTED_KEY, key.toByteArray().toHexString())
        }

    @Test
    fun `wallet reads a complete envelope produced by native Argon2id and AES-GCM`() =
        runTest {
            // Given: generated independently with Node crypto.argon2Sync and createCipheriv.
            val record =
                """
                {"version":2,"purpose":"seed","kdf":"Argon2id","argonVersion":19,
                "memoryKiB":65536,"iterations":3,"parallelism":1,"salt":"MDEyMzQ1Njc4OWFiY2RlZg==",
                "iv":"BwcHBwcHBwcHBwcH","ciphertext":"jGkNeJk0Wupsziae+5ec31kkMeotUyajfM/zd8VqNbHPnVdkON6RkpAgxjMaYg=="}
                """.trimIndent()

            // When
            val result = SeedAESInteractor(SaltDataSource()).decryptSeed(record, PASSWORD, EncryptedDataPurpose.SEED)

            // Then
            assertEquals("Native Argon2 fixture áβ🙂", assertNotNull(result).plaintext)
            assertFalse(result.needsMigration)
        }

    @Test
    fun `concurrent derivations use at most one worker and release it after completion`() =
        runTest {
            // Given
            observeArgon2Workers()
            val derivation = Argon2KeyDerivation()
            try {
                // When
                val first = async(start = CoroutineStart.UNDISPATCHED) { derivation.derive(PASSWORD, SALT, 3, 65_536, 1) }
                val second = async(start = CoroutineStart.UNDISPATCHED) { derivation.derive(PASSWORD, SALT, 3, 65_536, 1) }

                // Then
                assertEquals(EXPECTED_KEY, first.await().toByteArray().toHexString())
                assertEquals(EXPECTED_KEY, second.await().toByteArray().toHexString())
                assertEquals(1, maximumArgon2Workers())
                assertEquals(0, activeArgon2Workers())
            } finally {
                restoreArgon2Workers()
            }
        }

    @Test
    fun `cancelling a derivation terminates its worker and permits another derivation`() =
        runTest {
            // Given
            observeArgon2Workers()
            val derivation = Argon2KeyDerivation()
            try {
                val pending = async(start = CoroutineStart.UNDISPATCHED) { derivation.derive(PASSWORD, SALT, 3, 65_536, 1) }
                assertEquals(1, activeArgon2Workers())

                // When
                pending.cancelAndJoin()

                // Then
                assertEquals(0, activeArgon2Workers())
                assertEquals(EXPECTED_KEY, derivation.derive(PASSWORD, SALT, 3, 65_536, 1).toByteArray().toHexString())
                assertEquals(0, activeArgon2Workers())
            } finally {
                restoreArgon2Workers()
            }
        }

    @Test
    fun `worker startup load computation and timeout failures release resources and remain retryable`() =
        runTest {
            // Given
            val derivation = Argon2KeyDerivation()
            for (fault in listOf("constructor", "error", "response", "timeout")) {
                observeArgon2Workers(fault)
                try {
                    // When / Then
                    assertFailsWith<WalletStorageException> { derivation.derive(PASSWORD, SALT, 3, 65_536, 1) }
                    assertEquals(0, activeArgon2Workers())
                } finally {
                    restoreArgon2Workers()
                }
            }
            assertEquals(EXPECTED_KEY, derivation.derive(PASSWORD, SALT, 3, 65_536, 1).toByteArray().toHexString())
        }

    private companion object {
        const val PASSWORD = "Synthetic7!"
        val SALT = "0123456789abcdef".encodeToByteArray()
        const val EXPECTED_KEY = "cbcc56771c89e2649bbc1d27e0a123493482958985625af523da6bc84a59dcc3"
    }
}
