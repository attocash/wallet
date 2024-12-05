package cash.atto.wallet.datasource

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import cash.atto.commons.AttoPublicKey
import cash.atto.commons.AttoTransaction
import cash.atto.commons.AttoWork

@Database(
    entities = [Transaction::class, Work::class],
    version = 2
)
abstract class AppDatabase : RoomDatabase(), DB {
    abstract fun transactionDao(): TransactionDao
    abstract fun workDao(): WorkDao
    override fun clearAllTables(): Unit {}
}

interface DB {
    fun clearAllTables(): Unit {}
}

@Dao
interface TransactionDao {
    @Query(
        "SELECT _transaction from transactions " +
        "WHERE publicKey = :publicKey " +
        "ORDER BY id DESC LIMIT 1"
    )
    suspend fun last(publicKey: ByteArray): ByteArray?

    @Query(
        "SELECT _transaction from transactions " +
        "WHERE publicKey = :publicKey " +
        "ORDER BY id DESC"
    )
    suspend fun list(publicKey: ByteArray): List<ByteArray>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(transaction: Transaction)
}

@Dao
interface WorkDao {
    @Query("SELECT * FROM work ORDER BY _work LIMIT 1")
    suspend fun get(): Work?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(work: Work)

    @Query("DELETE FROM work")
    suspend fun clear()
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val publicKey: ByteArray,
    @ColumnInfo(name = "_transaction")
    val transaction: ByteArray
)

@Entity(tableName = "work")
data class Work(
    @ColumnInfo(name = "_work")
    @PrimaryKey val work: ByteArray
)