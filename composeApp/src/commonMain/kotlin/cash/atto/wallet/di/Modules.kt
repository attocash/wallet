package cash.atto.wallet.di

import cash.atto.commons.wallet.AttoAccountEntryRepository
import cash.atto.commons.wallet.AttoTransactionRepository
import cash.atto.commons.wallet.inMemory
import cash.atto.wallet.interactor.CheckPasswordInteractor
import cash.atto.wallet.repository.AppStateRepository
import cash.atto.wallet.repository.RepresentativeRepository
import cash.atto.wallet.repository.WalletManagerRepository
import cash.atto.wallet.viewmodel.AppViewModel
import cash.atto.wallet.viewmodel.BackupSecretViewModel
import cash.atto.wallet.viewmodel.CreatePasswordViewModel
import cash.atto.wallet.viewmodel.ImportSecretViewModel
import cash.atto.wallet.viewmodel.OverviewViewModel
import cash.atto.wallet.viewmodel.RepresentativeViewModel
import cash.atto.wallet.viewmodel.SecretPhraseViewModel
import cash.atto.wallet.viewmodel.SendTransactionViewModel
import cash.atto.wallet.viewmodel.SettingsViewModel
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
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

val repositoryModule = module {
    includes(httpClientModule)
    includes(dataSourceModule)
    singleOf(::AppStateRepository)
    singleOf(::RepresentativeRepository)
    singleOf(::WalletManagerRepository)
    single { AttoAccountEntryRepository.inMemory() }
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
    viewModelOf(::OverviewViewModel)
    viewModelOf(::RepresentativeViewModel)
    viewModelOf(::SecretPhraseViewModel)
    viewModelOf(::SendTransactionViewModel)
    viewModelOf(::SettingsViewModel)
}