package cash.atto.wallet.datasource

import androidx.room3.ExperimentalRoomApi
import androidx.room3.RoomDatabaseConstructor

@OptIn(ExperimentalRoomApi::class)
internal object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase = AppDatabase_Impl()
}
