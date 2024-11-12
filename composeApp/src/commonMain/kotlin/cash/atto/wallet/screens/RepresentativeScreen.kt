package cash.atto.wallet.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.representative_change
import attowallet.composeapp.generated.resources.representative_subtitle
import attowallet.composeapp.generated.resources.representative_title
import cash.atto.wallet.components.common.AppBar
import cash.atto.wallet.components.settings.EnterRepresentativeBottomSheet
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.BottomSheetShape
import cash.atto.wallet.uistate.settings.RepresentativeUIState
import cash.atto.wallet.viewmodel.RepresentativeViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RepresentativeScreen(
    onBackNavigation: () -> Unit
) {
    val viewModel = koinViewModel<RepresentativeViewModel>()
    val uiState = viewModel.state.collectAsState()

    RepresentativeScreenContent(
        uiState = uiState.value,
        onBackNavigation = onBackNavigation,
        onChange = {
            viewModel.setRepresentative(it)
        }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RepresentativeScreenContent(
    uiState: RepresentativeUIState,
    onBackNavigation: () -> Unit,
    onChange: suspend (String) -> Boolean,
) {
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden
    )

    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheetLayout(
        sheetContent = {
            EnterRepresentativeBottomSheet(
                onChange = {
                    coroutineScope.launch {
                        if (onChange.invoke(it))
                            sheetState.hide()
                    }
                },
                onClose = {
                    coroutineScope.launch {
                        sheetState.hide()
                    }
                },
                showError = uiState.showError
            )
        },
        sheetState = sheetState,
        sheetShape = BottomSheetShape
    ) {
        Scaffold(
            topBar = { AppBar(onBackNavigation) },
            backgroundColor = MaterialTheme.colors.surface,
            content = {
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
                        text = stringResource(Res.string.representative_title),
                        color = MaterialTheme.colors.primary,
                        style = MaterialTheme.typography.h5
                    )

                    Text(
                        text = stringResource(Res.string.representative_subtitle),
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    Text(text = uiState.representative.orEmpty())

                    Spacer(Modifier.weight(1f))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                sheetState.show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(Res.string.representative_change))
                    }
                }
            }
        )
    }
}

@Preview
@Composable
fun RepresentativeScreenContentPreview() {
    AttoWalletTheme {
        RepresentativeScreenContent(
            uiState = RepresentativeUIState("atto://address"),
            onBackNavigation = {},
            onChange = { false }
        )
    }
}