package cash.atto.wallet.interactor

import cash.atto.commons.AttoAddress
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.AttoNetwork
import cash.atto.commons.gatekeeper.AttoAuthenticator
import cash.atto.commons.gatekeeper.attoBackend
import cash.atto.commons.toSigner
import cash.atto.commons.wallet.AttoNodeClient
import cash.atto.commons.wallet.AttoTransactionRepository
import cash.atto.commons.wallet.AttoWalletManager
import cash.atto.commons.wallet.AttoWalletViewer
import cash.atto.commons.wallet.AttoWorkCache
import cash.atto.commons.wallet.attoBackend
import cash.atto.commons.wallet.inMemory
import cash.atto.commons.worker.AttoWorker
import cash.atto.commons.worker.attoBackend
import cash.atto.wallet.state.AppState

class CreateWalletManagerInteractor() {
    fun invoke(state: AppState): AttoWalletManager? {
        if (state.privateKey == null)
            return null

        val signer = state.privateKey.toSigner()
        val authenticator = AttoAuthenticator.attoBackend(AttoNetwork.DEV, signer)
        val client = AttoNodeClient.attoBackend(AttoNetwork.DEV, authenticator)
        val transactionRepository = AttoTransactionRepository.inMemory() // TODO persist
        val viewer = AttoWalletViewer(signer.publicKey, client, transactionRepository)
        val worker = AttoWorker.attoBackend(authenticator)
        val workCache = AttoWorkCache.inMemory()
        val walletManager = AttoWalletManager(viewer, signer, client, worker, workCache) {
            AttoAddress(AttoAlgorithm.V1, signer.publicKey)
        }
        walletManager.start()
        return walletManager
    }
}