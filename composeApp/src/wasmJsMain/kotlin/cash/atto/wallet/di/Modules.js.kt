package cash.atto.wallet.di

import cash.atto.wallet.datasource.AppDatabase
import cash.atto.wallet.datasource.AppDatabaseWeb
import cash.atto.wallet.datasource.PasswordDataSource
import cash.atto.wallet.datasource.SaltDataSource
import cash.atto.wallet.datasource.SeedDataSource
import cash.atto.wallet.interactor.SeedArgon2Interactor
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val databaseModule =  module {
    single<AppDatabase> { AppDatabaseWeb() }
}

actual val dataSourceModule = module {
    includes(databaseModule)
    singleOf(::PasswordDataSource)
    singleOf(::SeedArgon2Interactor)
    singleOf(::SaltDataSource)
    singleOf(::SeedDataSource)
}