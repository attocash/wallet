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
    entities = [AccountEntryDesktop::class, WorkDesktop::class],
    version = Config.DATABASE_VERSION,
)
abstract class AppDatabaseDesktop :
    RoomDatabase(),
    AppDatabase {
    abstract override fun accountEntryDao(): AccountEntryDaoDesktop

    abstract override fun workDao(): WorkDaoDesktop
}

@Dao
interface AccountEntryDaoDesktop : AccountEntryDao {
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
    suspend fun save(entry: AccountEntryDesktop)

    override suspend fun save(entry: AccountEntry) = save(entry as AccountEntryDesktop)
}

@Dao
interface WorkDaoDesktop : WorkDao {
    @Query("SELECT * FROM work ORDER BY value LIMIT 1")
    override suspend fun get(): WorkDesktop?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(work: WorkDesktop)

    override suspend fun set(work: Work) = set(work as WorkDesktop)

    @Query("DELETE FROM work")
    override suspend fun clear()
}

@Entity(tableName = "accountEntries")
data class AccountEntryDesktop(
    @PrimaryKey
    override val hash: ByteArray,
    override val publicKey: ByteArray,
    override val height: Long,
    override val entry: String,
) : AccountEntry

@Entity(tableName = "work")
data class WorkDesktop(
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
    AccountEntryDesktop(
        hash = hash,
        publicKey = publicKey,
        height = height,
        entry = entry,
    )

actual fun createWork(
    publicKey: ByteArray,
    value: ByteArray,
): Work =
    WorkDesktop(
        publicKey = publicKey,
        value = value,
    )
