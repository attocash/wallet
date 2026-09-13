package cash.atto.wallet.repository

import cash.atto.commons.AttoMnemonic
import cash.atto.commons.AttoMnemonicException
import cash.atto.wallet.PlatformType
import cash.atto.wallet.datasource.PasswordDataSource
import cash.atto.wallet.datasource.SeedDataSource
import cash.atto.wallet.getPlatform
import cash.atto.wallet.interactor.SeedAESInteractor
import cash.atto.wallet.state.AppState.AuthState

internal interface WalletKeyStore {
    suspend fun authState(): AuthState

    suspend fun unlock(password: String): AttoMnemonic?

    suspend fun savePassword(
        phrase: String?,
        password: String,
    ): AttoMnemonic

    suspend fun clear()
}

internal class PlatformWalletKeyStore(
    private val seedDataSource: SeedDataSource,
    private val passwordDataSource: PasswordDataSource,
    private val seedAESInteractor: SeedAESInteractor,
) : WalletKeyStore {
    private val isWeb = getPlatform().type == PlatformType.WEB

    override suspend fun authState(): AuthState {
        val storedSeed = seedDataSource.getSeed() ?: return AuthState.NO_SEED
        return if (!isWeb && passwordDataSource.getPassword(storedSeed) == null) {
            AuthState.NO_PASSWORD
        } else {
            AuthState.SESSION_INVALID
        }
    }

    override suspend fun unlock(password: String): AttoMnemonic? {
        val storedSeed = seedDataSource.getSeed() ?: return null
        val phrase =
            if (isWeb) {
                seedAESInteractor.decryptSeed(storedSeed, password)
            } else {
                if (passwordDataSource.getPassword(storedSeed) != password) return null
                storedSeed
            }
        return try {
            AttoMnemonic.fromPhrase(phrase)
        } catch (_: AttoMnemonicException) {
            null
        }
    }

    override suspend fun savePassword(
        phrase: String?,
        password: String,
    ): AttoMnemonic {
        val pendingPhrase =
            phrase ?: run {
                check(!isWeb) { "No recovery phrase is awaiting a password" }
                val storedSeed = checkNotNull(seedDataSource.getSeed())
                check(passwordDataSource.getPassword(storedSeed) == null) { "Wallet is already password protected" }
                storedSeed
            }
        val mnemonic = AttoMnemonic.fromPhrase(pendingPhrase)
        if (isWeb) {
            seedDataSource.setSeed(seedAESInteractor.encryptSeed(pendingPhrase, password))
        } else {
            passwordDataSource.setPassword(pendingPhrase, password)
            seedDataSource.setSeed(pendingPhrase)
        }
        return mnemonic
    }

    override suspend fun clear() = seedDataSource.clearSeed()
}
