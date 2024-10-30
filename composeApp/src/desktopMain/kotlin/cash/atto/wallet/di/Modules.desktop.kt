package cash.atto.wallet.di

import cash.atto.wallet.datasource.SeedDataSource
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val dataSourceModule = module {
    singleOf(::SeedDataSource)
}
actual val databaseModule: Module
    get() = TODO("Not yet implemented")