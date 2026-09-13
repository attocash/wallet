package cash.atto.wallet.repository

import cash.atto.commons.AttoHash
import cash.atto.commons.AttoMnemonic
import cash.atto.commons.toAttoIndex
import cash.atto.wallet.datasource.TempSeedDataSource
import cash.atto.wallet.state.AppState
import cash.atto.wallet.state.AppState.AuthState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
class AppStateRepositoryTest {
    @Test
    fun `existing wallet starts locked and only the correct password opens it`() =
        runTest {
            // Given
            // Browser key derivation uses real asynchronous work, so keep its session timer on real time.
            val repositoryScope = CoroutineScope(backgroundScope.coroutineContext + Dispatchers.Default)
            val store = TestKeyStore(AttoMnemonic.fromEntropy(ByteArray(33)))
            val repository = AppStateRepository(store, TempSeedDataSource(), repositoryScope)
            repository.state.first { it.authState != AuthState.UNKNOWN }

            // When
            val wrongPasswordAccepted = repository.submitPassword("wrong")

            // Then
            assertFalse(wrongPasswordAccepted)
            assertEquals(AuthState.SESSION_INVALID, repository.state.value.authState)
            assertNull(repository.state.value.getSeed())
            assertTrue(repository.submitPassword(PASSWORD))
            assertNotNull(repository.state.value.getSeed())
        }

    @Test
    fun `saving a password consumes imported plaintext only after storage succeeds`() =
        runTest {
            // Given
            // Browser key derivation uses real asynchronous work, so keep its session timer on real time.
            val repositoryScope = CoroutineScope(backgroundScope.coroutineContext + Dispatchers.Default)
            val mnemonic = AttoMnemonic.fromEntropy(ByteArray(33))
            val temporary = TempSeedDataSource()
            val store = TestKeyStore(null)
            val repository = AppStateRepository(store, temporary, repositoryScope)
            repository.importSecret(mnemonic.words)
            store.failSave = true

            // When
            assertFailsWith<IllegalStateException> { repository.savePassword(PASSWORD) }

            // Then
            assertEquals(mnemonic.words.joinToString(" "), temporary.seed)
            assertNull(repository.state.value.getSeed())
            store.failSave = false
            repository.savePassword(PASSWORD)
            assertNull(temporary.seed)
            assertEquals(AuthState.SESSION_VALID, repository.state.value.authState)
            assertNotNull(repository.state.value.getSeed())
        }

    @Test
    fun `manual lock revokes retained state and signers and permits a new authenticated session`() =
        runTest {
            // Given
            // Browser key derivation uses real asynchronous work, so keep its session timer on real time.
            val repositoryScope = CoroutineScope(backgroundScope.coroutineContext + Dispatchers.Default)
            val repository =
                AppStateRepository(
                    TestKeyStore(AttoMnemonic.fromEntropy(ByteArray(33))),
                    TempSeedDataSource(),
                    repositoryScope,
                )
            repository.submitPassword(PASSWORD)
            val snapshot = repository.state.value
            val signer = assertNotNull(snapshot.unlockedWallet).signer(0U.toAttoIndex())
            val hash = AttoHash.parse("01".repeat(32))
            assertNotNull(signer.sign(hash))

            // When
            repository.lock()

            // Then
            assertRevoked(repository.state.value)
            assertNull(snapshot.mnemonic)
            assertNull(snapshot.password)
            assertNull(snapshot.getSeed())
            assertFailsWith<CancellationException> { signer.sign(hash) }
            assertTrue(repository.submitPassword(PASSWORD))
            assertNotNull(repository.state.value.getSeed())
            assertFailsWith<CancellationException> { signer.sign(hash) }
        }

