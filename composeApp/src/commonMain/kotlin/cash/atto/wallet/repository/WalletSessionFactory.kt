package cash.atto.wallet.repository

import cash.atto.commons.AttoKeyIndex
import cash.atto.commons.AttoNetwork
import cash.atto.commons.gatekeeper.AttoAuthenticator
import cash.atto.commons.gatekeeper.attoBackend
import cash.atto.commons.node.AttoNodeClient
import cash.atto.commons.node.monitor.createAccountMonitor
import cash.atto.commons.toAttoIndex
import cash.atto.commons.toSigner
import cash.atto.commons.wallet.AttoWallet
import cash.atto.commons.wallet.create
import cash.atto.commons.worker.AttoWorker
import cash.atto.commons.worker.cached
import cash.atto.commons.worker.retry
import cash.atto.wallet.model.AccountPreference
import cash.atto.wallet.model.AccountPreferenceStatus
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
        workSource: WorkSourcePreference,
        signerIndex: AttoKeyIndex,
    ): WalletSession? {
        val seed = appState.getSeed() ?: return null
        val accountIndexes = accountPreferences.accountIndexes()
        val activeIndexes = accountPreferences.activeAccountIndexes()
        val signer = seed.toSigner(signerIndex)
        val authenticator = AttoAuthenticator.attoBackend(network, signer)
        val client = AttoNodeClient.attoBackend(network, authenticator)
        val workerDelegate =
            when {
                workSource == WorkSourcePreference.LOCAL && isLocalWorkerSupported() -> createLocalWorker()
                else -> AttoWorker.attoBackend(network, authenticator)
            }
        val worker =
            PersistentWorkCachingWorker(
                network = network,
                workCache = workCache,
                delegate = workerDelegate.retry(retryDelay).cached(),
            )
        val wallet = AttoWallet.create(client, worker, seed)
        val walletAccounts =
            retryWalletOperation("open wallet account", retryDelay) {
                wallet.openAccount(activeIndexes)
            }.associateBy { it.index }
        val addresses =
            accountIndexes.associateWith { index ->
                seed.toSigner(index).address
            }
        val accountMonitor = client.createAccountMonitor()
        accountMonitor.monitor(activeIndexes.mapNotNull(addresses::get))

        return WalletSession(
            client = client,
            wallet = wallet,
            walletAccounts = walletAccounts,
            addresses = addresses,
            accountPreferences = accountPreferences,
            accountMonitor = accountMonitor,
            worker = worker,
        )
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
