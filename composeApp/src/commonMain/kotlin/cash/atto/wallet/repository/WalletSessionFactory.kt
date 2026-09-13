package cash.atto.wallet.repository

import cash.atto.commons.AttoKeyIndex
import cash.atto.commons.AttoNetwork
import cash.atto.commons.AttoSigner
import cash.atto.commons.node.AttoNodeClient
import cash.atto.commons.node.monitor.createAccountMonitor
import cash.atto.commons.node.remote
import cash.atto.commons.toAttoIndex
import cash.atto.commons.wallet.AttoWallet
import cash.atto.commons.wallet.create
import cash.atto.commons.worker.AttoWorker
import cash.atto.commons.worker.cached
import cash.atto.commons.worker.remote
import cash.atto.commons.worker.retry
import cash.atto.wallet.model.AccountPreference
import cash.atto.wallet.model.AccountPreferenceStatus
import cash.atto.wallet.model.WorkPreference
import cash.atto.wallet.model.WorkSourcePreference
import cash.atto.wallet.state.AppState
import cash.atto.wallet.worker.createLocalWorker
import cash.atto.wallet.worker.isLocalWorkerSupported
import kotlin.time.Duration

internal class WalletSessionFactory(
    private val network: AttoNetwork,
    private val workCache: PersistentWorkCache,
    private val retryDelay: Duration,
) {
    suspend fun create(
        appState: AppState,
        accountPreferences: Map<String, AccountPreference>,
        work: WorkPreference,
    ): WalletSession? {
        if (appState.authState != AppState.AuthState.SESSION_VALID) return null
        val unlockedWallet = appState.unlockedWallet ?: return null
        if (!unlockedWallet.job.isActive) return null
        return unlockedWallet.run {
            val accountIndexes = accountPreferences.accountIndexes()
            val activeIndexes = accountPreferences.activeAccountIndexes()
            val gatekeeper = "https://gatekeeper.${network.name.lowercase()}.application.atto.cash"
            val client = TimedAttoNodeClient(AttoNodeClient.remote(gatekeeper), unlockedWallet)
            val workerDelegate =
                when {
                    work.source == WorkSourcePreference.LOCAL && isLocalWorkerSupported() -> createLocalWorker()
                    else -> AttoWorker.remote(gatekeeper)
                }
            val worker =
                PersistentWorkCachingWorker(
                    network = network,
                    workCache = workCache,
                    delegate = workerDelegate.retry(retryDelay).cached(),
                )
            try {
                val signerProvider: suspend (AttoKeyIndex) -> AttoSigner = unlockedWallet::signer
                val wallet = AttoWallet.create(client, worker, signerProvider)
                val walletAccounts =
                    retryWalletOperation("open wallet account", retryDelay) {
                        wallet.openAccount(activeIndexes)
                    }.associateBy { it.index }
                val addresses =
                    accountIndexes.associateWith { index ->
                        unlockedWallet.signer(index).address
                    }
                val accountMonitor = client.createAccountMonitor()
                accountMonitor.monitor(activeIndexes.mapNotNull(addresses::get))

                unlockedWallet.ensureActive()
                WalletSession(
                    client = client,
                    wallet = wallet,
                    walletAccounts = walletAccounts,
                    addresses = addresses,
                    accountPreferences = accountPreferences,
                    accountMonitor = accountMonitor,
                    worker = worker,
                    unlockedWallet = unlockedWallet,
                )
            } catch (error: Throwable) {
                worker.close()
                throw error
            }
        }
    }

    private fun Map<String, AccountPreference>.accountIndexes(): List<AttoKeyIndex> =
        keys
            .mapNotNull(String::toUIntOrNull)
            .sorted()
            .map(UInt::toAttoIndex)

    private fun Map<String, AccountPreference>.activeAccountIndexes(): List<AttoKeyIndex> =
        filterValues { it.status == AccountPreferenceStatus.ACTIVATED }
            .keys
            .mapNotNull(String::toUIntOrNull)
            .sorted()
            .map(UInt::toAttoIndex)
}
