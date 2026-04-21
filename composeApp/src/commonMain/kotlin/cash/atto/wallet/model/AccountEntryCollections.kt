package cash.atto.wallet.model

import cash.atto.commons.AttoAccountEntry

fun mergeAccountEntries(
    current: List<AttoAccountEntry>,
    incoming: List<AttoAccountEntry>,
    limit: Int? = null,
): List<AttoAccountEntry> {
    val merged =
        (current + incoming)
            .distinctBy { it.hash }
            .sortedByDescending { it.height }

    return limit?.let(merged::take) ?: merged
}

fun mergeAccountEntries(
    current: List<AttoAccountEntry>,
    incoming: AttoAccountEntry,
    limit: Int? = null,
): List<AttoAccountEntry> = mergeAccountEntries(current, listOf(incoming), limit)
