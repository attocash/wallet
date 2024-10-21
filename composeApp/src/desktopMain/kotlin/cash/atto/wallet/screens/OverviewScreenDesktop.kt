package cash.atto.wallet.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.overview_receive
import attowallet.composeapp.generated.resources.overview_send
import cash.atto.wallet.components.common.AttoOutlinedButton
import cash.atto.wallet.components.overview.OverviewHeader
import cash.atto.wallet.components.overview.TransactionsList
import cash.atto.wallet.ui.BottomSheetShape
import cash.atto.wallet.uistate.overview.OverviewUiState
import cash.atto.wallet.components.overview.ReceiveAttoBottomSheet
import cash.atto.wallet.viewmodel.OverviewViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OverviewScreenDesktop(onSettingsClicked: () -> Unit) {
    KoinContext {
        val viewModel = koinViewModel<OverviewViewModel>()
        val uiState = viewModel.state.collectAsState()

        val clipboardManager: ClipboardManager = LocalClipboardManager.current

        Overview(
            uiState = uiState.value,
            onSettingsClicked = onSettingsClicked,
            onReceiveCopyClick = {
                uiState.value.receiveAddress?.let {
                    clipboardManager.setText(AnnotatedString(it))
                }
            },
            onReceiveShareClick = {}
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun OverviewDesktop(
    uiState: OverviewUiState,
    onSettingsClicked: () -> Unit,
    onReceiveCopyClick: () -> Unit,
    onReceiveShareClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden
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
            modifier = Modifier.fillMaxSize()
                .background(color = MaterialTheme.colors.surface)
                .safeDrawingPadding()
                .padding(16.dp)
        ) {
            OverviewHeader(
                uiState = uiState.headerUiState,
                onSettingsClicked = onSettingsClicked,
                modifier = Modifier.fillMaxWidth()
            )

            TransactionsList(
                uiState = uiState.transactionListUiState,
                modifier = Modifier.weight(1f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            if (uiState.receiveAddress != null)
                                sheetState.show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = stringResource(Res.string.overview_receive))
                }

                AttoOutlinedButton(
                    onClick = {},
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = stringResource(Res.string.overview_send))
                }
            }
        }
    }
}