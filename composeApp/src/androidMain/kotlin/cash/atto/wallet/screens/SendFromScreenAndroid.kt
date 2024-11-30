package cash.atto.wallet.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.atto_overview_background
import attowallet.composeapp.generated.resources.send_button
import attowallet.composeapp.generated.resources.send_error_address
import attowallet.composeapp.generated.resources.send_error_amount
import attowallet.composeapp.generated.resources.send_from_address_hint
import attowallet.composeapp.generated.resources.send_from_amount_hint
import attowallet.composeapp.generated.resources.send_from_title
import attowallet.composeapp.generated.resources.send_scan_qr
import cash.atto.wallet.QRScannerActivity
import cash.atto.wallet.components.common.AppBar
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.components.common.AttoLoader
import cash.atto.wallet.components.common.AttoOutlinedButton
import cash.atto.wallet.di.AppScope
import cash.atto.wallet.ui.AttoFormatter
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.attoFontFamily
import cash.atto.wallet.uistate.send.SendFromUiState
import cash.atto.wallet.viewmodel.SendTransactionViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
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

    val coroutineScope = rememberCoroutineScope()

    SendFromAndroid(
        uiState = uiState.value.sendFromUiState,
        onBackNavigation = onBackNavigation,
        onSendClicked = {
            coroutineScope.launch {
                if (viewModel.checkTransactionData())
                    onSendClicked.invoke()
            }
        },
        onAmountChanged = { amount ->
            coroutineScope.launch {
                viewModel.updateSendInfo(
                    amount = amount,
                    address = uiState.value.sendFromUiState.address
                )
            }
        },
        onAddressChanged = { address ->
            coroutineScope.launch {
                viewModel.updateSendInfo(
                    amount = uiState.value.sendFromUiState.amountString,
                    address = address
                )
            }
        }
    )
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SendFromAndroid(
    uiState: SendFromUiState,
    onBackNavigation: () -> Unit,
    onSendClicked: () -> Unit,
    onAmountChanged: (String?) -> Unit,
    onAddressChanged: (String?) -> Unit
) {
    val context = LocalContext.current
    val activityResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            val qr = it.data?.getStringExtra(QRScannerActivity.QR_TAG)

            onAddressChanged.invoke(qr)
        }
    }

    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA
    )

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) openQRScanner(context, activityResultLauncher)
        }
    )

    Scaffold(
        topBar = { AppBar(onBackNavigation) },
        modifier = Modifier.paint(
            painter = painterResource(resource = Res.drawable.atto_overview_background),
            contentScale = ContentScale.FillBounds
        ),
        backgroundColor = Color.Transparent,
        content = { padding ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(
                        bottom = WindowInsets.systemBars
                            .asPaddingValues()
                            .calculateBottomPadding()
                    )
            ) {
                if (uiState.showLoader)
                    AttoLoader(alpha = 0.7f)

                SendFromAndroidContent(
                    uiState = uiState,
                    onSendClicked = onSendClicked,
                    onScanClicked = {
                        if (cameraPermissionState.status.isGranted) {
                            openQRScanner(context, activityResultLauncher)
                        } else requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    onAmountChanged = onAmountChanged,
                    onAddressChanged = onAddressChanged
                )
            }
        }
    )
}

@Composable
fun SendFromAndroidContent(
    uiState: SendFromUiState,
    onSendClicked: () -> Unit,
    onScanClicked: () -> Unit,
    onAmountChanged: (String?) -> Unit,
    onAddressChanged: (String?) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.send_from_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.W300,
                fontFamily = attoFontFamily(),
            )

            uiState.accountName?.let {
                Text(
                    text = it,
                    modifier = Modifier.padding(top = 16.dp),
                    style = MaterialTheme.typography.body2
                )
            }

            uiState.accountSeed?.let {
                Text(
                    text = it,
                    modifier = Modifier.padding(top = 20.dp),
                    color = MaterialTheme.colors.secondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W600,
                    fontFamily = attoFontFamily(),
                    textAlign = TextAlign.Center
                )
            }

            Text(
                text = "(${AttoFormatter.format(
                    uiState.accountBalance
                )})",
                modifier = Modifier.padding(top = 36.dp),
                style = MaterialTheme.typography.h6
            )

            TextField(
                value = uiState.amountString.orEmpty(),
                onValueChange = onAmountChanged,
                modifier = Modifier.fillMaxWidth()
                    .padding(top = 20.dp),
                placeholder = {
                    Text(text = stringResource(Res.string.send_from_amount_hint))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            if (uiState.showAmountError) {
                Text(
                    text = stringResource(Res.string.send_error_amount),
                    modifier = Modifier.padding(top = 4.dp),
                    color = MaterialTheme.colors.error,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.caption
                )
            }

            TextField(
                value = uiState.address.orEmpty(),
                onValueChange = {
                    onAddressChanged.invoke(it)
                },
                modifier = Modifier.fillMaxWidth()
                    .padding(top = 16.dp),
                placeholder = {
                    Text(text = stringResource(Res.string.send_from_address_hint))
                }
            )

            if (uiState.showAddressError) {
                Text(
                    text = stringResource(Res.string.send_error_address),
                    modifier = Modifier.padding(top = 4.dp),
                    color = MaterialTheme.colors.error,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.caption
                )
            }
        }

        AttoButton(
            onClick = onSendClicked,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(Res.string.send_button))
        }

        AttoOutlinedButton(
            onClick = onScanClicked,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(Res.string.send_scan_qr))
        }
    }
}

fun openQRScanner(
    context: Context,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>
) {
    val intent = Intent(context, QRScannerActivity::class.java)
    launcher.launch(intent)
}

@Preview
@Composable
fun SendFromAndroidContentPreview() {
    AttoWalletTheme {
        SendFromAndroidContent(
            uiState = SendFromUiState.DEFAULT,
            onSendClicked = {},
            onScanClicked = {},
            onAmountChanged = {},
            onAddressChanged = {}
        )
    }
}

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