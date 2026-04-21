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
import cash.atto.wallet.Config

@OptIn(ExperimentalRoomApi::class)
@Database(
    entities = [AccountEntry::class, Work::class],
    version = Config.DATABASE_VERSION,
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
            "WHERE publicKey = :publicKey " +
            "ORDER BY height DESC",
    )
    suspend fun list(publicKey: ByteArray): List<String>

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
)

@Entity(tableName = "work")
data class Work(
    @PrimaryKey
    val publicKey: ByteArray,
    val value: ByteArray,
)
