package cash.atto.wallet.repository

import cash.atto.commons.AttoBlockType
import cash.atto.commons.AttoPublicKey
import cash.atto.wallet.platform.exportCsv
import cash.atto.wallet.support.testAccountEntry
import cash.atto.wallet.uistate.overview.TransactionType
import kotlinx.coroutines.test.runTest
import kotlinx.io.Buffer
import kotlinx.io.readString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransactionHistoryCsvExportTest {
    @Test
    fun `export pages all history in descending order while filtering transaction types`() =
        runTest {
            // Given
            val dao = AccountEntryTestDao()
            val repository = PersistentAccountEntryRepository(dao)
            val entries =
                (1UL..503UL).map { height ->
                    testAccountEntry(
                        height,
                        if (height % 2UL == 0UL) AttoBlockType.SEND else AttoBlockType.RECEIVE,
                        "2",
                        if (height % 2UL == 0UL) "1" else "3",
                    )
                }
            entries.forEach { repository.save(it) }
            val sink = Buffer()

            // When
            repository.exportCsv(entries.first().publicKey, setOf(TransactionType.RECEIVE), sink = sink)

            // Then
            val rows = sink.readString().trimEnd().lines()
            assertEquals(HEADER, rows.first())
            val data = rows.drop(1).map { it.removeSurrounding("\"").split("\",\"") }
            val expected = entries.reversed().filter { it.blockType == AttoBlockType.RECEIVE }
            assertEquals(expected.map { it.height.toString() }, data.map { it[5] })
            assertEquals(expected.map { it.hash.toString() }, data.map { it[6] })
            assertTrue(data.all { it[0] == "RECEIVE" })
            assertEquals(listOf(null to 500, 4L to 500), dao.pageRequests)
        }

    @Test
    fun `export forwards address voter and hash resolvers and escapes subject labels`() =
        runTest {
            // Given
            val repository = PersistentAccountEntryRepository(AccountEntryTestDao())
            val send = testAccountEntry(2UL, AttoBlockType.SEND, "2", "1")
            val change = testAccountEntry(1UL, AttoBlockType.CHANGE, "1", "1")
            repository.save(send)
            repository.save(change)
            val addresses = mutableListOf<String>()
            val voters = mutableListOf<String>()
            val hashes = mutableListOf<String>()
            val sink = Buffer()

            // When
            repository.exportCsv(
                publicKey = send.publicKey,
                selectedTypes = setOf(TransactionType.SEND, TransactionType.CHANGE),
                addressLabelResolver = {
                    addresses.add(it)
                    "Savings, \"joint\""
                },
                voterLabelResolver = {
                    voters.add(it)
                    "Representative"
                },
                hashLabelResolver = {
                    hashes.add(it)
                    "Transfer"
                },
                sink = sink,
            )

            // Then
            val rows = sink.readString().trimEnd().lines()
            assertEquals(listOf(send.subjectAddress.toString()), addresses)
            assertEquals(listOf(change.subjectAddress.toString()), voters)
            assertEquals(listOf(send.hash.toString(), change.hash.toString()), hashes)
            assertTrue(rows[1].contains("\"Savings, \"\"joint\"\"\""))
            assertTrue(rows[2].contains("\"Representative\""))
            assertEquals(3, rows.size)
        }

    @Test
    fun `empty history exports only the CSV header`() =
        runTest {
            // Given
            val repository = PersistentAccountEntryRepository(AccountEntryTestDao())
            val sink = Buffer()

            // When
            repository.exportCsv(AttoPublicKey(ByteArray(32)), TransactionType.entries.toSet(), sink = sink)

            // Then
            assertEquals("$HEADER\n", sink.readString())
        }

    private companion object {
        const val HEADER = "\"type\",\"amount\",\"subject\",\"subject_label\",\"timestamp\",\"height\",\"hash\""
    }
}
