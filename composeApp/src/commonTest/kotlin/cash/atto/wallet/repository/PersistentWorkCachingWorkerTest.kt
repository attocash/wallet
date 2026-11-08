package cash.atto.wallet.repository

import cash.atto.commons.AttoAccount
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.AttoAmount
import cash.atto.commons.AttoHash
import cash.atto.commons.AttoHeight
import cash.atto.commons.AttoNetwork
import cash.atto.commons.AttoPublicKey
import cash.atto.commons.AttoWork
import cash.atto.commons.AttoWorkTarget
import cash.atto.commons.toAddress
import cash.atto.commons.toAttoVersion
import cash.atto.commons.worker.AttoWorker
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PersistentWorkCachingWorkerTest {
    @Test
    fun `superseded work cannot finish or deregister a newer request for the same target`() =
        runTest {
            // Given
            val dao = WorkTestDao()
            val delegate = TestWorker()
            val worker = PersistentWorkCachingWorker(AttoNetwork.LIVE, PersistentWorkCache(dao), delegate, backgroundScope.coroutineContext)
            worker.cacheNextWork(PUBLIC_KEY, null)
            runCurrent()
            worker.cacheNextWork(PUBLIC_KEY, ACCOUNT)
            runCurrent()
            worker.cacheNextWork(PUBLIC_KEY, null)
            runCurrent()
            assertEquals(3, delegate.requests.size)

            // When
            delegate.requests[0].result.complete(WORK)
            runCurrent()
            worker.cacheNextWork(PUBLIC_KEY, null)
            runCurrent()

            // Then
            assertNull(dao.stored)
            assertEquals(3, delegate.requests.size)
            delegate.requests[1].result.complete(WORK)
            runCurrent()
            assertNull(dao.stored)
            delegate.requests[2].result.complete(WORK)
            runCurrent()
            assertEquals(WORK, dao.stored?.let { AttoWork(it.value) })
            worker.close()
        }

    @Test
    fun `cancelling the owning session stops pending work before it is persisted`() =
        runTest {
            // Given
            val parent = Job(backgroundScope.coroutineContext[Job])
            val dao = WorkTestDao()
            val delegate = TestWorker()
            val worker =
                PersistentWorkCachingWorker(
                    AttoNetwork.LIVE,
                    PersistentWorkCache(dao),
                    delegate,
                    backgroundScope.coroutineContext + parent,
                )
            worker.cacheNextWork(PUBLIC_KEY, null)
            runCurrent()
            assertEquals(1, delegate.requests.size)

            // When
            parent.cancel()
            delegate.requests
                .single()
                .result
                .complete(WORK)
            runCurrent()

            // Then
            assertNull(dao.stored)
            worker.close()
            assertTrue(delegate.closed)
        }

    private class TestWorker : AttoWorker {
        class Request(
            val target: AttoWorkTarget,
            val result: CompletableDeferred<AttoWork> = CompletableDeferred(),
        )

        val requests = mutableListOf<Request>()
        var closed = false

        override suspend fun work(
            threshold: ULong,
            target: AttoWorkTarget,
        ): AttoWork {
            val request = Request(target)
            requests.add(request)
            return request.result.await()
        }

        override fun close() {
            closed = true
        }
    }

    private companion object {
        val PUBLIC_KEY = AttoPublicKey(ByteArray(32))
        val WORK = AttoWork.parse("576887B000000000")
        val ADDRESS = PUBLIC_KEY.toAddress(AttoAlgorithm.V1)
        val ACCOUNT =
            AttoAccount(
                address = ADDRESS,
                network = AttoNetwork.LIVE,
                version = 0.toAttoVersion(),
                height = AttoHeight(1UL),
                balance = AttoAmount.MIN,
                lastTransactionHash = AttoHash.parse("01".repeat(32)),
                lastTransactionTimestamp = AttoNetwork.INITIAL_INSTANT,
                representativeAddress = ADDRESS,
            )
    }
}
