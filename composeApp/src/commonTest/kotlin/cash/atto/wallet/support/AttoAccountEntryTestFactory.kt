package cash.atto.wallet.support

import cash.atto.commons.AttoAccountEntry
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.AttoAmount
import cash.atto.commons.AttoBlockType
import cash.atto.commons.AttoHash
import cash.atto.commons.AttoHeight
import cash.atto.commons.AttoInstant
import cash.atto.commons.AttoPublicKey
import cash.atto.commons.AttoUnit
import cash.atto.commons.toAddress

private const val ACCOUNT_PUBLIC_KEY_HEX = "1111111111111111111111111111111111111111111111111111111111111111"
private const val SUBJECT_PUBLIC_KEY_HEX = "2222222222222222222222222222222222222222222222222222222222222222"

fun testAccountEntry(
    height: ULong,
    blockType: AttoBlockType,
    previousBalanceAtto: String,
    balanceAtto: String,
    hashHex: String = height.toString(16).padStart(64, '0'),
): AttoAccountEntry =
    AttoAccountEntry(
        hash = AttoHash.parse(hashHex),
        address = AttoPublicKey.parse(ACCOUNT_PUBLIC_KEY_HEX).toAddress(AttoAlgorithm.V1),
        height = AttoHeight(height),
        blockType = blockType,
        subjectAddress = AttoPublicKey.parse(SUBJECT_PUBLIC_KEY_HEX).toAddress(AttoAlgorithm.V1),
        previousBalance = AttoAmount.from(AttoUnit.ATTO, previousBalanceAtto),
        balance = AttoAmount.from(AttoUnit.ATTO, balanceAtto),
        timestamp = AttoInstant.fromEpochMilliseconds(height.toLong() * 1_000L),
    )
