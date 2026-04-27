package cash.atto.wallet.model

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val addresses: List<LabeledPreferenceEntry> = emptyList(),
    val hashes: List<LabeledPreferenceEntry> = emptyList(),
) {
    fun addressLabel(value: String): String? = addresses.firstOrNull { it.value == value }?.label

    fun hashLabel(value: String): String? = hashes.firstOrNull { it.value == value }?.label

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

    companion object {
        val EMPTY = UserPreferences()
    }
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
