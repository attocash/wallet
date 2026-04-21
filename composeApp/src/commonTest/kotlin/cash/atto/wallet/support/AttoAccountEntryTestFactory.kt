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
        algorithm = AttoAlgorithm.V1,
        publicKey = AttoPublicKey.parse(ACCOUNT_PUBLIC_KEY_HEX),
        height = AttoHeight(height),
        blockType = blockType,
        subjectAlgorithm = AttoAlgorithm.V1,
        subjectPublicKey = AttoPublicKey.parse(SUBJECT_PUBLIC_KEY_HEX),
        previousBalance = AttoAmount.from(AttoUnit.ATTO, previousBalanceAtto),
        balance = AttoAmount.from(AttoUnit.ATTO, balanceAtto),
        timestamp = AttoInstant.fromEpochMilliseconds(height.toLong() * 1_000L),
    )
