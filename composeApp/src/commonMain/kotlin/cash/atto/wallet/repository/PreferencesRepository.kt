package cash.atto.wallet.repository

import cash.atto.wallet.PlatformType
import cash.atto.wallet.datasource.PreferencesDataSource
import cash.atto.wallet.getPlatform
import cash.atto.wallet.interactor.EncryptedDataPurpose
import cash.atto.wallet.interactor.SeedAESInteractor
import cash.atto.wallet.model.AccountPreferenceStatus
import cash.atto.wallet.model.UserPreferences
import cash.atto.wallet.model.WorkPreference
import cash.atto.wallet.model.WorkSourcePreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class PreferencesRepository(
    private val preferencesDataSource: PreferencesDataSource,
    private val appStateRepository: AppStateRepository,
    private val seedAESInteractor: SeedAESInteractor,
) {
    private val _state = MutableStateFlow(UserPreferences.EMPTY)
    val state = _state.asStateFlow()
    private val _work = MutableStateFlow(WorkPreference())
    val work = _work.asStateFlow()
    private val _workSource = MutableStateFlow(_work.value.source)
    val workSource = _workSource.asStateFlow()

    private val json =
        Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        }
    private val workJson =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        scope.launch {
            combine(
                preferencesDataSource.blob,
                appStateRepository.state,
            ) { _, _ -> Unit }.collect {
                appStateRepository.withWalletStorage { appState ->
                    // Re-read both values inside the same ordering as rotation and preference writes.
                    _state.emit(decodePreferences(preferencesDataSource.blob.first(), appState.password))
                }
            }
        }

        scope.launch {
            preferencesDataSource.work.collect { storedWork ->
                val work = decodeWorkPreference(storedWork).normalized()
                _work.emit(work)
                _workSource.emit(work.source)
            }
        }
    }

    suspend fun saveAddressLabel(
        address: String,
        label: String,
    ) {
        updatePreferences { it.withAddressLabel(value = address, label = label) }
    }

    suspend fun saveHashLabel(
        hash: String,
        label: String,
    ) {
        updatePreferences { it.withHashLabel(value = hash, label = label) }
    }

    suspend fun setAccountStatus(
        index: UInt,
        status: AccountPreferenceStatus,
    ) {
        updatePreferences { preferences ->
            val isActivating = status == AccountPreferenceStatus.ACTIVATED
            val alreadyActive = preferences.accountStatus(index) == AccountPreferenceStatus.ACTIVATED
            if (isActivating && !alreadyActive && preferences.activeAccountIndexes().size >= UserPreferences.MAX_ACTIVE_ACCOUNT_COUNT) {
                error("Only ${UserPreferences.MAX_ACTIVE_ACCOUNT_COUNT} active accounts are supported.")
            }
            preferences.withAccountStatus(index = index, status = status)
        }
    }

    suspend fun addAccount(): UInt? {
        var addedIndex: UInt? = null
        updatePreferences { preferences ->
            if (preferences.activeAccountIndexes().size >= UserPreferences.MAX_ACTIVE_ACCOUNT_COUNT) {
                preferences
            } else {
                val nextIndex = preferences.nextAvailableAccountIndex()
                addedIndex = nextIndex
                if (nextIndex == null) preferences else preferences.withAccountStatus(nextIndex, AccountPreferenceStatus.ACTIVATED)
            }
        }
        return addedIndex
    }

    suspend fun setWorkSource(workSource: WorkSourcePreference) {
        val work = WorkPreference.forSource(workSource)
        _work.emit(work)
        _workSource.emit(work.source)
        val storedWork =
            workJson.encodeToString(
                WorkPreference.serializer(),
                work,
            )
        preferencesDataSource.setWork(storedWork)
    }

    fun getAddressLabel(address: String): String? = state.value.addressLabel(address)

    fun getHashLabel(hash: String): String? = state.value.hashLabel(hash)

    fun exportPlainJson(): String = json.encodeToString(UserPreferences.serializer(), state.value)

    suspend fun importPlainJson(rawJson: String) {
        val importedPreferences =
            runCatching {
                json.decodeFromString(UserPreferences.serializer(), rawJson)
            }.getOrElse {
                error("Invalid preferences JSON.")
            }.normalized()

        updatePreferences { importedPreferences }
    }

    private suspend fun updatePreferences(update: (UserPreferences) -> UserPreferences) {
        appStateRepository.withWalletStorage { appState ->
            val wallet = appState.unlockedWallet ?: throw WalletStorageException("Unlock the wallet before changing its preferences.")
            wallet.ensureActive()
            val snapshot = preferencesDataSource.walletSnapshot()
            if (getPlatform().type == PlatformType.WEB) {
                val password = checkNotNull(wallet.password)
                val seed = snapshot.seed
                val persistedPhrase = seed?.let { seedAESInteractor.decryptSeed(it, password, EncryptedDataPurpose.SEED)?.plaintext }
                if (persistedPhrase == null || persistedPhrase != wallet.mnemonic?.phrase) {
                    throw WalletStorageException("Wallet data changed in another tab. Lock and unlock this wallet, then try again.")
                }
            }
            val currentPreferences = decodePreferences(snapshot.blob, appState.password, requireReadable = true)
            val normalizedPreferences = update(currentPreferences).normalized()
            val rawJson = json.encodeToString(UserPreferences.serializer(), normalizedPreferences)
            val storedJson =
                if (getPlatform().type == PlatformType.WEB) {
                    val password = appState.password ?: error("Password is required to store encrypted preferences on web.")
                    seedAESInteractor.encryptSeed(
                        seed = rawJson,
                        password = password,
                        purpose = EncryptedDataPurpose.PREFERENCES,
                    )
                } else {
                    rawJson
                }
            preferencesDataSource.setWalletBlob(storedJson, snapshot)
            _state.emit(normalizedPreferences)
        }
    }

    private suspend fun decodePreferences(
        storedPreferences: String?,
        password: String?,
        requireReadable: Boolean = false,
    ): UserPreferences {
        if (storedPreferences.isNullOrBlank()) {
            return UserPreferences.EMPTY
        }

        val rawJson =
            if (getPlatform().type == PlatformType.WEB) {
                if (password.isNullOrBlank()) {
                    return UserPreferences.EMPTY
                }

                val decrypted = seedAESInteractor
                    .decryptSeed(
                        encryptedSeed = storedPreferences,
                        password = password,
                        purpose = EncryptedDataPurpose.PREFERENCES,
                    )?.plaintext
                if (decrypted == null && requireReadable) {
                    throw WalletStorageException("Could not read wallet preferences. Lock and unlock this wallet, then try again.")
                }
                decrypted.orEmpty()
            } else {
                storedPreferences
            }

        if (rawJson.isBlank()) {
            return UserPreferences.EMPTY
        }

        return runCatching {
            json.decodeFromString(UserPreferences.serializer(), rawJson)
        }.getOrElse {
            UserPreferences.EMPTY
        }.normalized()
    }

    private fun decodeWorkPreference(storedWork: String?): WorkPreference {
        if (storedWork.isNullOrBlank()) {
            return WorkPreference()
        }

        return runCatching {
            workJson.decodeFromString(WorkPreference.serializer(), storedWork)
        }.getOrElse {
            WorkPreference()
        }
    }
}

private fun UserPreferences.normalized(): UserPreferences {
    val emptyPreferences = UserPreferences(accounts = emptyMap())
    val normalizedAddresses =
        addresses.fold(emptyPreferences) { preferences, entry ->
            preferences.withAddressLabel(
                value = entry.value,
                label = entry.label,
            )
        }

    val normalizedHashes =
        hashes.fold(normalizedAddresses) { preferences, entry ->
            preferences.withHashLabel(
                value = entry.value,
                label = entry.label,
            )
        }

    return normalizedHashes.copy(
        accounts = copy(accounts = accounts).normalizedAccounts(),
    )
}
