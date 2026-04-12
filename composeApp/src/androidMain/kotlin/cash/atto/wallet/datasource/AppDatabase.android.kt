package cash.atto.wallet.datasource

import androidx.room3.Dao
import androidx.room3.Database
import androidx.room3.Entity
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.PrimaryKey
import androidx.room3.Query
import androidx.room3.RoomDatabase
import cash.atto.wallet.Config

@Database(
    entities = [AccountEntryAndroid::class, WorkAndroid::class],
    version = Config.DATABASE_VERSION,
)
abstract class AppDatabaseAndroid :
    RoomDatabase(),
    AppDatabase {
    abstract override fun accountEntryDao(): AccountEntryDaoAndroid

    abstract override fun workDao(): WorkDaoAndroid
}

@Dao
interface AccountEntryDaoAndroid : AccountEntryDao {
    @Query(
        "SELECT entry from accountEntries " +
            "WHERE publicKey = :publicKey " +
            "ORDER BY height DESC LIMIT 1",
    )
    override suspend fun last(publicKey: ByteArray): ByteArray?

    @Query(
        "SELECT entry from accountEntries " +
            "WHERE publicKey = :publicKey " +
            "ORDER BY height DESC",
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
    override val entry: String,
) : AccountEntry

@Entity(tableName = "work")
data class WorkAndroid(
    @PrimaryKey
    override val publicKey: ByteArray,
    override val value: ByteArray,
) : Work

actual fun createAccountEntry(
    hash: ByteArray,
    publicKey: ByteArray,
    height: Long,
    entry: String,
): AccountEntry =
    AccountEntryAndroid(
        hash = hash,
        publicKey = publicKey,
        height = height,
        entry = entry,
    )

actual fun createWork(
    publicKey: ByteArray,
    value: ByteArray,
): Work =
    WorkAndroid(
        publicKey = publicKey,
        value = value,
    )
