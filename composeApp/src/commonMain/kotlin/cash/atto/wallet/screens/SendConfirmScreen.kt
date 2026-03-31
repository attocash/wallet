package cash.atto.wallet.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.atto_overview_background
import attowallet.composeapp.generated.resources.send_confirm
import attowallet.composeapp.generated.resources.send_confirm_amount
import attowallet.composeapp.generated.resources.send_confirm_cancel
import attowallet.composeapp.generated.resources.send_confirm_sending
import attowallet.composeapp.generated.resources.send_confirm_to
import cash.atto.wallet.components.common.AppBar
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.components.common.AttoLoader
import cash.atto.wallet.components.common.AttoOutlinedButton
import cash.atto.wallet.ui.AttoFormatter
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.uistate.send.SendConfirmUiState
import cash.atto.wallet.viewmodel.SendTransactionViewModel
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

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

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${AttoFormatter.format(uiState.amount)} ATTO",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            uiState.amountUsd?.let {
                Text(
                    text = AttoFormatter.formatUsd(it),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Text(
            text = stringResource(Res.string.send_confirm_to),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = uiState.address.orEmpty(),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
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
fun SendConfirmContentRedesigned(
    uiState: SendConfirmUiState,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    isLoading: Boolean = false
) {
    Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.send_confirm_sending),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(Res.string.send_confirm_amount),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "${AttoFormatter.format(uiState.amount)} ATTO",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                uiState.amountUsd?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = AttoFormatter.formatUsd(it),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(Res.string.send_confirm_to),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = uiState.address.orEmpty(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        AttoButton(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(text = stringResource(Res.string.send_confirm))
            }
        }

        Spacer(Modifier.height(8.dp))

        AttoOutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(text = stringResource(Res.string.send_confirm_cancel))
        }
    }
}

enum class SendScreenState {
    SEND, CONFIRM, RESULT;
}

@Composable
fun SendConfirmContentPreview() {
    AttoWalletTheme {
        SendConfirmContent(
            uiState = SendConfirmUiState(
                amount = BigDecimal.TEN,
                amountUsd = null,
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
                amountUsd = null,
                address = "atto://address",
                showLoader = false
            ),
            onBackNavigation = {},
            onConfirm = {},
            onCancel = {}
        )
    }
}
