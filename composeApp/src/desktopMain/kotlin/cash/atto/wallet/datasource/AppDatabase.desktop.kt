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
    entities = [AccountEntryDesktop::class, WorkDesktop::class],
    version = 2
)
abstract class AppDatabaseDesktop : RoomDatabase(), AppDatabase, DB {
    abstract override fun accountEntryDao(): AccountEntryDaoDesktop
    abstract override fun workDao(): WorkDaoDesktop

    override fun clearAllTables() {
        super.clearAllTables()
    }
}

interface DB {
    fun clearAllTables() {}
}

@Dao
interface AccountEntryDaoDesktop : AccountEntryDao {
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
    override val entry: String
) : AccountEntry

@Entity(tableName = "work")
data class WorkDesktop(
    @PrimaryKey
    override val publicKey: ByteArray,
    override val value: ByteArray
) : Work

actual fun createAccountEntry(
    hash: ByteArray,
    publicKey: ByteArray,
    height: Long,
    entry: String
): AccountEntry = AccountEntryDesktop(
    hash = hash,
    publicKey = publicKey,
    height = height,
    entry = entry
)

actual fun createWork(
    publicKey: ByteArray,
    value: ByteArray
): Work = WorkDesktop(
    publicKey = publicKey,
    value = value
)