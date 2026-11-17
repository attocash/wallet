package cash.atto.wallet.repository

import cash.atto.commons.AttoAccountEntry
import cash.atto.commons.AttoBlockType
import cash.atto.commons.node.monitor.AttoHeightMonitor.Message
import cash.atto.wallet.support.testAccountEntry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class AccountEntrySyncTest {
    @Test
    fun `catch up commits at most 100 entries at a time and flushes the final batch`() =
        runTest {
            // Given
            val entries = accountEntries(205)
            val saved = mutableListOf<List<AttoAccountEntry>>()
            val acknowledged = mutableListOf<AttoAccountEntry>()
            val messages =
                entries.map { entry ->
                    Message(entry) {
                        assertTrue(saved.any { entry in it })
                        acknowledged.add(entry)
                    }
                }

            // When
            persistAccountEntryStream(messages.asFlow(), 1.seconds) { saved.add(it) }

            // Then
            assertEquals(listOf(100, 100, 5), saved.map { it.size })
            assertEquals(entries, saved.flatten())
            assertEquals(entries, acknowledged)
        }

    @Test
    fun `a partial batch flushes while the stream stays open`() =
        runTest {
            // Given
            val entries = accountEntries(2)
            val stream =
                flow {
                    emit(Message(entries.first()) {})
                    delay(20.milliseconds)
                    emit(Message(entries.last()) {})
                    awaitCancellation()
                }
            val saved = mutableListOf<List<AttoAccountEntry>>()

            // When
            val syncing = launch { persistAccountEntryStream(stream, 1.seconds) { saved.add(it) } }
            advanceTimeBy(24.milliseconds)
            runCurrent()

            // Then
            assertTrue(saved.isEmpty())
            advanceTimeBy(1.milliseconds)
            runCurrent()
            assertEquals(listOf(entries), saved)
            syncing.cancelAndJoin()
        }

    @Test
    fun `a failed batch retries before later entries or the resume cursor advance`() =
        runTest {
            // Given
            val entries = accountEntries(101)
            val acknowledged = mutableListOf<AttoAccountEntry>()
            val messages = entries.map { entry -> Message(entry) { acknowledged.add(entry) } }
            val attempts = mutableListOf<List<AttoAccountEntry>>()

            // When
            val syncing =
                launch {
                    persistAccountEntryStream(messages.asFlow(), 1.seconds) { batch ->
                        attempts.add(batch)
                        check(attempts.size > 1) { "Storage temporarily unavailable" }
                    }
                }
            runCurrent()

            // Then
            val first = entries.take(100)
            assertEquals(listOf(first), attempts)
            assertTrue(acknowledged.isEmpty())
            advanceTimeBy(1.seconds)
            syncing.join()
            assertEquals(listOf(first, first, entries.takeLast(1)), attempts)
            assertEquals(entries, acknowledged)
        }

    private fun accountEntries(count: Int): List<AttoAccountEntry> =
        List(count) { index -> testAccountEntry(index.toULong() + 1UL, AttoBlockType.RECEIVE, "0", "1") }
}
