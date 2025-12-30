package cash.atto.wallet.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.*
import cash.atto.wallet.components.common.AppBar
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.components.settings.EnterVoterBottomSheet
import cash.atto.wallet.model.Voter
import cash.atto.wallet.ui.*
import cash.atto.wallet.uistate.settings.VoterUIState
import cash.atto.wallet.viewmodel.VoterViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant


@Composable
fun VoterScreen(
    onBackNavigation: () -> Unit,
    onVoterClick: (String) -> Unit = {}
) {
    val viewModel = koinViewModel<VoterViewModel>()
    val uiState = viewModel.state.collectAsState()

    VoterScreenContent(
        uiState = uiState.value,
        onBackNavigation = onBackNavigation,
        onVoterClick = onVoterClick,
        onChange = {
            viewModel.setVoter(it)
        }
    )
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun VoterScreenContent(
    uiState: VoterUIState,
    onBackNavigation: () -> Unit,
    onVoterClick: (String) -> Unit,
    onChange: suspend (String) -> Boolean,
) {
    val windowSizeClass = calculateWindowSizeClass()

    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
        VoterScreenCompact(
            uiState = uiState,
            onBackNavigation = onBackNavigation,
            onVoterClick = onVoterClick,
            onChange = onChange
        )
    } else {
        VoterScreenExpanded(
            uiState = uiState,
            onBackNavigation = onBackNavigation,
            onVoterClick = onVoterClick,
            onChange = onChange
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoterScreenCompact(
    uiState: VoterUIState,
    onBackNavigation: () -> Unit,
    onVoterClick: (String) -> Unit,
    onChange: suspend (String) -> Boolean,
) {
    val coroutineScope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    var showBottomSheet by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        if (showBottomSheet) {
            EnterVoterBottomSheet(
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
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
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
                        item {
                            Text(
                                text = stringResource(Res.string.staking_title),
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }

                        // Staking Info Section
                        item {
                            StakingInfoCard(
                                title = stringResource(Res.string.staking_info_title),
                                description = stringResource(Res.string.staking_info_description)
                            )
                        }

                        // APY Info Section
                        item {
                            StakingInfoCard(
                                title = stringResource(Res.string.staking_global_apy) + ": ${uiState.globalApy ?: "—"}%",
                                description = stringResource(Res.string.staking_apy_info)
                            )
                        }

                        // Current Voter Section
                        item {
                            CurrentVoterCard(
                                voterLabel = uiState.currentVoterLabel
                                    ?: stringResource(Res.string.staking_unknown_voter),
                                voterAddress = uiState.currentVoter.orEmpty(),
                                userApy = uiState.currentVoterApy,
                                currentVoterWeightPercentage = uiState.currentVoterWeightPercentage,
                                currentVoterLastVotedAt = uiState.currentVoterLastVotedAt,
                                onChangeClick = {
                                    coroutineScope.launch {
                                        showBottomSheet = true
                                    }
                                }
                            )
                        }

                        if (uiState.voters.isNotEmpty()) {
                            item {
                                Text(
                                    text = stringResource(Res.string.staking_voters_list),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                )
                            }

                            items(uiState.voters) { voter ->
                                val globalApy = uiState.globalApy?.toDoubleOrNull() ?: 0.0
                                val voterApy = globalApy * voter.sharePercentage / 100.0
                                VoterCard(
                                    voter = voter,
                                    calculatedApy = voterApy,
                                    isSelected = voter.address == uiState.currentVoter,
                                    onClick = { onVoterClick(voter.address) }
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoterScreenExpanded(
    uiState: VoterUIState,
    onBackNavigation: () -> Unit,
    onVoterClick: (String) -> Unit,
    onChange: suspend (String) -> Boolean,
) {
    val coroutineScope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedVoterAddress by remember { mutableStateOf<String?>(null) }

    val selectedVoter = selectedVoterAddress?.let { address ->
        uiState.voters.find { it.address == address }
    }
    val globalApy = uiState.globalApy?.toDoubleOrNull() ?: 0.0
    val selectedVoterApy = selectedVoter?.let { globalApy * it.sharePercentage / 100.0 } ?: 0.0

    Box(Modifier.fillMaxSize()) {
        if (showBottomSheet) {
            EnterVoterBottomSheet(
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
            topBar = {
                AppBar(
                    onBackNavigation = {
                        if (selectedVoterAddress != null) {
                            selectedVoterAddress = null
                        } else {
                            onBackNavigation()
                        }
                    }
                )
            },
            modifier = Modifier.paint(
                painter = painterResource(Res.drawable.atto_background_desktop),
                contentScale = ContentScale.FillBounds
            ),
            containerColor = Color.Transparent,
            content = {
                Box(Modifier.fillMaxSize()) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else if (selectedVoter != null) {
                        // Show voter detail overlay
                        VoterDetailContent(
                            voter = selectedVoter,
                            calculatedApy = selectedVoterApy,
                            onConfirm = {
                                coroutineScope.launch {
                                    onChange(selectedVoter.address)
                                    selectedVoterAddress = null
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .fillMaxHeight(0.9f)
                                .align(Alignment.Center)
                                .clip(RoundedCornerShape(50.dp))
                                .background(color = MaterialTheme.colorScheme.surface)
                                .padding(48.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .fillMaxHeight(0.9f)
                                .align(Alignment.Center)
                                .clip(RoundedCornerShape(50.dp))
                                .background(color = MaterialTheme.colorScheme.surface)
                                .padding(
                                    horizontal = 48.dp,
                                    vertical = 48.dp
                                ),
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            item {
                                Text(
                                    text = stringResource(Res.string.staking_title),
                                    style = MaterialTheme.typography.headlineLarge
                                )
                            }

                            // Staking Info Section
                            item {
                                StakingInfoCard(
                                    title = stringResource(Res.string.staking_info_title),
                                    description = stringResource(Res.string.staking_info_description)
                                )
                            }

                            // APY Info Section
                            item {
                                StakingInfoCard(
                                    title = stringResource(Res.string.staking_global_apy) + ": ${uiState.globalApy ?: "—"}%",
                                    description = stringResource(Res.string.staking_apy_info)
                                )
                            }

                            // Current Voter Section
                            item {
                                CurrentVoterCard(
                                    voterLabel = uiState.currentVoterLabel
                                        ?: stringResource(Res.string.staking_unknown_voter),
                                    voterAddress = uiState.currentVoter.orEmpty(),
                                    userApy = uiState.currentVoterApy,
                                    currentVoterWeightPercentage = uiState.currentVoterWeightPercentage,
                                    currentVoterLastVotedAt = uiState.currentVoterLastVotedAt,
                                    onChangeClick = {
                                        coroutineScope.launch {
                                            showBottomSheet = true
                                        }
                                    }
                                )
                            }

                            // Voters List Section
                            if (uiState.voters.isNotEmpty()) {
                                item {
                                    Text(
                                        text = stringResource(Res.string.staking_voters_list),
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp)
                                    )
                                }

                                items(uiState.voters) { voter ->
                                    val voterApy = globalApy * voter.sharePercentage / 100.0
                                    VoterCard(
                                        voter = voter,
                                        calculatedApy = voterApy,
                                        isSelected = voter.address == uiState.currentVoter,
                                        onClick = { selectedVoterAddress = voter.address }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun StakingInfoCard(
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun CurrentVoterCard(
    voterLabel: String,
    voterAddress: String,
    userApy: Double?,
    currentVoterWeightPercentage: Double?,
    currentVoterLastVotedAt: Instant?,
    onChangeClick: () -> Unit
) {
    val weightAbove1Percent = (currentVoterWeightPercentage ?: 0.0) > 1.0

    // Warning conditions
    val now = Clock.System.now()
    val hasNotVotedIn24H = currentVoterLastVotedAt?.let {
        (now - it) > 24.hours
    } ?: false
    val apyIsZero = userApy == null || userApy == 0.0

    val hasWarning = hasNotVotedIn24H || weightAbove1Percent || apyIsZero

    // Warning color
    val warningColor = Color(0xFFFF9800)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(Res.string.staking_current_voter),
                style = MaterialTheme.typography.labelMedium
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = voterLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (hasWarning) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = warningColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Text(
                text = voterAddress,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Weight and Last Voted row (similar to VoterCard)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (userApy != null) {
                    val formattedApy = (kotlin.math.round(userApy * 100) / 100).toString()
                    Text(
                        text = "APY: $formattedApy%",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (weightAbove1Percent) warningColor else green_700
                    )
                }
                if (currentVoterWeightPercentage != null) {
                    val formattedWeight = (kotlin.math.round(currentVoterWeightPercentage * 100) / 100).toString()
                    Text(
                        text = "Weight: $formattedWeight%",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (weightAbove1Percent) warningColor else green_700
                    )
                }
                if (currentVoterLastVotedAt != null) {
                    val lastVotedText = AttoDateFormatter.formatDate(currentVoterLastVotedAt)
                    Text(
                        text = "Last voted: $lastVotedText",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (hasNotVotedIn24H) warningColor else green_700
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            AttoButton(
                onClick = onChangeClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(Res.string.staking_change))
            }
        }
    }
}

@Composable
fun VoterCard(
    voter: Voter,
    calculatedApy: Double,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val lastVotedAtFormatted = voter.lastVotedAtFormatted
    val supplyPercentage = voter.voteWeightPercentage.toPlainString()

    // Warning conditions
    val now = Clock.System.now()
    val timeSinceLastVote = now - (voter.lastVotedAt ?: Instant.fromEpochMilliseconds(0))
    val hasNotVotedIn24H = timeSinceLastVote > 24.hours
    val weightAbove1Percent = voter.voteWeightPercentage.doubleValue(false) > 1.0
    val apyIsZero = calculatedApy == 0.0

    val hasWarning = hasNotVotedIn24H || weightAbove1Percent || apyIsZero

    // Warning color
    val warningColor = Color(0xFFFF9800) // Orange/Amber color for warnings

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                hasWarning -> warningColor.copy(alpha = 0.15f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = voter.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (hasWarning) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = warningColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val formattedApy = (kotlin.math.round(calculatedApy * 100) / 100).toString()
                    Text(
                        text = "APY: $formattedApy%",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (weightAbove1Percent) warningColor else green_700
                    )
                    Text(
                        text = "Weight: $supplyPercentage%",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (weightAbove1Percent) warningColor else green_700
                    )
                    Text(
                        text = "Last voted: $lastVotedAtFormatted",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (hasNotVotedIn24H) warningColor else green_700
                    )
                }
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview
@Composable
fun VoterScreenCompactPreview() {
    AttoWalletTheme {
        VoterScreenCompact(
            uiState = VoterUIState("atto://address"),
            onBackNavigation = {},
            onVoterClick = {},
            onChange = { false }
        )
    }
}

@Preview
@Composable
fun VoterScreenExpandedPreview() {
    AttoWalletTheme {
        VoterScreenExpanded(
            uiState = VoterUIState("atto://address"),
            onBackNavigation = {},
            onVoterClick = {},
            onChange = { false }
        )
    }
}