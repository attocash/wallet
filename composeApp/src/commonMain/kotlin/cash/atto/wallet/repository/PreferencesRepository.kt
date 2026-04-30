package cash.atto.wallet.repository

import cash.atto.wallet.PlatformType
import cash.atto.wallet.datasource.PreferencesDataSource
import cash.atto.wallet.getPlatform
import cash.atto.wallet.interactor.SeedAESInteractor
import cash.atto.wallet.model.AccountPreferenceStatus
import cash.atto.wallet.model.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class PreferencesRepository(
    private val preferencesDataSource: PreferencesDataSource,
    private val appStateRepository: AppStateRepository,
    private val seedAESInteractor: SeedAESInteractor,
) {
    private val _state = MutableStateFlow(UserPreferences.EMPTY)
    val state = _state.asStateFlow()

    private val json =
        Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        }

    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        scope.launch {
            combine(
                preferencesDataSource.blob,
                appStateRepository.state,
            ) { storedPreferences, appState ->
                decodePreferences(
                    storedPreferences = storedPreferences,
                    password = appState.password,
                )
            }.collect { preferences ->
                _state.emit(preferences)
            }
        }
    }

    suspend fun saveAddressLabel(
        address: String,
        label: String,
    ) {
        savePreferences(
            state.value.withAddressLabel(
                value = address,
                label = label,
            ),
        )
    }

    suspend fun saveHashLabel(
        hash: String,
        label: String,
    ) {
        savePreferences(
            state.value.withHashLabel(
                value = hash,
                label = label,
            ),
        )
    }

    suspend fun setAccountStatus(
        index: UInt,
        status: AccountPreferenceStatus,
    ) {
        val preferences = state.value
        val isActivating = status == AccountPreferenceStatus.ACTIVATED
        val alreadyActive = preferences.accountStatus(index) == AccountPreferenceStatus.ACTIVATED
        if (isActivating && !alreadyActive && preferences.activeAccountIndexes().size >= UserPreferences.MAX_ACTIVE_ACCOUNT_COUNT) {
            error("Only ${UserPreferences.MAX_ACTIVE_ACCOUNT_COUNT} active accounts are supported.")
        }

        savePreferences(
            preferences.withAccountStatus(
                index = index,
                status = status,
            ),
        )
    }

    suspend fun addAccount(): UInt? {
        val preferences = state.value
        if (preferences.activeAccountIndexes().size >= UserPreferences.MAX_ACTIVE_ACCOUNT_COUNT) {
            return null
        }

        val nextIndex = preferences.nextAvailableAccountIndex() ?: return null
        savePreferences(preferences.withAccountStatus(nextIndex, AccountPreferenceStatus.ACTIVATED))
        return nextIndex
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

        savePreferences(importedPreferences)
    }

    private suspend fun savePreferences(preferences: UserPreferences) {
        val normalizedPreferences = preferences.normalized()
        _state.emit(normalizedPreferences)

        val rawJson = json.encodeToString(UserPreferences.serializer(), normalizedPreferences)
        val storedJson =
            if (getPlatform().type == PlatformType.WEB) {
                val password =
                    appStateRepository.state.value.password
                        ?: error("Password is required to store encrypted preferences on web.")
                seedAESInteractor.encryptSeed(
                    seed = rawJson,
                    password = password,
                )
            } else {
                rawJson
            }

        preferencesDataSource.setBlob(storedJson)
    }

    private suspend fun decodePreferences(
        storedPreferences: String?,
        password: String?,
    ): UserPreferences {
        if (storedPreferences.isNullOrBlank()) {
            return UserPreferences.EMPTY
        }

        val rawJson =
            if (getPlatform().type == PlatformType.WEB) {
                if (password.isNullOrBlank()) {
                    return UserPreferences.EMPTY
                }

                seedAESInteractor.decryptSeed(
                    encryptedSeed = storedPreferences,
                    password = password,
                )
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