    @Test
    fun `automatic expiry cancels pending work before it can publish`() =
        runTest {
            // Given
            val repository =
                AppStateRepository(
                    TestKeyStore(AttoMnemonic.fromEntropy(ByteArray(33))),
                    TempSeedDataSource(),
                    backgroundScope,
                )
            repository.submitPassword(PASSWORD)
            val snapshot = repository.state.value
            val unlockedWallet = assertNotNull(snapshot.unlockedWallet)
            val workStarted = CompletableDeferred<Unit>()
            val workReady = CompletableDeferred<Unit>()
            var published = false
            val receive =
                async {
                    unlockedWallet.run {
                        workStarted.complete(Unit)
                        workReady.await()
                        published = true
                    }
                }
            workStarted.await()

            // When
            advanceTimeBy(20.minutes)
            runCurrent()
            workReady.complete(Unit)

            // Then
            assertFailsWith<CancellationException> { receive.await() }
            assertFalse(published)
            assertRevoked(repository.state.value)
            assertNull(snapshot.mnemonic)
            assertNull(snapshot.getSeed())
        }

    @Test
    fun `logout revokes keys and cancels the expiry timer`() =
        runTest {
            // Given
            val store = TestKeyStore(AttoMnemonic.fromEntropy(ByteArray(33)))
            val temporary = TempSeedDataSource()
            val repository = AppStateRepository(store, temporary, backgroundScope)
            repository.submitPassword(PASSWORD)
            val snapshot = repository.state.value

            // When
            repository.deleteKeys()
            advanceTimeBy(21.minutes)
            runCurrent()

            // Then
            assertEquals(AuthState.NO_SEED, repository.state.value.authState)
            assertNull(temporary.seed)
            assertNull(snapshot.mnemonic)
            assertNull(snapshot.password)
            assertNull(repository.state.value.getSeed())
            assertNull(store.mnemonic)
            assertFalse(repository.submitPassword(PASSWORD))
        }

    @Test
    fun `locking abandoned onboarding discards the pending phrase`() =
        runTest {
            // Given
            val temporary = TempSeedDataSource()
            val repository = AppStateRepository(TestKeyStore(null), temporary, backgroundScope)
            repository.generateNewSecret()
            assertNotNull(temporary.seed)

            // When
            repository.lock()

            // Then
            assertNull(temporary.seed)
            assertRevoked(repository.state.value)
        }

    @Test
    fun `caller cancellation stops its operation without locking the wallet`() =
        runTest {
            // Given
            // Browser key derivation uses real asynchronous work, so keep its session timer on real time.
            val repositoryScope = CoroutineScope(backgroundScope.coroutineContext + Dispatchers.Default)
            val repository =
                AppStateRepository(
                    TestKeyStore(AttoMnemonic.fromEntropy(ByteArray(33))),
                    TempSeedDataSource(),
                    repositoryScope,
                )
            repository.submitPassword(PASSWORD)
            val unlockedWallet = assertNotNull(repository.state.value.unlockedWallet)
            val started = CompletableDeferred<Unit>()
            val stopped = CompletableDeferred<Unit>()
            val caller =
                async {
                    unlockedWallet.run {
                        try {
                            started.complete(Unit)
                            awaitCancellation()
                        } finally {
                            stopped.complete(Unit)
                        }
                    }
                }
            started.await()

            // When
            caller.cancel()
            stopped.await()

            // Then
            assertTrue(unlockedWallet.job.isActive)
            assertNotNull(repository.state.value.getSeed())
        }

    private suspend fun assertRevoked(state: AppState) {
        assertEquals(AuthState.SESSION_INVALID, state.authState)
        assertNull(state.mnemonic)
        assertNull(state.password)
        assertNull(state.getSeed())
    }

    private class TestKeyStore(
        var mnemonic: AttoMnemonic?,
    ) : WalletKeyStore {
        var failSave = false

        override suspend fun authState() = if (mnemonic == null) AuthState.NO_SEED else AuthState.SESSION_INVALID

        override suspend fun unlock(password: String) = mnemonic.takeIf { password == PASSWORD }

        override suspend fun savePassword(
            phrase: String?,
            password: String,
        ): AttoMnemonic {
            check(!failSave) { "Storage failed" }
            check(password == PASSWORD)
            val saved = AttoMnemonic.fromPhrase(checkNotNull(phrase))
            mnemonic = saved
            return saved
        }

        override suspend fun clear() {
            mnemonic = null
        }
    }

    private companion object {
        const val PASSWORD = "Synthetic7!"
    }
}
