package cash.atto.wallet.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.send_button
import attowallet.composeapp.generated.resources.send_from_address_hint
import attowallet.composeapp.generated.resources.send_from_amount_hint
import attowallet.composeapp.generated.resources.send_from_title
import attowallet.composeapp.generated.resources.send_scan_qr
import cash.atto.wallet.components.common.AppBar
import cash.atto.wallet.components.common.AttoOutlinedButton
import cash.atto.wallet.di.AppScope
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.uistate.send.SendFromUiState
import cash.atto.wallet.viewmodel.SendTransactionViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.koin.compose.getKoin
import org.koin.compose.viewmodel.koinViewModel
import java.math.BigDecimal

@Composable
fun SendFromScreenAndroid(
    onBackNavigation: () -> Unit,
    onSendClicked: () -> Unit
) {
    val viewModel = koinViewModel<SendTransactionViewModel>()
    val uiState = viewModel.state.collectAsState()

    SendFromAndroid(
        uiState = uiState.value.sendFromUiState,
        onBackNavigation = onBackNavigation,
        onSendClicked = onSendClicked,
        onAmountChanged = { amount ->
            viewModel.updateSendInfo(
                amount = amount,
                address = uiState.value.sendFromUiState.address
            )
        },
        onAddressChanged = { address ->
            viewModel.updateSendInfo(
                amount = uiState.value.sendFromUiState.amount,
                address = address
            )
        }
    )
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SendFromAndroid(
    uiState: SendFromUiState,
    onBackNavigation: () -> Unit,
    onSendClicked: () -> Unit,
    onAmountChanged: suspend (BigDecimal?) -> Unit,
    onAddressChanged: suspend (String?) -> Unit
) {
    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA
    )

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) openQRScanner()
        }
    )

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = { AppBar(onBackNavigation) },
        backgroundColor = MaterialTheme.colors.surface,
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(
                        bottom = WindowInsets.systemBars
                            .asPaddingValues()
                            .calculateBottomPadding()
                                + 16.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(Res.string.send_from_title),
                    color = MaterialTheme.colors.primary,
                    style = MaterialTheme.typography.h5
                )

                uiState.accountName?.let { Text(text = it) }
                uiState.accountSeed?.let {
                    Text(
                        text = it,
                        textAlign = TextAlign.Center
                    )
                }

                Text(text = "(${uiState.accountBalance})")

                TextField(
                    value = uiState.amount?.toString().orEmpty(),
                    onValueChange = {
                        coroutineScope.launch {
                            onAmountChanged.invoke(it.toBigDecimalOrNull())
                        }
                    },
                    placeholder = {
                        Text(text = stringResource(Res.string.send_from_amount_hint))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                TextField(
                    value = uiState.address.orEmpty(),
                    onValueChange = {
                        coroutineScope.launch {
                            onAddressChanged.invoke(it)
                        }
                    },
                    placeholder = {
                        Text(text = stringResource(Res.string.send_from_address_hint))
                    }
                )

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = onSendClicked,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(Res.string.send_button))
                }

                AttoOutlinedButton(
                    onClick = {
                        if (cameraPermissionState.status.isGranted)
                            openQRScanner()
                        else requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(Res.string.send_scan_qr))
                }
            }
        }
    )
}

fun openQRScanner() {}

@Preview
@Composable
fun SendFromAndroidPreview() {
    AttoWalletTheme {
        SendFromAndroid(
            uiState = SendFromUiState.DEFAULT,
            onBackNavigation = {},
            onSendClicked = {},
            onAmountChanged = {},
            onAddressChanged = {}
        )
    }
}