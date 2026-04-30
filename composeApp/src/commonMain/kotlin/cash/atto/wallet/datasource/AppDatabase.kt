package cash.atto.wallet.datasource

import androidx.room3.Dao
import androidx.room3.Database
import androidx.room3.Entity
import androidx.room3.ExperimentalRoomApi
import androidx.room3.Index
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.PrimaryKey
import androidx.room3.Query
import androidx.room3.RoomDatabase
import androidx.room3.RoomWarnings

@OptIn(ExperimentalRoomApi::class)
@Database(
    entities = [AccountEntry::class, Work::class],
    version = 4,
)
@Suppress(RoomWarnings.NO_DATABASE_CONSTRUCTOR)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountEntryDao(): AccountEntryDao

    abstract fun workDao(): WorkDao
}

@Dao
interface AccountEntryDao {
    @Query(
        "SELECT entry from accountEntries " +
            "WHERE publicKey = :publicKey " +
            "ORDER BY height DESC LIMIT 1",
    )
    suspend fun last(publicKey: ByteArray): String?

    @Query(
        "SELECT entry from accountEntries " +
            "WHERE publicKey = :publicKey AND height < :beforeHeightExclusive " +
            "ORDER BY height DESC LIMIT :limit",
    )
    suspend fun listBefore(
        publicKey: ByteArray,
        beforeHeightExclusive: Long,
        limit: Int,
    ): List<String>

    @Query(
        "SELECT entry from accountEntries " +
            "WHERE publicKey = :publicKey " +
            "ORDER BY height DESC LIMIT :limit",
    )
    suspend fun listRecent(
        publicKey: ByteArray,
        limit: Int,
    ): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entry: AccountEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAll(entries: List<AccountEntry>)

    @Query(
        "SELECT blockType, balanceRaw, previousBalanceRaw FROM accountEntries " +
            "WHERE publicKey = :publicKey AND blockType != ''",
    )
    suspend fun summaryRows(publicKey: ByteArray): List<SummaryRow>

    data class SummaryRow(
        val blockType: String,
        val balanceRaw: String,
        val previousBalanceRaw: String,
    )
}

@Dao
interface WorkDao {
    @Query("SELECT * FROM work ORDER BY value LIMIT 1")
    suspend fun get(): Work?

    @Query("SELECT * FROM work WHERE publicKey = :publicKey LIMIT 1")
    suspend fun get(publicKey: ByteArray): Work?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(work: Work)

    @Query("DELETE FROM work WHERE publicKey = :publicKey")
    suspend fun clear(publicKey: ByteArray)
}

@Entity(
    tableName = "accountEntries",
    indices = [Index(value = ["publicKey", "height"])],
)
data class AccountEntry(
    @PrimaryKey
    val hash: ByteArray,
    val publicKey: ByteArray,
    val height: Long,
    val entry: String,
    val blockType: String = "",
    val balanceRaw: String = "0",
    val previousBalanceRaw: String = "0",
)

@Entity(tableName = "work")
data class Work(
    @PrimaryKey
    val publicKey: ByteArray,
    val value: ByteArray,
)
