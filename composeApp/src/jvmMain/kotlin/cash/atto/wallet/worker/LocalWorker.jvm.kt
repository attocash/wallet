package cash.atto.wallet.worker

import cash.atto.commons.worker.AttoWorker

internal actual suspend fun createLocalWorker(): AttoWorker = error("Local worker is only supported on web")

internal actual suspend fun isLocalWorkerSupported(): Boolean = false
