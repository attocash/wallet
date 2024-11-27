package cash.atto.wallet.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.atto_background_desktop
import attowallet.composeapp.generated.resources.representative_change
import attowallet.composeapp.generated.resources.representative_subtitle
import attowallet.composeapp.generated.resources.representative_title
import cash.atto.wallet.components.common.AppBar
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.components.settings.EnterRepresentativeBottomSheet
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.BottomSheetShape
import cash.atto.wallet.ui.attoFontFamily
import cash.atto.wallet.uistate.settings.RepresentativeUIState
import cash.atto.wallet.viewmodel.RepresentativeViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
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

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun RepresentativeScreenContent(
    uiState: RepresentativeUIState,
    onBackNavigation: () -> Unit,
    onChange: suspend (String) -> Boolean,
) {
    val windowSizeClass = calculateWindowSizeClass()

    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
        RepresentativeScreenCompact(
            uiState = uiState,
            onBackNavigation = onBackNavigation,
            onChange = onChange
        )
    } else {
        RepresentativeScreenExpanded(
            uiState = uiState,
            onBackNavigation = onBackNavigation,
            onChange = onChange
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RepresentativeScreenCompact(
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RepresentativeScreenExpanded(
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
            modifier = Modifier.paint(
                painter = painterResource(Res.drawable.atto_background_desktop),
                contentScale = ContentScale.FillBounds
            ),
            backgroundColor = Color.Transparent,
            content = {
                Box(Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .fillMaxHeight(0.8f)
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(50.dp))
                            .background(color = MaterialTheme.colors.surface)
                            .padding(top = 84.dp, bottom = 72.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(Res.string.representative_title),
                            style = MaterialTheme.typography.h4
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = stringResource(Res.string.representative_subtitle),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.W400,
                            fontFamily = attoFontFamily()
                        )

                        val displayAddress = uiState.representative
                            ?.let { address ->
                                address.substring(0, address.length / 2) +
                                    "\n" +
                                    address.substring(address.length / 2, address.length)
                            }

                        Text(
                            text = displayAddress.orEmpty(),
                            modifier = Modifier.clip(RoundedCornerShape(50.dp))
                                .background(MaterialTheme.colors.secondaryVariant)
                                .padding(
                                    vertical = 24.dp,
                                    horizontal = 72.dp
                                )
                        )

                        Spacer(Modifier.height(60.dp))

                        AttoButton(
                            onClick = {
                                coroutineScope.launch {
                                    sheetState.show()
                                }
                            },
                            modifier = Modifier.width(300.dp)
                        ) {
                            Text(text = stringResource(Res.string.representative_change))
                        }
                    }
                }
            }
        )
    }
}

@Preview
@Composable
fun RepresentativeScreenCompactPreview() {
    AttoWalletTheme {
        RepresentativeScreenCompact(
            uiState = RepresentativeUIState("atto://address"),
            onBackNavigation = {},
            onChange = { false }
        )
    }
}

@Preview
@Composable
fun RepresentativeScreenExpandedPreview() {
    AttoWalletTheme {
        RepresentativeScreenExpanded(
            uiState = RepresentativeUIState("atto://address"),
            onBackNavigation = {},
            onChange = { false }
        )
    }
}