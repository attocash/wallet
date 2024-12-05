package cash.atto.wallet.repository

import cash.atto.commons.AttoBlock
import cash.atto.commons.AttoPublicKey
import cash.atto.commons.AttoTransaction
import cash.atto.commons.readAttoSignature
import cash.atto.commons.readAttoWork
import cash.atto.commons.wallet.AttoTransactionRepository
import cash.atto.wallet.datasource.AppDatabase
import cash.atto.wallet.datasource.Transaction
import kotlinx.io.Buffer
import kotlinx.io.readByteArray

class PersistentTransactionRepository(
    appDatabase: AppDatabase
) : AttoTransactionRepository {

    private val dao = appDatabase.transactionDao()

    override suspend fun last(publicKey: AttoPublicKey): AttoTransaction? {
        return dao.last(publicKey.value)
            ?.let {
                AttoTransaction.fromBuffer(
                    Buffer().apply {
                        write(
                            source = it,
                            startIndex = 0,
                            endIndex = it.size
                        )
                    }
                )
            }
    }

    override suspend fun list(publicKey: AttoPublicKey): List<AttoTransaction> {
        return dao.list(publicKey.value)
            .mapNotNull {
                AttoTransaction.fromBuffer(
                    Buffer().apply {
                        write(
                            source = it,
                            startIndex = 0,
                            endIndex = it.size
                        )
                    }
                )
            }
    }

    override suspend fun save(transaction: AttoTransaction) {
        val publicKey = transaction.block.publicKey

        dao.save(Transaction(
            publicKey = publicKey.value,
            transaction = transaction.toBuffer().readByteArray()
        ))
    }

    suspend fun clear() {}
}