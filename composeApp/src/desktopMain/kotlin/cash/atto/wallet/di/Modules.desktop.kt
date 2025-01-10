package cash.atto.wallet.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import cash.atto.wallet.datasource.AppDatabase
import cash.atto.wallet.datasource.AppDatabaseDesktop
import cash.atto.wallet.datasource.PasswordDataSource
import cash.atto.wallet.datasource.SeedDataSource
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.io.File

fun getDatabaseBuilder(): AppDatabase {
    val homeDir = System.getProperty("user.home")
    val dbFile = File(homeDir, ".atto/wallet.db")
    dbFile.parentFile?.mkdirs()
    return Room.databaseBuilder<AppDatabaseDesktop>(dbFile.absolutePath)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

actual val databaseModule = module {
    single<AppDatabase> { getDatabaseBuilder() }
}

actual val dataSourceModule = module {
    includes(databaseModule)
    singleOf(::PasswordDataSource)
    singleOf(::SeedDataSource)
}