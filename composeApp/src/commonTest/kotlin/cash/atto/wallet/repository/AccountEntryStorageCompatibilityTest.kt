package cash.atto.wallet.repository

import cash.atto.commons.AttoAccountEntry
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.AttoAmount
import cash.atto.commons.AttoBlockType
import cash.atto.commons.AttoHeight
import cash.atto.commons.AttoInstant
import cash.atto.commons.AttoPublicKey
import cash.atto.commons.toAddress
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class AccountEntryStorageCompatibilityTest {
    @Test
    fun `persisted Commons 8 account entries retain addresses and exact values after upgrading`() {
        // Given: Commons 8 persisted split address fields and numeric 64-bit values.
        val storedEntry =
            """
            {
                "hash":"68BA42CDD87328380BE32D5AA6DBB86E905B50273D37AF1DE12F47B83A001154",
                "algorithm":"V1",
                "publicKey":"FD595851104FDDB2FEBF3739C8006C8AAE9B8A2B1BC390D5FDF07EBDD8583FA1",
                "height":9007199254740993,
                "blockType":"RECEIVE",
                "subjectAlgorithm":"V1",
                "subjectPublicKey":"2EB21717813E7A0E0A7E308B8E2FD8A051F8724F5C5F0047E92E19310C582E3A",
                "previousBalance":0,
                "balance":1347229065083333337,
                "timestamp":1704616009211
            }
            """.trimIndent()

        // When: use the same plain Json codec as PersistentAccountEntryRepository.
        val entry = Json.decodeFromString<AttoAccountEntry>(storedEntry)
        val reloaded = Json.decodeFromString<AttoAccountEntry>(Json.encodeToString(entry))

        // Then
        assertEquals(
            AttoPublicKey.parse("FD595851104FDDB2FEBF3739C8006C8AAE9B8A2B1BC390D5FDF07EBDD8583FA1").toAddress(AttoAlgorithm.V1),
            entry.address,
        )
        assertEquals(
            AttoPublicKey.parse("2EB21717813E7A0E0A7E308B8E2FD8A051F8724F5C5F0047E92E19310C582E3A").toAddress(AttoAlgorithm.V1),
            entry.subjectAddress,
        )
        assertEquals(AttoHeight(9007199254740993UL), entry.height)
        assertEquals(AttoBlockType.RECEIVE, entry.blockType)
        assertEquals(AttoAmount.MIN, entry.previousBalance)
        assertEquals(AttoAmount(1347229065083333337UL), entry.balance)
        assertEquals(AttoInstant.fromEpochMilliseconds(1704616009211), entry.timestamp)
        assertEquals(entry, reloaded)
    }
}
