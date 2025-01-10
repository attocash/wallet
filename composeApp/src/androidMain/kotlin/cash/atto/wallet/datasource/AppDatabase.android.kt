package cash.atto.wallet.datasource

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Database(
    entities = [AccountEntryAndroid::class, WorkAndroid::class],
    version = 2
)
abstract class AppDatabaseAndroid : RoomDatabase(), AppDatabase, DB {
    abstract override fun accountEntryDao(): AccountEntryDaoAndroid
    abstract override fun workDao(): WorkDaoAndroid

    override fun clearAllTables() {
        super.clearAllTables()
    }
}

interface DB {
    fun clearAllTables() {}
}

@Dao
interface AccountEntryDaoAndroid : AccountEntryDao {
    @Query(
        "SELECT entry from accountEntries " +
                "WHERE publicKey = :publicKey " +
                "ORDER BY height DESC LIMIT 1"
    )
    override suspend fun last(publicKey: ByteArray): ByteArray?

    @Query(
        "SELECT entry from accountEntries " +
                "WHERE publicKey = :publicKey " +
                "ORDER BY height DESC"
    )
    override suspend fun list(publicKey: ByteArray): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entry: AccountEntryAndroid)

    override suspend fun save(entry: AccountEntry) = save(entry as AccountEntryAndroid)
}

@Dao
interface WorkDaoAndroid : WorkDao {
    @Query("SELECT * FROM work ORDER BY value LIMIT 1")
    override suspend fun get(): WorkAndroid?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(work: WorkAndroid)

    override suspend fun set(work: Work) = set(work as WorkAndroid)

    @Query("DELETE FROM work")
    override suspend fun clear()
}

@Entity(tableName = "accountEntries")
data class AccountEntryAndroid(
    @PrimaryKey
    override val hash: ByteArray,
    override val publicKey: ByteArray,
    override val height: Long,
    override val entry: String
) : AccountEntry

@Entity(tableName = "work")
data class WorkAndroid(
    @PrimaryKey
    override val publicKey: ByteArray,
    override val value: ByteArray
) : Work

actual fun createAccountEntry(
    hash: ByteArray,
    publicKey: ByteArray,
    height: Long,
    entry: String
): AccountEntry = AccountEntryAndroid(
    hash = hash,
    publicKey = publicKey,
    height = height,
    entry = entry
)

actual fun createWork(
    publicKey: ByteArray,
    value: ByteArray
): Work = WorkAndroid(
    publicKey = publicKey,
    value = value
)