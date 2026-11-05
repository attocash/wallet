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
            val store = TestKeyStore(AttoMnemonic.fromEntropy(ByteArray(AttoMnemonic.ENTROPY_SIZE)))
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
            val mnemonic = AttoMnemonic.fromEntropy(ByteArray(AttoMnemonic.ENTROPY_SIZE))
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
                    TestKeyStore(AttoMnemonic.fromEntropy(ByteArray(AttoMnemonic.ENTROPY_SIZE))),
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
                    TestKeyStore(AttoMnemonic.fromEntropy(ByteArray(AttoMnemonic.ENTROPY_SIZE))),
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
            val store = TestKeyStore(AttoMnemonic.fromEntropy(ByteArray(AttoMnemonic.ENTROPY_SIZE)))
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
    fun `failed logout stays locked while the stored wallet can still be unlocked`() =
        runTest {
            // Given
            val store = TestKeyStore(AttoMnemonic.fromEntropy(ByteArray(AttoMnemonic.ENTROPY_SIZE)))
            val repositoryScope = CoroutineScope(backgroundScope.coroutineContext + Dispatchers.Default)
            val repository = AppStateRepository(store, TempSeedDataSource(), repositoryScope)
            repository.submitPassword(PASSWORD)
            val snapshot = repository.state.value
            val signer = assertNotNull(snapshot.unlockedWallet).signer(0U.toAttoIndex())
            store.failClear = true

            // When
            assertFailsWith<IllegalStateException> { repository.deleteKeys() }

            // Then
            assertRevoked(repository.state.value)
            assertNull(snapshot.mnemonic)
            assertNull(snapshot.password)
            assertFailsWith<CancellationException> { signer.sign(AttoHash.parse("01".repeat(32))) }
            assertNotNull(store.mnemonic)
            assertTrue(repository.submitPassword(PASSWORD))
            store.failClear = false
            repository.deleteKeys()
            assertEquals(AuthState.NO_SEED, repository.state.value.authState)
        }

    @Test
    fun `cancelling logout after storage starts completes deletion and the state transition`() =
        runTest {
            // Given
            val store = TestKeyStore(AttoMnemonic.fromEntropy(ByteArray(AttoMnemonic.ENTROPY_SIZE)))
            val repository = AppStateRepository(store, TempSeedDataSource(), backgroundScope)
            repository.submitPassword(PASSWORD)
            val snapshot = repository.state.value
            val wallet = assertNotNull(snapshot.unlockedWallet)
            val workStarted = CompletableDeferred<Unit>()
            val work = async {
                wallet.run {
                    workStarted.complete(Unit)
                    awaitCancellation()
                }
            }
            workStarted.await()
            store.clearStarted = CompletableDeferred()
            store.continueClear = CompletableDeferred()
            val deleting = async { repository.deleteKeys() }
            store.clearStarted!!.await()

            // When
            deleting.cancel()
            store.continueClear!!.complete(Unit)
            deleting.join()

            // Then
            assertFailsWith<CancellationException> { work.await() }
            assertNull(snapshot.mnemonic)
            assertNull(snapshot.password)
            assertNull(store.mnemonic)
            assertEquals(AuthState.NO_SEED, repository.state.value.authState)
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
                    TestKeyStore(AttoMnemonic.fromEntropy(ByteArray(AttoMnemonic.ENTROPY_SIZE))),
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

    @Test
    fun `password rotation replaces persisted credentials and revokes the old session`() =
        runTest {
            // Given
            val store = TestKeyStore(AttoMnemonic.fromEntropy(ByteArray(AttoMnemonic.ENTROPY_SIZE)))
            val repository = AppStateRepository(store, TempSeedDataSource(), backgroundScope)
            assertTrue(repository.submitPassword(PASSWORD))
            val previous = repository.state.value
            val mnemonic = previous.mnemonic

            // When
            assertTrue(repository.changePassword(PASSWORD, NEW_PASSWORD))

            // Then
            assertEquals(NEW_PASSWORD, repository.state.value.password)
            assertEquals(mnemonic, repository.state.value.mnemonic)
            assertNull(previous.password)
            repository.lock()
            val restarted = AppStateRepository(store, TempSeedDataSource(), backgroundScope)
            assertFalse(restarted.submitPassword(PASSWORD))
            assertTrue(restarted.submitPassword(NEW_PASSWORD))
            assertEquals(mnemonic, restarted.state.value.mnemonic)
        }

    @Test
    fun `wrong current weak identical and locked password changes preserve the wallet`() =
        runTest {
            // Given
            val store = TestKeyStore(AttoMnemonic.fromEntropy(ByteArray(AttoMnemonic.ENTROPY_SIZE)))
            val repository = AppStateRepository(store, TempSeedDataSource(), backgroundScope)
            assertTrue(repository.submitPassword(PASSWORD))
            val session = repository.state.value.unlockedWallet

            // When
            assertFalse(repository.changePassword("wrong", NEW_PASSWORD))
            assertFailsWith<IllegalArgumentException> { repository.changePassword(PASSWORD, PASSWORD) }
            assertFailsWith<IllegalArgumentException> { repository.changePassword(PASSWORD, "weak") }

            // Then
            assertTrue(session === repository.state.value.unlockedWallet)
            assertEquals(PASSWORD, store.password)
            repository.lock()
            assertFailsWith<IllegalStateException> { repository.changePassword(PASSWORD, NEW_PASSWORD) }
            assertTrue(repository.submitPassword(PASSWORD))
        }

    @Test
    fun `failed password commit preserves the authenticated session and original password`() =
        runTest {
            // Given
            val store = TestKeyStore(AttoMnemonic.fromEntropy(ByteArray(AttoMnemonic.ENTROPY_SIZE)))
            val repository = AppStateRepository(store, TempSeedDataSource(), backgroundScope)
            repository.submitPassword(PASSWORD)
            val session = repository.state.value.unlockedWallet
            store.failSave = true

            // When
            assertFailsWith<IllegalStateException> { repository.changePassword(PASSWORD, NEW_PASSWORD) }

            // Then
            assertTrue(session === repository.state.value.unlockedWallet)
            assertEquals(PASSWORD, repository.state.value.password)
            repository.lock()
            assertTrue(repository.submitPassword(PASSWORD))
            assertFalse(repository.submitPassword(NEW_PASSWORD))
        }

    @Test
    fun `cancelling password preparation leaves credentials unchanged`() =
        runTest {
            // Given
            val store = TestKeyStore(AttoMnemonic.fromEntropy(ByteArray(AttoMnemonic.ENTROPY_SIZE)))
            val repository = AppStateRepository(store, TempSeedDataSource(), backgroundScope)
            repository.submitPassword(PASSWORD)
            store.preparationStarted = CompletableDeferred()
            store.continuePreparation = CompletableDeferred()
            val changing = async { repository.changePassword(PASSWORD, NEW_PASSWORD) }
            store.preparationStarted!!.await()

            // When
            changing.cancel()
            assertFailsWith<CancellationException> { changing.await() }

            // Then
            assertEquals(PASSWORD, store.password)
            assertEquals(PASSWORD, repository.state.value.password)
            assertNotNull(repository.state.value.mnemonic)
        }

    @Test
    fun `cancellation after commit starts completes the matching session transition`() =
        runTest {
            // Given
            val store = TestKeyStore(AttoMnemonic.fromEntropy(ByteArray(AttoMnemonic.ENTROPY_SIZE)))
            val repository = AppStateRepository(store, TempSeedDataSource(), backgroundScope)
            repository.submitPassword(PASSWORD)
            store.commitStarted = CompletableDeferred()
            store.continueCommit = CompletableDeferred()
            val changing = async { repository.changePassword(PASSWORD, NEW_PASSWORD) }
            store.commitStarted!!.await()

            // When
            changing.cancel()
            store.continueCommit!!.complete(Unit)
            changing.join()

            // Then
            assertEquals(NEW_PASSWORD, store.password)
            assertEquals(NEW_PASSWORD, repository.state.value.password)
            assertNotNull(repository.state.value.mnemonic)
            repository.lock()
            assertTrue(repository.submitPassword(NEW_PASSWORD))
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
        var failClear = false
        var password = PASSWORD
        var preparationStarted: CompletableDeferred<Unit>? = null
        var continuePreparation: CompletableDeferred<Unit>? = null
        var commitStarted: CompletableDeferred<Unit>? = null
        var continueCommit: CompletableDeferred<Unit>? = null
        var clearStarted: CompletableDeferred<Unit>? = null
        var continueClear: CompletableDeferred<Unit>? = null

        override suspend fun authState() = if (mnemonic == null) AuthState.NO_SEED else AuthState.SESSION_INVALID

        override suspend fun unlock(password: String) = mnemonic.takeIf { password == this.password }

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

        override suspend fun preparePasswordChange(
            mnemonic: AttoMnemonic,
            currentPassword: String,
            newPassword: String,
        ): suspend () -> Unit {
            preparationStarted?.complete(Unit)
            continuePreparation?.await()
            return {
                commitStarted?.complete(Unit)
                continueCommit?.await()
                check(!failSave) { "Storage failed" }
                password = newPassword
            }
        }

        override suspend fun clear() {
            clearStarted?.complete(Unit)
            continueClear?.await()
            check(!failClear) { "Storage failed" }
            mnemonic = null
        }
    }

    private companion object {
        const val PASSWORD = "Synthetic7!"
        const val NEW_PASSWORD = "fK7q9Vm2xT6n4Rb8"
    }
}
