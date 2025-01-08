package cash.atto.wallet.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.atto_overview_background
import attowallet.composeapp.generated.resources.send_confirm
import attowallet.composeapp.generated.resources.send_confirm_cancel
import attowallet.composeapp.generated.resources.send_confirm_sending
import attowallet.composeapp.generated.resources.send_confirm_to
import cash.atto.wallet.components.common.AppBar
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.components.common.AttoLoader
import cash.atto.wallet.components.common.AttoOutlinedButton
import cash.atto.wallet.components.common.AttoOutlinedTextCard
import cash.atto.wallet.ui.AttoFormatter
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.uistate.send.SendConfirmUiState
import cash.atto.wallet.viewmodel.SendTransactionViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import java.math.BigDecimal

@Composable
fun SendConfirmScreen(
    onBackNavigation: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val viewModel = koinViewModel<SendTransactionViewModel>()
    val uiState = viewModel.state.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    SendConfirm(
        uiState = uiState.value.sendConfirmUiState,
        onBackNavigation = onBackNavigation,
        onConfirm = {
            coroutineScope.launch {
                viewModel.showLoader()
                viewModel.send()
                viewModel.hideLoader()

                onConfirm.invoke()
            }
        },
        onCancel = onCancel
    )
}

@Composable
fun SendConfirm(
    uiState: SendConfirmUiState,
    onBackNavigation: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        if (uiState.showLoader) {
            AttoLoader(
                alpha = 0.4f,
                darkMode = true
            )
        }

        Scaffold(
            topBar = { AppBar(onBackNavigation) },
            modifier = Modifier.paint(
                painter = painterResource(resource = Res.drawable.atto_overview_background),
                contentScale = ContentScale.FillBounds
            ),
            containerColor = Color.Transparent,
            content = { innerPadding ->
                Box(
                    Modifier.fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp)
                ) {
                    Column(Modifier.fillMaxSize()) {
                        Spacer(Modifier.height(96.dp))

                        SendConfirmContent(
                            uiState = uiState,
                            onConfirm = onConfirm,
                            onCancel = onCancel
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun SendConfirmContent(
    modifier: Modifier = Modifier,
    uiState: SendConfirmUiState,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.send_confirm_sending),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineMedium
        )

        AttoOutlinedTextCard(
            text = AttoFormatter.format(uiState.amount),
            color = MaterialTheme.colorScheme.background
        )

        Text(
            text = stringResource(Res.string.send_confirm_to),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineMedium
        )

        AttoOutlinedTextCard(
            text = uiState.address.orEmpty(),
            color = MaterialTheme.colorScheme.background
        )

        Spacer(Modifier.weight(1f))

        AttoButton(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(Res.string.send_confirm))
        }

        AttoOutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(Res.string.send_confirm_cancel))
        }
    }
}

@Composable
fun SendConfirmContentPreview() {
    AttoWalletTheme {
        SendConfirmContent(
            uiState = SendConfirmUiState(
                amount = BigDecimal.TEN,
                address = "atto://address",
                showLoader = false
            ),
            onConfirm = {},
            onCancel = {}
        )
    }
}

@Preview
@Composable
fun SendConfirmPreview() {
    AttoWalletTheme {
        SendConfirm(
            uiState = SendConfirmUiState(
                amount = BigDecimal.TEN,
                address = "atto://address",
                showLoader = false
            ),
            onBackNavigation = {},
            onConfirm = {},
            onCancel = {}
        )
    }
}