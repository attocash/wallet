package cash.atto.wallet.di

import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(viewModelModule)
}