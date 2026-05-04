package cash.atto.wallet.repository

import cash.atto.commons.AttoAccount
import cash.atto.commons.AttoAddress
import cash.atto.commons.AttoAmount
import cash.atto.commons.AttoHeight
import cash.atto.commons.AttoInstant
import cash.atto.commons.AttoKeyIndex
import cash.atto.commons.AttoPublicKey
import cash.atto.commons.AttoReceivable
import cash.atto.commons.AttoSendBlock
import cash.atto.commons.node.AttoNodeClient
import cash.atto.commons.node.monitor.AttoAccountMonitor
import cash.atto.commons.node.monitor.toAccountEntryMonitor
import cash.atto.commons.toAttoAmount
import cash.atto.commons.toAttoIndex
import cash.atto.commons.wallet.AttoWallet
import cash.atto.commons.wallet.AttoWalletAccount
import cash.atto.wallet.model.AccountPreference
import cash.atto.wallet.model.AccountPreferenceStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

internal class WalletSession(
    private val client: AttoNodeClient,
    private val wallet: AttoWallet,
    private val walletAccounts: Map<AttoKeyIndex, AttoWalletAccount>,
    private val addresses: Map<AttoKeyIndex, AttoAddress>,
    private val accountPreferences: Map<String, AccountPreference>,
    private val accountMonitor: AttoAccountMonitor,
    private val worker: PersistentWorkCachingWorker,
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val walletAccountsByPublicKey = walletAccounts.values.associateBy { it.address.publicKey }

    fun launch(block: suspend CoroutineScope.() -> Unit): Job = scope.launch(block = block)

    fun walletAccount(index: AttoKeyIndex): AttoWalletAccount? = walletAccounts[index]

    fun address(index: AttoKeyIndex): AttoAddress? = addresses[index]

    fun publicKey(index: AttoKeyIndex): AttoPublicKey? = address(index)?.publicKey

    fun account(index: AttoKeyIndex): AttoAccount? = walletAccount(index)?.account

    suspend fun nodeTimeDifference(currentTime: AttoInstant): Long = client.now(currentTime).differenceMillis

    fun isActive(index: AttoKeyIndex): Boolean = accountPreferences[index.value.toString()]?.status == AccountPreferenceStatus.ACTIVATED

    fun activeIndexes(): List<AttoKeyIndex> =
        accountPreferences
            .filterValues { it.status == AccountPreferenceStatus.ACTIVATED }
            .keys
            .mapNotNull(String::toUIntOrNull)
            .sorted()
            .map(UInt::toAttoIndex)

    fun accountStates(): List<WalletAccountState> =
        addresses
            .toList()
            .sortedBy { it.first.value }
            .map { (index, address) ->
                WalletAccountState(
                    index = index,
                    address = address,
                    account = account(index),
                    status = accountPreferences[index.value.toString()]?.status ?: AccountPreferenceStatus.DEACTIVATED,
                )
            }

    fun updateAccount(account: AttoAccount) {
        walletAccountsByPublicKey[account.publicKey]?.account = account
    }

    fun isAccount(
        index: AttoKeyIndex,
        account: AttoAccount,
    ): Boolean = account.publicKey == publicKey(index)

    suspend fun send(
        index: AttoKeyIndex,
        receiverAddress: AttoAddress,
        amount: AttoAmount,
        timestampProvider: suspend () -> AttoInstant,
    ): AttoSendBlock {
        val walletAccount = walletAccount(index) ?: throw IllegalStateException("Wallet is not ready yet")
        val account = walletAccount.account ?: throw IllegalStateException("Account is not open yet")

        require(receiverAddress.publicKey != walletAccount.address.publicKey) { "You can't send $amount to yourself" }
        if (amount > account.balance) {
            throw IllegalStateException("${account.balance} balance is not enough to send $amount")
        }

        val timestamp = timestampProvider()
        val transaction =
            wallet.send(
                index = index,
                receiverAddress = receiverAddress,
                amount = amount,
                timestamp = timestamp,
            )

        return transaction.block as? AttoSendBlock
            ?: throw IllegalStateException("Expected send block but received ${transaction.block::class}")
    }

    suspend fun changeRepresentative(
        index: AttoKeyIndex,
        representative: AttoAddress,
    ) {
        wallet.change(index, representative)
    }

    suspend fun receive(
        receivable: AttoReceivable,
        representative: AttoAddress,
    ) {
        wallet.receive(receivable, representative)
    }

    suspend fun prepareReceiveWork(receivable: AttoReceivable): Boolean {
        val walletAccount = walletAccountsByPublicKey[receivable.receiverPublicKey] ?: return false
        return worker.prepareReceiveWork(receivable, walletAccount.account)
    }

    suspend fun cacheNextWorkForOpenedAccounts() {
        walletAccounts.values.mapNotNull { it.account }.forEach { account ->
            cacheNextWork(account)
        }
    }

    suspend fun cacheNextWork(account: AttoAccount) {
        worker.cacheNextWork(account)
    }

    fun accountStream() = accountMonitor.accountStream()

    fun accountEntryStream(nextHeight: suspend (AttoAddress) -> AttoHeight) = accountMonitor.toAccountEntryMonitor(nextHeight).stream()

    fun receivableStream() = accountMonitor.receivableStream(minAmount = 1UL.toAttoAmount())

    fun close() {
        scope.cancel()
        worker.close()
    }
}
