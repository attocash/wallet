package cash.atto.wallet.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Brush
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
import cash.atto.wallet.components.common.AttoOutlinedTextCard
import cash.atto.wallet.components.settings.EnterRepresentativeBottomSheet
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.BottomSheetShape
import cash.atto.wallet.ui.attoFontFamily
import cash.atto.wallet.ui.primaryGradient
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepresentativeScreenCompact(
    uiState: RepresentativeUIState,
    onBackNavigation: () -> Unit,
    onChange: suspend (String) -> Boolean,
) {
    val coroutineScope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    var showBottomSheet by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        if (showBottomSheet) {
            EnterRepresentativeBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                onChange = {
                    coroutineScope.launch {
                        if (onChange.invoke(it))
                            showBottomSheet = false
                    }
                },
                onClose = {
                    coroutineScope.launch {
                        showBottomSheet = false
                    }
                },
                showError = uiState.showError
            )
        }

        Scaffold(
            topBar = { AppBar(onBackNavigation) },
            modifier = Modifier.background(
                brush = Brush.horizontalGradient(
                    colors = MaterialTheme.colorScheme.primaryGradient
                )
            ),
            containerColor = Color.Transparent,
            content = { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding() + 16.dp)
                        .clip(BottomSheetShape)
                        .background(color = MaterialTheme.colorScheme.secondary)
                        .padding(
                            start = 16.dp,
                            top = 24.dp,
                            end = 16.dp,
                            bottom = innerPadding.calculateBottomPadding() + 16.dp
                        ),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(Res.string.representative_title),
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Text(
                        text = stringResource(Res.string.representative_subtitle),
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    AttoOutlinedTextCard(
                        text = uiState.representative.orEmpty(),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.weight(1f))

                    AttoButton(
                        onClick = {
                            coroutineScope.launch {
                                showBottomSheet = true
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepresentativeScreenExpanded(
    uiState: RepresentativeUIState,
    onBackNavigation: () -> Unit,
    onChange: suspend (String) -> Boolean,
) {

    val coroutineScope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    var showBottomSheet by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        if (showBottomSheet) {
            EnterRepresentativeBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                onChange = {
                    coroutineScope.launch {
                        if (onChange.invoke(it))
                            showBottomSheet = false
                    }
                },
                onClose = {
                    coroutineScope.launch {
                        showBottomSheet = false
                    }
                },
                showError = uiState.showError
            )
        }

        Scaffold(
            topBar = { AppBar(onBackNavigation) },
            modifier = Modifier.paint(
                painter = painterResource(Res.drawable.atto_background_desktop),
                contentScale = ContentScale.FillBounds
            ),
            containerColor = Color.Transparent,
            content = {
                Box(Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .fillMaxHeight(0.8f)
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(50.dp))
                            .background(color = MaterialTheme.colorScheme.surface)
                            .padding(top = 84.dp, bottom = 72.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(Res.string.representative_title),
                            style = MaterialTheme.typography.headlineLarge
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
                                .background(MaterialTheme.colorScheme.secondary)
                                .padding(
                                    vertical = 24.dp,
                                    horizontal = 72.dp
                                )
                        )

                        Spacer(Modifier.height(60.dp))

                        AttoButton(
                            onClick = {
                                coroutineScope.launch {
                                    showBottomSheet = true
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