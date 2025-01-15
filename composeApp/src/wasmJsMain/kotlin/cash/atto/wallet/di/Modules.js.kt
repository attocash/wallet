package cash.atto.wallet.di

import cash.atto.wallet.datasource.PasswordDataSource
import cash.atto.wallet.datasource.SeedDataSource
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val databaseModule =  module {
}

actual val dataSourceModule = module {
    includes(databaseModule)
    singleOf(::PasswordDataSource)
    singleOf(::SeedDataSource)
}