package cash.atto.wallet.repository

import cash.atto.commons.AttoMnemonic
import cash.atto.commons.AttoMnemonicException
import cash.atto.wallet.PlatformType
import cash.atto.wallet.datasource.PasswordDataSource
import cash.atto.wallet.datasource.PreferencesDataSource
import cash.atto.wallet.datasource.SeedDataSource
import cash.atto.wallet.datasource.WalletStorageSnapshot
import cash.atto.wallet.getPlatform
import cash.atto.wallet.interactor.DecryptedWalletData
import cash.atto.wallet.interactor.EncryptedDataPurpose
import cash.atto.wallet.interactor.SeedAESInteractor
import cash.atto.wallet.state.AppState.AuthState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

internal interface WalletKeyStore {
    suspend fun authState(): AuthState

    suspend fun unlock(password: String): AttoMnemonic?

    suspend fun savePassword(
        phrase: String?,
        password: String,
    ): AttoMnemonic

    suspend fun preparePasswordChange(
        mnemonic: AttoMnemonic,
        currentPassword: String,
        newPassword: String,
    ): suspend () -> Unit

    suspend fun clear()
}

internal class PlatformWalletKeyStore(
    private val seedDataSource: SeedDataSource,
    private val passwordDataSource: PasswordDataSource,
    private val seedAESInteractor: SeedAESInteractor,
    private val preferencesDataSource: PreferencesDataSource,
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
        val snapshot = if (isWeb) preferencesDataSource.walletSnapshot() else null
        val storedSeed = (if (isWeb) snapshot?.seed else seedDataSource.getSeed()) ?: return null
        val decrypted =
            if (isWeb) {
                seedAESInteractor.decryptSeed(storedSeed, password, EncryptedDataPurpose.SEED) ?: return null
            } else {
                if (passwordDataSource.getPassword(storedSeed) != password) return null
                DecryptedWalletData(storedSeed, needsMigration = false)
            }
        val mnemonic =
            try {
                AttoMnemonic.fromPhrase(decrypted.plaintext)
            } catch (_: AttoMnemonicException) {
                return null
            }
        if (isWeb) migrateEncryptedWallet(checkNotNull(snapshot), decrypted, password)
        return mnemonic
    }

    private suspend fun migrateEncryptedWallet(
        snapshot: WalletStorageSnapshot,
        seed: DecryptedWalletData,
        password: String,
    ) {
        try {
            val storedPreferences = snapshot.blob
            val preferences =
                if (storedPreferences.isNullOrBlank()) {
                    null
                } else {
                    // Logout can leave preferences belonging to a previous password. Preserve unreadable records.
                    seedAESInteractor.decryptSeed(storedPreferences, password, EncryptedDataPurpose.PREFERENCES)
                }
            val updatedPreferences =
                if (preferences?.needsMigration == true) {
                    seedAESInteractor.encryptSeed(preferences.plaintext, password, EncryptedDataPurpose.PREFERENCES)
                } else {
                    null
                }
            val updatedSeed =
                if (seed.needsMigration) {
                    seedAESInteractor.encryptSeed(seed.plaintext, password, EncryptedDataPurpose.SEED)
                } else {
                    null
                }

            currentCoroutineContext().ensureActive()
            preferencesDataSource.migrateEncryptedWallet(
                seed = updatedSeed ?: checkNotNull(snapshot.seed),
                blob = updatedPreferences ?: storedPreferences,
                expected = snapshot,
            )
        } catch (error: CancellationException) {
            throw error
        } catch (error: WalletStorageException) {
            throw error
        } catch (error: Throwable) {
            throw WalletStorageException("Could not update wallet storage. Please try again.", error)
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
            seedDataSource.setSeed(seedAESInteractor.encryptSeed(pendingPhrase, password, EncryptedDataPurpose.SEED))
        } else {
            passwordDataSource.setPassword(pendingPhrase, password)
            seedDataSource.setSeed(pendingPhrase)
        }
        return mnemonic
    }

    override suspend fun preparePasswordChange(
        mnemonic: AttoMnemonic,
        currentPassword: String,
        newPassword: String,
    ): suspend () -> Unit {
        val phrase = mnemonic.words.joinToString(" ")
        if (!isWeb) return { passwordDataSource.setPassword(phrase, newPassword) }

        val snapshot = preferencesDataSource.walletSnapshot()
        val storedSeed = snapshot.seed ?: throw WalletStorageException("Wallet data changed. Unlock the wallet again before changing its password.")
        val persistedPhrase = seedAESInteractor.decryptSeed(storedSeed, currentPassword, EncryptedDataPurpose.SEED)?.plaintext
        val persistedMnemonic =
            try {
                persistedPhrase?.let { AttoMnemonic.fromPhrase(it) }
            } catch (_: AttoMnemonicException) {
                null
            }
        if (persistedMnemonic != mnemonic) {
            throw WalletStorageException("Wallet data changed. Unlock the wallet again before changing its password.")
        }
        val storedPreferences = snapshot.blob
        val plaintextPreferences =
            if (storedPreferences.isNullOrBlank()) {
                "{}"
            } else {
                seedAESInteractor.decryptSeed(storedPreferences, currentPassword, EncryptedDataPurpose.PREFERENCES)?.plaintext
                    ?: throw WalletStorageException("Could not read wallet preferences. Your password has not changed.")
            }
        val encryptedSeed = seedAESInteractor.encryptSeed(phrase, newPassword, EncryptedDataPurpose.SEED)
        val encryptedPreferences = seedAESInteractor.encryptSeed(plaintextPreferences, newPassword, EncryptedDataPurpose.PREFERENCES)
        currentCoroutineContext().ensureActive()
        return { preferencesDataSource.replaceEncryptedWallet(encryptedSeed, encryptedPreferences, snapshot) }
    }

    override suspend fun clear() = seedDataSource.clearSeed()
}
