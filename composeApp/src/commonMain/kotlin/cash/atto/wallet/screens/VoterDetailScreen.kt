package cash.atto.wallet.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.*
import cash.atto.wallet.components.common.AppBar
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.model.Voter
import cash.atto.wallet.model.calculateEntityWeightPercentage
import cash.atto.wallet.ui.*
import cash.atto.wallet.viewmodel.VoterViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

@Composable
fun VoterDetailScreen(
    voterAddress: String,
    onBackNavigation: () -> Unit,
    onConfirm: () -> Unit
) {
    val viewModel = koinViewModel<VoterViewModel>()
    val uiState = viewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val voter = uiState.value.voters.find { it.address == voterAddress }
    val globalApy = uiState.value.globalApy?.toDoubleOrNull() ?: 0.0
    val calculatedApy = voter?.let { globalApy * it.sharePercentage / 100.0 } ?: 0.0

    VoterDetailScreenContent(
        voter = voter,
        allVoters = uiState.value.voters,
        calculatedApy = calculatedApy,
        isLoading = uiState.value.isLoading,
        onBackNavigation = onBackNavigation,
        onConfirm = {
            coroutineScope.launch {
                if (voter != null) {
                    viewModel.setVoter(voter.address)
                }
                onConfirm()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun VoterDetailScreenContent(
    voter: Voter?,
    allVoters: List<Voter>,
    calculatedApy: Double,
    isLoading: Boolean,
    onBackNavigation: () -> Unit,
    onConfirm: () -> Unit
) {
    val windowSizeClass = calculateWindowSizeClass()

    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
        VoterDetailScreenCompact(
            voter = voter,
            allVoters = allVoters,
            calculatedApy = calculatedApy,
            isLoading = isLoading,
            onBackNavigation = onBackNavigation,
            onConfirm = onConfirm
        )
    } else {
        VoterDetailScreenExpanded(
            voter = voter,
            allVoters = allVoters,
            calculatedApy = calculatedApy,
            isLoading = isLoading,
            onBackNavigation = onBackNavigation,
            onConfirm = onConfirm
        )
    }
}

@Composable
fun VoterDetailScreenCompact(
    voter: Voter?,
    allVoters: List<Voter>,
    calculatedApy: Double,
    isLoading: Boolean,
    onBackNavigation: () -> Unit,
    onConfirm: () -> Unit
) {
    Scaffold(
        topBar = { AppBar(onBackNavigation) },
        modifier = Modifier.background(
            brush = Brush.horizontalGradient(
                colors = MaterialTheme.colorScheme.primaryGradient
            )
        ),
        containerColor = Color.Transparent,
        content = { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 6.dp)
                        .clip(BottomSheetShape)
                        .background(color = MaterialTheme.colorScheme.surface)
                ) {
                    if (isLoading || voter == null) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        VoterDetailContent(
                            voter = voter,
                            allVoters = allVoters,
                            calculatedApy = calculatedApy,
                            onConfirm = onConfirm,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun VoterDetailScreenExpanded(
    voter: Voter?,
    allVoters: List<Voter>,
    calculatedApy: Double,
    isLoading: Boolean,
    onBackNavigation: () -> Unit,
    onConfirm: () -> Unit
) {
    Scaffold(
        topBar = { AppBar(onBackNavigation) },
        modifier = Modifier.paint(
            painter = painterResource(Res.drawable.atto_background_desktop),
            contentScale = ContentScale.FillBounds
        ),
        containerColor = Color.Transparent,
        content = {
            Box(Modifier.fillMaxSize()) {
                if (isLoading || voter == null) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    VoterDetailContent(
                        voter = voter,
                        allVoters = allVoters,
                        calculatedApy = calculatedApy,
                        onConfirm = onConfirm,
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .fillMaxHeight(0.85f)
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(50.dp))
                            .background(color = MaterialTheme.colorScheme.surface)
                            .padding(48.dp)
                    )
                }
            }
        }
    )
}

@Composable
fun VoterDetailContent(
    voter: Voter,
    allVoters: List<Voter>,
    calculatedApy: Double,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val lastVotedAtFormatted = AttoDateFormatter.formatRelativeDate(voter.lastVotedAt)
    val nodeWeightPercentage = voter.voteWeightPercentage.toPlainString()
    val entityWeightPercentage = voter.calculateEntityWeightPercentage(allVoters).toPlainString()

    Column(
        modifier = modifier.verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.voter_detail_title),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(8.dp))

        // Voter Name
        Text(
            text = voter.label,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

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
                    text = voter.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailRow(
                    label = stringResource(Res.string.voter_detail_entity),
                    value = voter.entity
                )

                DetailRow(
                    label = stringResource(Res.string.voter_detail_share),
                    value = "${voter.sharePercentage}%"
                )

                val formattedApy = (kotlin.math.round(calculatedApy * 100) / 100).toString()
                DetailRow(
                    label = stringResource(Res.string.voter_detail_apy),
                    value = "$formattedApy%",
                    valueColor = MaterialTheme.colorScheme.primary
                )

                DetailRow(
                    label = stringResource(Res.string.voter_detail_entity_weight),
                    value = "$entityWeightPercentage%"
                )

                DetailRow(
                    label = stringResource(Res.string.voter_detail_voter_weight),
                    value = "$nodeWeightPercentage%"
                )

                DetailRow(
                    label = stringResource(Res.string.voter_detail_last_voted),
                    value = lastVotedAtFormatted
                )
            }
        }

        // Address Card
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
                    text = stringResource(Res.string.voter_detail_address),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = voter.address,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // Confirm Button
        AttoButton(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(Res.string.voter_detail_confirm))
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Preview
@Composable
fun VoterDetailScreenCompactPreview() {
    val testVoter = Voter(
        address = "atto://address123",
        label = "Test Voter",
        entity = "Test Entity",
        sharePercentage = 80,
        addedAt = "2024-01-01",
        description = "This is a test voter description",
        voteWeight = "1000000000000000000000000000",
        lastVotedAt = Clock.System.now()
    )
    AttoWalletTheme {
        VoterDetailScreenCompact(
            voter = testVoter,
            allVoters = listOf(testVoter),
            calculatedApy = 5.5,
            isLoading = false,
            onBackNavigation = {},
            onConfirm = {}
        )
    }
}

@Preview
@Composable
fun VoterDetailScreenExpandedPreview() {
    val testVoter = Voter(
        address = "atto://address123",
        label = "Test Voter",
        entity = "Test Entity",
        sharePercentage = 80,
        addedAt = "2024-01-01",
        description = "This is a test voter description",
        voteWeight = "1000000000000000000000000000",
        lastVotedAt = Clock.System.now()
    )
    AttoWalletTheme {
        VoterDetailScreenExpanded(
            voter = testVoter,
            allVoters = listOf(testVoter),
            calculatedApy = 5.5,
            isLoading = false,
            onBackNavigation = {},
            onConfirm = {}
        )
    }
}
