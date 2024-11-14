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

@Deprecated("Wallet Manager handles representatives by itself now")
@Database(entities = [Representative::class], version = 1)
abstract class AppDatabase : RoomDatabase(), DB {
    abstract fun getDao(): RepresentativeDao
    override fun clearAllTables(): Unit {}
}

@Deprecated("Wallet Manager handles representatives by itself now")
interface DB {
    fun clearAllTables(): Unit {}
}

@Deprecated("Wallet Manager handles representatives by itself now")
@Dao
interface RepresentativeDao {
    @Query("SELECT * FROM representatives WHERE publicKey = :wallet")
    suspend fun getRepresentatives(wallet: String): List<Representative>

    @Update
    suspend fun updateRepresentative(representative: Representative)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createRepresentative(representative: Representative)
}

@Deprecated("Wallet Manager handles representatives by itself now")
@Entity(tableName = "representatives")
data class Representative(
    @PrimaryKey
    val publicKey: String,
    val address: String
)