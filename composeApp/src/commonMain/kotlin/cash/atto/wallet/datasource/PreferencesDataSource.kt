package cash.atto.wallet.datasource

import kotlinx.coroutines.flow.Flow

expect class PreferencesDataSource {
    val blob: Flow<String?>
    val termsAndConditionsDate: Flow<String?>
    val work: Flow<String?>

    suspend fun setBlob(blob: String)

    suspend fun setTermsAndConditionsDate(date: String)

    suspend fun clearTermsAndConditionsDate()

    suspend fun setWork(work: String)

    internal suspend fun walletSnapshot(): WalletStorageSnapshot

    internal suspend fun setWalletBlob(blob: String, expected: WalletStorageSnapshot)

    internal suspend fun migrateEncryptedWallet(seed: String, blob: String?, expected: WalletStorageSnapshot)

    internal suspend fun replaceEncryptedWallet(seed: String, blob: String, expected: WalletStorageSnapshot)

    suspend fun migrateStorage()
}
