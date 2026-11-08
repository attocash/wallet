package cash.atto.wallet.repository

import cash.atto.commons.AttoNetwork
import cash.atto.commons.AttoPublicKey
import cash.atto.commons.AttoWork
import cash.atto.commons.AttoWorkTarget
import cash.atto.wallet.datasource.Work
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class PersistentWorkCacheTest {
    @Test
    fun `clearing during a save leaves persisted and cached work cleared`() =
        runTest {
            // Given
            val dao = WorkTestDao()
            val cache = PersistentWorkCache(dao)
            val setStarted = CompletableDeferred<Unit>()
            val continueSet = CompletableDeferred<Unit>()
            dao.afterSet = {
                setStarted.complete(Unit)
                continueSet.await()
            }
            val saving = async { cache.save(PUBLIC_KEY, WORK) }
            setStarted.await()

            // When
            val clearing = async { cache.clear(PUBLIC_KEY) }
            runCurrent()

            // Then
            assertFalse(clearing.isCompleted)
            continueSet.complete(Unit)
            saving.await()
            clearing.await()
            assertNull(dao.stored)
            assertNull(cache.getValid(PUBLIC_KEY, NETWORK, TIMESTAMP, TARGET))
            assertNull(PersistentWorkCache(dao).getValid(PUBLIC_KEY, NETWORK, TIMESTAMP, TARGET))
            assertEquals(2L, cache.version.value)
        }

    @Test
    fun `clearing during a storage read cannot rehydrate removed work`() =
        runTest {
            // Given
            val dao = WorkTestDao()
            dao.stored = Work(PUBLIC_KEY.value, WORK.value)
            val cache = PersistentWorkCache(dao)
            val readStarted = CompletableDeferred<Unit>()
            val continueRead = CompletableDeferred<Unit>()
            dao.afterGet = {
                readStarted.complete(Unit)
                continueRead.await()
            }
            val reading = async { cache.getValid(PUBLIC_KEY, NETWORK, TIMESTAMP, TARGET) }
            readStarted.await()

            // When
            val clearing = async { cache.clear(PUBLIC_KEY) }
            runCurrent()

            // Then
            assertFalse(clearing.isCompleted)
            continueRead.complete(Unit)
            assertEquals(WORK, reading.await())
            clearing.await()
            assertNull(dao.stored)
            assertNull(cache.getValid(PUBLIC_KEY, NETWORK, TIMESTAMP, TARGET))
            assertEquals(1L, cache.version.value)
        }

    @Test
    fun `failed clearing retains committed memory and does not announce a change`() =
        runTest {
            // Given
            val dao = WorkTestDao()
            val cache = PersistentWorkCache(dao)
            cache.save(PUBLIC_KEY, WORK)
            dao.beforeClear = { error("Storage unavailable") }

            // When
            assertFailsWith<IllegalStateException> { cache.clear(PUBLIC_KEY) }
            dao.failGet = true

            // Then
            assertEquals(WORK, cache.getValid(PUBLIC_KEY, NETWORK, TIMESTAMP, TARGET))
            assertEquals(1L, cache.version.value)
            assertEquals(WORK, dao.stored?.let { AttoWork(it.value) })
        }

    private companion object {
        val PUBLIC_KEY = AttoPublicKey(ByteArray(32))
        val TARGET = AttoWorkTarget(PUBLIC_KEY.value)
        val NETWORK = AttoNetwork.LIVE
        val TIMESTAMP = AttoNetwork.INITIAL_INSTANT
        val WORK = AttoWork.parse("576887B000000000")
    }
}
