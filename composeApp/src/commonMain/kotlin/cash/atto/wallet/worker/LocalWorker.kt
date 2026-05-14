package cash.atto.wallet.worker

import cash.atto.commons.worker.AttoWorker

internal expect suspend fun createLocalWorker(): AttoWorker

internal expect suspend fun isLocalWorkerSupported(): Boolean
