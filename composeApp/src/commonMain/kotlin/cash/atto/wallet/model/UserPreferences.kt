package cash.atto.wallet.model

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val addresses: List<LabeledPreferenceEntry> = emptyList(),
    val hashes: List<LabeledPreferenceEntry> = emptyList(),
    val accounts: Map<String, AccountPreference> = defaultAccountPreferences(),
) {
    fun addressLabel(value: String): String? = addresses.firstOrNull { it.value == value }?.label

    fun hashLabel(value: String): String? = hashes.firstOrNull { it.value == value }?.label

    fun accountStatus(index: UInt): AccountPreferenceStatus =
        normalizedAccounts()[index.toString()]?.status ?: AccountPreferenceStatus.DEACTIVATED

    fun activeAccountIndexes(): List<UInt> =
        normalizedAccounts()
            .filterValues { it.status == AccountPreferenceStatus.ACTIVATED }
            .keys
            .mapNotNull(String::toUIntOrNull)
            .sorted()

    fun knownAccountIndexes(): List<UInt> =
        normalizedAccounts()
            .keys
            .mapNotNull(String::toUIntOrNull)
            .sorted()

    fun withAddressLabel(
        value: String,
        label: String,
    ): UserPreferences =
        copy(
            addresses = addresses.updated(value = value, label = label),
        )

    fun withHashLabel(
        value: String,
        label: String,
    ): UserPreferences =
        copy(
            hashes = hashes.updated(value = value, label = label),
        )

    fun withAccountStatus(
        index: UInt,
        status: AccountPreferenceStatus,
    ): UserPreferences {
        val currentAccounts =
            if (accounts.isEmpty()) {
                emptyMap()
            } else {
                normalizedAccounts()
            }
        val updatedAccounts =
            currentAccounts + (
                index.toString() to
                    AccountPreference(
                        status = status,
                    )
            )

        return copy(accounts = updatedAccounts.normalizedAccountPreferences())
    }

    fun nextAvailableAccountIndex(): UInt? {
        val knownIndexes = knownAccountIndexes().toSet()
        var index = 0U
        while (index in knownIndexes) {
            if (index == UInt.MAX_VALUE) {
                return null
            }
            index++
        }

        return index
    }

    fun normalizedAccounts(): Map<String, AccountPreference> = accounts.normalizedAccountPreferences()

    companion object {
        const val MAX_ACTIVE_ACCOUNT_COUNT = 5
        val EMPTY = UserPreferences()
    }
}

@Serializable
data class AccountPreference(
    val status: AccountPreferenceStatus = AccountPreferenceStatus.ACTIVATED,
)

fun defaultAccountName(index: UInt): String = "Account #$index"

@Serializable
enum class AccountPreferenceStatus {
    ACTIVATED,
    DEACTIVATED,
}

@Serializable
data class LabeledPreferenceEntry(
    val label: String,
    val value: String,
)

private fun List<LabeledPreferenceEntry>.updated(
    value: String,
    label: String,
): List<LabeledPreferenceEntry> {
    val normalizedValue = value.trim()
    if (normalizedValue.isEmpty()) {
        return this
    }

    val normalizedLabel = label.trim()
    val filtered = filterNot { it.value == normalizedValue }

    return if (normalizedLabel.isEmpty()) {
        filtered
    } else {
        (filtered + LabeledPreferenceEntry(label = normalizedLabel, value = normalizedValue))
            .sortedBy { it.value }
    }
}

private fun defaultAccountPreferences(): Map<String, AccountPreference> =
    mapOf(
        "0" to AccountPreference(AccountPreferenceStatus.ACTIVATED),
    )

private fun Map<String, AccountPreference>.normalizedAccountPreferences(): Map<String, AccountPreference> {
    val normalized =
        entries
            .mapNotNull { (key, preference) ->
                val index = key.toUIntOrNull() ?: return@mapNotNull null
                index to preference
            }.sortedBy { it.first }
            .toMap()

    if (normalized.isEmpty()) {
        return defaultAccountPreferences()
    }

    var activeCount = 0
    val limitedActiveAccounts =
        normalized.mapValues { (_, preference) ->
            if (preference.status == AccountPreferenceStatus.DEACTIVATED) {
                return@mapValues preference
            }

            activeCount++
            if (activeCount <= UserPreferences.MAX_ACTIVE_ACCOUNT_COUNT) {
                preference
            } else {
                AccountPreference(AccountPreferenceStatus.DEACTIVATED)
            }
        }

    val withActiveAccount =
        if (limitedActiveAccounts.values.any { it.status == AccountPreferenceStatus.ACTIVATED }) {
            limitedActiveAccounts
        } else {
            limitedActiveAccounts + (0U to AccountPreference(AccountPreferenceStatus.ACTIVATED))
        }

    return withActiveAccount
        .toList()
        .associate { (index, preference) -> index.toString() to preference }
}
