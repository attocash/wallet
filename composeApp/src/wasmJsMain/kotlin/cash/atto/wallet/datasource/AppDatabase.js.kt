package cash.atto.wallet.datasource

import androidx.room3.Dao
import androidx.room3.Database
import androidx.room3.Entity
import androidx.room3.ExperimentalRoomApi
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.PrimaryKey
import androidx.room3.Query
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.room3.RoomWarnings
import cash.atto.wallet.Config

@OptIn(ExperimentalRoomApi::class)
@Database(
    entities = [AccountEntryWasmJs::class, WorkWasmJs::class],
    version = Config.DATABASE_VERSION,
)
@Suppress(RoomWarnings.NO_DATABASE_CONSTRUCTOR)
abstract class AppDatabaseWasmJs :
    RoomDatabase(),
    AppDatabase {
    abstract override fun accountEntryDao(): AccountEntryDaoWasmJs

    abstract override fun workDao(): WorkDaoWasmJs
}

internal object AppDatabaseWasmJsConstructor : RoomDatabaseConstructor<AppDatabaseWasmJs> {
    override fun initialize(): AppDatabaseWasmJs = AppDatabaseWasmJs_Impl()
}

@Dao
interface AccountEntryDaoWasmJs : AccountEntryDao {
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
    suspend fun save(entry: AccountEntryWasmJs)

    override suspend fun save(entry: AccountEntry) = save(entry as AccountEntryWasmJs)
}

@Dao
interface WorkDaoWasmJs : WorkDao {
    @Query("SELECT * FROM work ORDER BY value LIMIT 1")
    override suspend fun get(): WorkWasmJs?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(work: WorkWasmJs)

    override suspend fun set(work: Work) = set(work as WorkWasmJs)

    @Query("DELETE FROM work")
    override suspend fun clear()
}

@Entity(tableName = "accountEntries")
data class AccountEntryWasmJs(
    @PrimaryKey
    override val hash: ByteArray,
    override val publicKey: ByteArray,
    override val height: Long,
    override val entry: String,
) : AccountEntry

@Entity(tableName = "work")
data class WorkWasmJs(
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
    AccountEntryWasmJs(
        hash = hash,
        publicKey = publicKey,
        height = height,
        entry = entry,
    )

actual fun createWork(
    publicKey: ByteArray,
    value: ByteArray,
): Work =
    WorkWasmJs(
        publicKey = publicKey,
        value = value,
    )
