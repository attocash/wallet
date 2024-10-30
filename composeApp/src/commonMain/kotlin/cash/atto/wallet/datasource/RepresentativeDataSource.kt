package cash.atto.wallet.datasource

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update

@Database(entities = [Representative::class], version = 1)
abstract class AppDatabase : RoomDatabase(), DB {
    abstract fun getDao(): RepresentativeDao
    override fun clearAllTables(): Unit {}
}

interface DB {
    fun clearAllTables(): Unit {}
}

@Dao
interface RepresentativeDao {
    @Query("SELECT * FROM representatives WHERE publicKey = :wallet")
    suspend fun getRepresentatives(wallet: String): List<Representative>

    @Update
    suspend fun updateRepresentative(representative: Representative)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createRepresentative(representative: Representative)
}

@Entity(tableName = "representatives")
data class Representative(
    @PrimaryKey
    val publicKey: String,
    val address: String
)