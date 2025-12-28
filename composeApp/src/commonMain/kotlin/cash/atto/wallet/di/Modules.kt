package cash.atto.wallet.di

import cash.atto.commons.AttoNetwork
import cash.atto.wallet.datasource.TempSeedDataSource
import cash.atto.wallet.interactor.CheckPasswordInteractor
import cash.atto.wallet.repository.AppStateRepository
import cash.atto.wallet.repository.PersistentAccountEntryRepository
import cash.atto.wallet.repository.PersistentWorkCache
import cash.atto.wallet.repository.VotersRepository
import cash.atto.wallet.repository.WalletManagerRepository
import cash.atto.wallet.viewmodel.AppViewModel
import cash.atto.wallet.viewmodel.BackupSecretViewModel
import cash.atto.wallet.viewmodel.CreatePasswordViewModel
import cash.atto.wallet.viewmodel.ImportSecretViewModel
import cash.atto.wallet.viewmodel.MainScreenViewModel
import cash.atto.wallet.viewmodel.OverviewViewModel
import cash.atto.wallet.viewmodel.ReceiveViewModel
import cash.atto.wallet.viewmodel.VoterViewModel
import cash.atto.wallet.viewmodel.SecretPhraseViewModel
import cash.atto.wallet.viewmodel.SendTransactionViewModel
import cash.atto.wallet.viewmodel.SettingsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

object AppScope

expect val databaseModule: Module

expect val dataSourceModule: Module

val commonDataSourceModule = module {
    singleOf(::TempSeedDataSource)
}

val repositoryModule = module {
    includes(commonDataSourceModule)
    includes(dataSourceModule)
//    single { AttoNetwork.DEV }
    single { AttoNetwork.LIVE }
    singleOf(::AppStateRepository)
    singleOf(::PersistentAccountEntryRepository)
    singleOf(::PersistentWorkCache)
    singleOf(::VotersRepository)
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
    viewModelOf(::VoterViewModel)
    viewModelOf(::SecretPhraseViewModel)
    viewModelOf(::SendTransactionViewModel)
    viewModelOf(::SettingsViewModel)
}