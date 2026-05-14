package cash.atto.wallet.worker

import cash.atto.commons.worker.AttoWorker
import cash.atto.commons.worker.isWebSupported
import cash.atto.commons.worker.web

internal actual suspend fun createLocalWorker(): AttoWorker = AttoWorker.web()

internal actual suspend fun isLocalWorkerSupported(): Boolean = AttoWorker.isWebSupported()
