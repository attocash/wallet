package cash.atto.wallet.di

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import cash.atto.wallet.datasource.AppDatabase
import cash.atto.wallet.datasource.PasswordDataSource
import cash.atto.wallet.datasource.RepresentativeDao
import cash.atto.wallet.datasource.RepresentativeDataSource
import cash.atto.wallet.datasource.SeedDataSource
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun getDatabaseBuilder(ctx: Context): AppDatabase {
    val dbFile = ctx.getDatabasePath("atto-wallet.db")
    return Room.databaseBuilder<AppDatabase>(ctx, dbFile.absolutePath)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

actual val databaseModule = module {
    single<AppDatabase> { getDatabaseBuilder(get()) }
}

actual val dataSourceModule = module {
    includes(databaseModule)
    singleOf(::RepresentativeDataSource).bind(RepresentativeDao::class)
    singleOf(::PasswordDataSource)
    singleOf(::SeedDataSource)
}