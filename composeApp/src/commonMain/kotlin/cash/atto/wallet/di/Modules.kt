package cash.atto.wallet.di

import cash.atto.commons.AttoNetwork
import cash.atto.wallet.datasource.TempSeedDataSource
import cash.atto.wallet.interactor.CheckPasswordInteractor
import cash.atto.wallet.repository.AppStateRepository
import cash.atto.wallet.repository.PersistentAccountEntryRepository
import cash.atto.wallet.repository.PersistentWorkCache
import cash.atto.wallet.repository.WalletManagerRepository
import cash.atto.wallet.viewmodel.AppViewModel
import cash.atto.wallet.viewmodel.BackupSecretViewModel
import cash.atto.wallet.viewmodel.CreatePasswordViewModel
import cash.atto.wallet.viewmodel.ImportSecretViewModel
import cash.atto.wallet.viewmodel.MainScreenViewModel
import cash.atto.wallet.viewmodel.OverviewViewModel
import cash.atto.wallet.viewmodel.ReceiveViewModel
import cash.atto.wallet.viewmodel.RepresentativeViewModel
import cash.atto.wallet.viewmodel.SecretPhraseViewModel
import cash.atto.wallet.viewmodel.SendTransactionViewModel
import cash.atto.wallet.viewmodel.SettingsViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

object AppScope

val httpClientModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(
                    json = Json { ignoreUnknownKeys = true },
                    contentType = ContentType.Any
                )
            }
            install(Logging) {
                level = LogLevel.ALL // Logs everything (headers, bodies, etc.)
                logger = Logger.SIMPLE
            }
            install(HttpTimeout)
        }
    }
}

expect val databaseModule: Module

expect val dataSourceModule: Module

val commonDataSourceModule = module {
    singleOf(::TempSeedDataSource)
}

val repositoryModule = module {
    includes(httpClientModule)
    includes(commonDataSourceModule)
    includes(dataSourceModule)
    single { AttoNetwork.DEV }
//    single { AttoNetwork.LIVE }
    singleOf(::AppStateRepository)
    singleOf(::PersistentAccountEntryRepository)
    singleOf(::PersistentWorkCache)
    singleOf(::WalletManagerRepository)
}

val interactorModule = module {
    singleOf(::CheckPasswordInteractor)
}

val viewModelModule = module {
    includes(repositoryModule)
    includes(interactorModule)
    viewModelOf(::AppViewModel)
    viewModelOf(::BackupSecretViewModel)
    viewModelOf(::CreatePasswordViewModel)
    viewModelOf(::ImportSecretViewModel)
    viewModelOf(::MainScreenViewModel)
    viewModelOf(::OverviewViewModel)
    viewModelOf(::ReceiveViewModel)
    viewModelOf(::RepresentativeViewModel)
    viewModelOf(::SecretPhraseViewModel)
    viewModelOf(::SendTransactionViewModel)
    viewModelOf(::SettingsViewModel)
}