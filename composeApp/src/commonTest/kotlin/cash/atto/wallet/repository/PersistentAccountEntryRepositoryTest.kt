package cash.atto.wallet.repository

import cash.atto.commons.AttoAccountEntry
import cash.atto.commons.AttoBlockType
import cash.atto.wallet.model.TransactionsHistoryState
import cash.atto.wallet.support.testAccountEntry
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class PersistentAccountEntryRepositoryTest {
    @Test
    fun `save completes and notifies observers only after history is persisted`() =
        runTest {
            // Given
            val dao = AccountEntryTestDao()
            val repository = PersistentAccountEntryRepository(dao)
            val entry = testAccountEntry(1UL, AttoBlockType.RECEIVE, "0", "1")
            val continueSave = CompletableDeferred<Unit>()
            dao.beforeSave = { continueSave.await() }
            val observations = mutableListOf<Boolean>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                repository.flow(entry.publicKey).collect { received ->
                    observations.add(repository.last(entry.publicKey) == received)
                }
            }

            // When
            val saving = async { repository.save(entry) }
            runCurrent()

            // Then
            assertFalse(saving.isCompleted)
            assertTrue(observations.isEmpty())
            assertNull(repository.last(entry.publicKey))
            continueSave.complete(Unit)
            saving.await()
            assertEquals(listOf(true), observations)
            assertEquals(entry, repository.last(entry.publicKey))
        }

    @Test
    fun `storage failure retries before stream acknowledgement or live notification`() =
        runTest {
            // Given
            val dao = AccountEntryTestDao()
            val repository = PersistentAccountEntryRepository(dao)
            val entry = testAccountEntry(1UL, AttoBlockType.RECEIVE, "0", "1")
            dao.beforeSave = { check(dao.saveAttempts > 1) { "Storage temporarily unavailable" } }
            val observed = mutableListOf<AttoAccountEntry>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                repository.flow(entry.publicKey).collect { observed.add(it) }
            }
            var acknowledged = false

            // When
            val saving =
                async {
                    retryWalletOperation("save test entry", 1.seconds) {
                        repository.save(entry)
                        acknowledged = true
                    }
                }
            runCurrent()

            // Then
            assertFalse(acknowledged)
            assertTrue(observed.isEmpty())
            assertNull(repository.last(entry.publicKey))
            advanceTimeBy(1.seconds)
            saving.await()
            assertTrue(acknowledged)
            assertEquals(2, dao.saveAttempts)
            assertEquals(listOf(entry), observed)
            assertEquals(entry, repository.last(entry.publicKey))
        }

    @Test
    fun `replaying a persisted entry retains one history row and one summary contribution`() =
        runTest {
            // Given
            val repository = PersistentAccountEntryRepository(AccountEntryTestDao())
            val entry = testAccountEntry(1UL, AttoBlockType.RECEIVE, "0", "1")
            repository.save(entry)

            // When
            repository.save(entry)

            // Then
            assertEquals(listOf(entry), repository.listRecent(entry.publicKey, 10))
            val snapshot = repository.summary(entry.publicKey)
            assertEquals(1, snapshot.summary.totalTransactions)
            assertEquals(entry.balance, snapshot.summary.totalReceived)
            assertEquals(entry.height.value, snapshot.throughHeight)
        }

    @Test
    fun `cancelling a pending save does not acknowledge or publish the entry`() =
        runTest {
            // Given
            val dao = AccountEntryTestDao()
            val repository = PersistentAccountEntryRepository(dao)
            val entry = testAccountEntry(1UL, AttoBlockType.RECEIVE, "0", "1")
            val continueSave = CompletableDeferred<Unit>()
            dao.beforeSave = { continueSave.await() }
            var acknowledged = false
            val observed = mutableListOf<AttoAccountEntry>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                repository.flow(entry.publicKey).collect { observed.add(it) }
            }
            val saving =
                launch {
                    repository.save(entry)
                    acknowledged = true
                }
            runCurrent()

            // When
            saving.cancel()
            continueSave.complete(Unit)
            saving.join()

            // Then
            assertFalse(acknowledged)
            assertTrue(observed.isEmpty())
            assertNull(repository.last(entry.publicKey))
        }

    @Test
    fun `summary containing a committed entry does not count its later notification twice`() =
        runTest {
            // Given
            val dao = AccountEntryTestDao()
            val repository = PersistentAccountEntryRepository(dao)
            val initial = testAccountEntry(1UL, AttoBlockType.RECEIVE, "0", "1")
            val incoming = testAccountEntry(2UL, AttoBlockType.RECEIVE, "1", "2")
            repository.save(initial)
            var history = TransactionsHistoryState().withInitialPage(repository.listRecent(initial.publicKey, 50), 50)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                repository.flow(initial.publicKey).collect { history = history.withLiveEntry(it) }
            }
            val committed = CompletableDeferred<Unit>()
            val continueSave = CompletableDeferred<Unit>()
            dao.afterSave = {
                committed.complete(Unit)
                continueSave.await()
            }
            val saving = async { repository.save(incoming) }
            committed.await()

            // When
            history = history.withSummary(repository.summary(initial.publicKey))
            continueSave.complete(Unit)
            saving.await()

            // Then
            assertEquals(listOf(incoming, initial), history.loadedEntries)
            assertEquals(2, history.fullHistorySummary.totalTransactions)
            assertEquals(incoming.balance, history.fullHistorySummary.totalReceived)
        }
}
