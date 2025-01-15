package cash.atto.wallet

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.uistate.AppUiState
import cash.atto.wallet.viewmodel.AppViewModel
import org.koin.compose.KoinContext
//import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AttoAppWeb(
//    component: DWNavigationComponent
) {
    AttoWalletTheme {
        Text("Hello")
//        KoinContext {
//            val viewModel = koinViewModel<AppViewModel>()
//            val uiState = viewModel.state.collectAsState()
//
//            AttoNavHost(
//                uiState = uiState.value,
//                component = component,
//                submitPassword = {
//                    viewModel.enterPassword(it)
//                }
//            )
//        }
    }
}

@Composable
fun AttoNavHost(
    uiState: AppUiState,
    component: DWNavigationComponent,
    modifier: Modifier = Modifier,
    submitPassword: suspend (String?) -> Boolean
) {}