package cash.atto.wallet.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.atto_overview_background
import attowallet.composeapp.generated.resources.overview_receive
import attowallet.composeapp.generated.resources.overview_send
import cash.atto.wallet.components.common.AttoOutlinedButton
import cash.atto.wallet.components.common.AttoReceiveButton
import cash.atto.wallet.components.common.AttoSendButton
import cash.atto.wallet.components.overview.OverviewHeader
import cash.atto.wallet.components.overview.TransactionsList
import cash.atto.wallet.ui.BottomSheetShape
import cash.atto.wallet.uistate.overview.OverviewUiState
import cash.atto.wallet.components.overview.ReceiveAttoBottomSheet
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.viewmodel.OverviewViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OverviewScreenAndroid(
    onSettingsClicked: () -> Unit,
    onSendClicked: () -> Unit
) {
    val viewModel = koinViewModel<OverviewViewModel>()
    val uiState = viewModel.state.collectAsState()

    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    OverviewAndroid(
        uiState = uiState.value,
        onSettingsClicked = onSettingsClicked,
        onSendClicked = onSendClicked,
        onReceiveCopyClick = {
            uiState.value.receiveAddress?.let {
                clipboardManager.setText(AnnotatedString(it))
            }
        },
        onReceiveShareClick = {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, uiState.value.receiveAddress)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            context.startActivity(shareIntent)
        }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun OverviewAndroid(
    uiState: OverviewUiState,
    onSettingsClicked: () -> Unit,
    onSendClicked: () -> Unit,
    onReceiveCopyClick: () -> Unit,
    onReceiveShareClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )

    ModalBottomSheetLayout(
        sheetContent = { ReceiveAttoBottomSheet(
            address = uiState.receiveAddress,
            onCopy = onReceiveCopyClick,
            onShare = onReceiveShareClick
        ) },
        sheetState = sheetState,
        sheetShape = BottomSheetShape
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .paint(
                    painter = painterResource(resource = Res.drawable.atto_overview_background),
                    contentScale = ContentScale.FillBounds
                )
                .safeDrawingPadding()
        ) {
            OverviewHeader(
                uiState = uiState.headerUiState,
                onSettingsClicked = onSettingsClicked,
                modifier = Modifier.fillMaxWidth()
            )

            OverviewAndroidContent(
                uiState = uiState,
                onSendClicked = onSendClicked,
                onReceiveClicked = {
                    scope.launch {
                        if (uiState.receiveAddress != null)
                            sheetState.show()
                    }
                }
            )
        }
    }
}

@Composable
fun OverviewAndroidContent(
    uiState: OverviewUiState,
    onSendClicked: () -> Unit,
    onReceiveClicked: () -> Unit
) {
    Column(Modifier.padding(
        top = 40.dp,
        bottom = 16.dp
    )) {
        Surface(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 16.dp),
            elevation = 1.dp,
            shape = RoundedCornerShape(50.dp)
        ) {
            Box(modifier = Modifier
                .background(color = MaterialTheme.colors.surface)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
            ) {
                TransactionsList(
                    uiState = uiState.transactionListUiState,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AttoSendButton(
                onClick = onSendClicked,
                modifier = Modifier.weight(1f)
            )

            AttoReceiveButton(
                onClick = onReceiveClicked,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview
@Composable
fun OverviewAndroidContentPreview() {
    AttoWalletTheme {
        OverviewAndroidContent(
            uiState = OverviewUiState.DEFAULT,
            onSendClicked = {},
            onReceiveClicked = {}
        )
    }
}