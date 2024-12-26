package cash.atto.wallet.datasource

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import java.math.BigDecimal

@Database(
    entities = [AccountEntry::class, Work::class],
    version = 2
)
abstract class AppDatabase : RoomDatabase(), DB {
    abstract fun accountEntryDao(): AccountEntryDao
    abstract fun workDao(): WorkDao
    override fun clearAllTables(): Unit {}
}

interface DB {
    fun clearAllTables(): Unit {}
}

@Dao
interface AccountEntryDao {
    @Query(
        "SELECT entry from accountEntries " +
                "WHERE publicKey = :publicKey " +
                "ORDER BY height DESC LIMIT 1"
    )
    suspend fun last(publicKey: ByteArray): ByteArray?

    @Query(
        "SELECT entry from accountEntries " +
                "WHERE publicKey = :publicKey " +
                "ORDER BY height DESC"
    )
    suspend fun list(publicKey: ByteArray): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entry: AccountEntry)
}

@Dao
interface WorkDao {
    @Query("SELECT * FROM work ORDER BY value LIMIT 1")
    suspend fun get(): Work?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(work: Work)

    @Query("DELETE FROM work")
    suspend fun clear()
}

@Entity(tableName = "accountEntries")
data class AccountEntry(
    @PrimaryKey
    val hash: ByteArray,
    val publicKey: ByteArray,
    val height: Long,
    val entry: String
)

@Entity(tableName = "work")
data class Work(
    @PrimaryKey
    val publicKey: ByteArray,
    val value: ByteArray
)