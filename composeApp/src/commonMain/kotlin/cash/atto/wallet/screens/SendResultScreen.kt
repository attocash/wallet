package cash.atto.wallet.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.atto_overview_background
import attowallet.composeapp.generated.resources.send_close
import attowallet.composeapp.generated.resources.send_failure_title
import attowallet.composeapp.generated.resources.send_failure_to
import attowallet.composeapp.generated.resources.send_success_to
import cash.atto.wallet.components.common.AttoOutlinedButton
import cash.atto.wallet.components.common.AttoOutlinedTextCard
import cash.atto.wallet.ui.AttoFormatter
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.success
import cash.atto.wallet.uistate.send.SendResultUiState
import cash.atto.wallet.uistate.send.SendTransactionUiState
import cash.atto.wallet.viewmodel.SendTransactionViewModel
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SendResultScreen(
    onClose: () -> Unit
) {
    val viewModel = koinViewModel<SendTransactionViewModel>()
    val uiState = viewModel.state.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    SendResult(
        uiState = uiState.value.sendResultUiState,
        onClose = {
            coroutineScope.launch {
                viewModel.clearTransactionData()
                onClose.invoke()
            }
        }
    )
}

@Composable
fun SendResult(
    uiState: SendResultUiState,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
            .paint(
                painter = painterResource(resource = Res.drawable.atto_overview_background),
                contentScale = ContentScale.FillBounds
            )
            .safeDrawingPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(64.dp))

        when (uiState.result) {
            SendTransactionUiState.SendOperationResult.SUCCESS -> {
                Icon(
                    imageVector = Icons.Outlined.Done,
                    contentDescription = "Check Icon",
                    modifier = Modifier
                        .height(96.dp)
                        .width(96.dp),
                    tint = MaterialTheme.colorScheme.success
                )

                AttoOutlinedTextCard(
                    text = AttoFormatter.format(uiState.amount),
                    color = MaterialTheme.colorScheme.success
                )

                Text(
                    text = stringResource(Res.string.send_success_to),
                    color = MaterialTheme.colorScheme.success,
                    style = MaterialTheme.typography.headlineMedium
                )

                AttoOutlinedTextCard(
                    text = uiState.address.orEmpty(),
                    color = MaterialTheme.colorScheme.success
                )
            }

            SendTransactionUiState.SendOperationResult.FAILURE -> {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Cross Icon",
                    modifier = Modifier
                        .height(96.dp)
                        .width(96.dp),
                    tint = MaterialTheme.colorScheme.error
                )

                Text(
                    text = stringResource(Res.string.send_failure_title),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.headlineMedium
                )

                AttoOutlinedTextCard(
                    text = AttoFormatter.format(uiState.amount),
                    color = MaterialTheme.colorScheme.error
                )

                Text(
                    text = stringResource(Res.string.send_failure_to),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.headlineMedium
                )

                AttoOutlinedTextCard(
                    text = uiState.address.orEmpty(),
                    color = MaterialTheme.colorScheme.error
                )
            }

            else -> {}
        }

        Spacer(Modifier.weight(1f))

        AttoOutlinedButton(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
            color = if (uiState.result == SendTransactionUiState.SendOperationResult.SUCCESS)
                MaterialTheme.colorScheme.success
            else MaterialTheme.colorScheme.error,
            transparent = true
        ) {
            Text(text = stringResource(Res.string.send_close))
        }
    }
}

@Preview
@Composable
fun SendResultPreview() {
    AttoWalletTheme {
        SendResult(
            uiState = SendResultUiState(
                result = SendTransactionUiState.SendOperationResult.SUCCESS,
                amount = BigDecimal.TEN,
                address = "atto://address"
            ),
            onClose = {}
        )
    }
}