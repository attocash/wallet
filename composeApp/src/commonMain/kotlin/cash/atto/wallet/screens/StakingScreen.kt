package cash.atto.wallet.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cash.atto.wallet.components.common.*
import cash.atto.wallet.model.Voter
import cash.atto.wallet.model.calculateEntityWeightPercentage
import cash.atto.wallet.ui.*
import cash.atto.wallet.uistate.settings.VoterUIState
import cash.atto.wallet.viewmodel.VoterViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

private val warningColor = Color(0xFFFF9800)

@Composable
fun StakingScreen(onBackClick: () -> Unit) {
    val viewModel = koinViewModel<VoterViewModel>()
    val uiState by viewModel.state.collectAsState()

    StakingContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onChangeVoter = { address -> viewModel.setVoter(address) },
    )
}

@Composable
private fun StakingContent(
    uiState: VoterUIState,
    onBackClick: () -> Unit,
    onChangeVoter: suspend (String) -> Boolean,
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedVoter by remember { mutableStateOf<Voter?>(null) }

    AttoPageFrame(
        title = "Staking",
        subtitle = "Delegate your voting weight to earn rewards",
        onBack = onBackClick,
    ) {
        selectedVoter?.let { voter ->
            val globalApy = uiState.globalApy?.toDoubleOrNull() ?: 0.0
            val voterApy = globalApy * voter.sharePercentage / 100.0

            StakingConfirmDialog(
                voterLabel = voter.label,
                voterAddress = voter.address,
                voterApr = "${((voterApy * 100).toLong() / 100.0)}%",
                voterUptime = formatLastVoted(voter.lastVotedAt),
                onDismiss = { selectedVoter = null },
                onConfirm = {
                    coroutineScope.launch {
                        if (onChangeVoter(voter.address)) {
                            selectedVoter = null
                        }
                    }
                },
            )
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = dark_accent)
            }
        } else {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val compact = isCompactWidth()

                val currentVoter = uiState.voters.find { it.address == uiState.currentVoter }
                val currentVoterHealthy =
                    currentVoter?.let {
                        val globalApy = uiState.globalApy?.toDoubleOrNull() ?: 0.0
                        !hasVoterWarning(it, globalApy, uiState.voters)
                    } ?: false

                if (compact) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        StakingCurrentCard(
                            modifier = Modifier.fillMaxWidth(),
                            voterLabel = uiState.currentVoterLabel ?: "Unknown node",
                            voterAddress = uiState.currentVoter.orEmpty(),
                            apr = uiState.currentVoterApy?.let { "${it.toPlainString()}%" } ?: "—",
                            weight = currentVoter?.voteWeightPercentage?.let { "${it.toPlainString()}%" } ?: "—",
                            entityWeight = uiState.currentVoterEntityWeightPercentage?.let { "${it.toPlainString()}%" }
                                ?: "—",
                            lastVoted = uiState.currentVoterLastVotedAt?.let { formatLastVoted(it) } ?: "—",
                            healthy = currentVoterHealthy,
                            onChangeClick = { selectedVoter = currentVoter },
                        )
                        StakingInfoCard(modifier = Modifier.fillMaxWidth())
                        StakingVotersSection(
                            modifier = Modifier.fillMaxWidth(),
                            uiState = uiState,
                            onSelect = { selectedVoter = it },
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        Column(
                            modifier = Modifier.weight(5f),
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                        ) {
                            StakingCurrentCard(
                                modifier = Modifier.fillMaxWidth(),
                                voterLabel = uiState.currentVoterLabel ?: "Unknown node",
                                voterAddress = uiState.currentVoter.orEmpty(),
                                apr = uiState.currentVoterApy?.let { "${it.toPlainString()}%" } ?: "—",
                                weight = currentVoter?.voteWeightPercentage?.let { "${it.toPlainString()}%" } ?: "—",
                                entityWeight = uiState.currentVoterEntityWeightPercentage?.let { "${it.toPlainString()}%" }
                                    ?: "—",
                                lastVoted = uiState.currentVoterLastVotedAt?.let { formatLastVoted(it) } ?: "—",
                                healthy = currentVoterHealthy,
                                onChangeClick = { selectedVoter = currentVoter },
                            )
                            StakingInfoCard(modifier = Modifier.fillMaxWidth())
                        }
                        StakingVotersSection(
                            modifier = Modifier.weight(7f),
                            uiState = uiState,
                            onSelect = { selectedVoter = it },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StakingCurrentCard(
    modifier: Modifier,
    voterLabel: String,
    voterAddress: String,
    apr: String,
    weight: String,
    entityWeight: String,
    lastVoted: String,
    healthy: Boolean,
    onChangeClick: () -> Unit,
) {
    AttoPanelCard(modifier = modifier, contentPadding = PaddingValues(20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Current Delegation",
                color = dark_accent,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.W700),
            )
            Text(
                text = voterLabel,
                color = dark_text_primary,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.W600),
            )
            Text(
                text = voterAddress,
                color = dark_text_dim,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        StakingMetricRow("APR", apr, dark_success)
        StakingMetricRow("Voting Weight", weight)
        StakingMetricRow("Entity Weight", entityWeight)
        StakingMetricRow("Last Voted", lastVoted, if (healthy) dark_success else dark_accent, showDivider = false)
    }
}

@Composable
private fun StakingInfoCard(modifier: Modifier) {
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(8.dp))
                .background(dark_accent.copy(alpha = 0.03f))
                .border(1.dp, dark_accent.copy(alpha = 0.19f), RoundedCornerShape(8.dp))
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "What is Staking?",
            color = dark_accent,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.W700),
        )
        Text(
            text =
                "Delegate your voting weight to a voter node to earn rewards. " +
                        "Voters help secure the network by validating transactions.",
            color = dark_text_secondary,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun StakingVotersSection(
    modifier: Modifier,
    uiState: VoterUIState,
    onSelect: (Voter) -> Unit,
) {
    var showHealthyOnly by remember { mutableStateOf(true) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Available Voters",
                    color = dark_text_primary,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W600),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "Healthy Only",
                        color = if (showHealthyOnly) dark_accent else dark_text_tertiary,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Switch(
                        checked = showHealthyOnly,
                        onCheckedChange = { showHealthyOnly = it },
                        colors =
                            SwitchDefaults.colors(
                                checkedThumbColor = dark_accent,
                                checkedTrackColor = dark_accent.copy(alpha = 0.3f),
                                uncheckedThumbColor = dark_text_tertiary,
                                uncheckedTrackColor = dark_surface_alt,
                                uncheckedBorderColor = dark_border,
                            ),
                    )
                }
            }
            Text(
                text = "Select a voter to delegate your voting weight",
                color = dark_text_tertiary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val globalApy = uiState.globalApy?.toDoubleOrNull() ?: 0.0
            val filteredVoters =
                if (showHealthyOnly) {
                    uiState.voters.filter { voter ->
                        !hasVoterWarning(voter, globalApy, uiState.voters)
                    }
                } else {
                    uiState.voters
                }
            filteredVoters.forEach { voter ->
                val voterApy = globalApy * voter.sharePercentage / 100.0

                StakingVoterCard(
                    voter = voter,
                    allVoters = uiState.voters,
                    calculatedApy = voterApy,
                    selected = voter.address == uiState.currentVoter,
                    onClick = { onSelect(voter) },
                )
            }
        }
    }
}

@Composable
private fun StakingVoterCard(
    voter: Voter,
    allVoters: List<Voter>,
    calculatedApy: Double,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val now = Clock.System.now()
    val hasNotVotedIn24H = (now - voter.lastVotedAt) > 1.days
    val entityWeight = voter.calculateEntityWeightPercentage(allVoters)
    val weightAbove1Percent = entityWeight.doubleValue(false) > 1.0
    val apyIsZero = calculatedApy == 0.0
    val hasWarning = hasNotVotedIn24H || weightAbove1Percent || apyIsZero
    val isHealthy = !hasWarning

    AttoCard(
        contentPadding = PaddingValues(16.dp),
        background = dark_surface,
        hoverBackground = dark_surface_alt,
        onClick = onClick,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = voter.label,
                        color = dark_text_primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.W600),
                    )
                    Icon(
                        imageVector = if (isHealthy) Icons.Outlined.CheckCircle else Icons.Outlined.WarningAmber,
                        contentDescription = null,
                        tint = if (isHealthy) dark_success else dark_accent,
                        modifier = Modifier.size(16.dp),
                    )
                    if (selected) {
                        Box(
                            modifier =
                                Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(dark_accent_soft)
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = "Current",
                                color = dark_accent,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.W700),
                            )
                        }
                    }
                }
                Text(
                    text = voter.address,
                    color = dark_text_dim,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                StakingMiniStat(
                    Modifier.weight(1f),
                    "APR",
                    "${kotlin.math.round(calculatedApy * 10) / 10.0}%",
                    if (apyIsZero) warningColor else dark_success,
                )
                StakingMiniStat(
                    Modifier.weight(1f),
                    "Weight",
                    "${voter.voteWeightPercentage.toPlainString()}%",
                    if (weightAbove1Percent) warningColor else dark_text_primary,
                )
                StakingMiniStat(
                    Modifier.weight(1f),
                    "Last Voted",
                    formatLastVoted(voter.lastVotedAt),
                    if (hasNotVotedIn24H) warningColor else dark_text_primary,
                )
            }
        }
    }
}

@Composable
private fun StakingConfirmDialog(
    voterLabel: String,
    voterAddress: String,
    voterApr: String,
    voterUptime: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AttoModal(
        title = "Change Voter",
        onDismiss = onDismiss,
        desktopWidth = 448.dp,
        contentPadding = PaddingValues(20.dp),
        contentSpacing = 24.dp,
    ) {
        AttoCopyField(
            label = "VOTER",
            value = voterAddress,
            maxLines = 2,
        )

        AttoDetailField(
            label = "LABEL",
            value = voterLabel,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AttoDetailField(
                label = "APR",
                value = voterApr,
                modifier = Modifier.weight(1f),
            )
            AttoDetailField(
                label = "LAST VOTED",
                value = voterUptime,
                modifier = Modifier.weight(1f),
            )
        }

        HorizontalDivider(color = dark_border)

        Text(
            text = "A change transaction will be created. You can change your voter at any time.",
            color = dark_text_secondary,
            style = MaterialTheme.typography.bodyMedium,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AttoButton(
                text = "Confirm",
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StakingMetricRow(
    label: String,
    value: String,
    valueColor: Color = dark_text_primary,
    showDivider: Boolean = true,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                color = dark_text_secondary,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = value,
                color = valueColor,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.W600),
            )
        }
        if (showDivider) {
            HorizontalDivider(color = dark_border)
        }
    }
}

@Composable
private fun StakingMiniStat(
    modifier: Modifier,
    label: String,
    value: String,
    valueColor: Color,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            color = dark_text_dim,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.W600),
        )
        Text(
            text = value,
            color = valueColor,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.W600),
        )
    }
}

private fun formatLastVoted(instant: kotlin.time.Instant): String {
    val now = Clock.System.now()
    val duration = now - instant
    return when {
        duration < 1.minutes -> "Just now"
        duration < 1.hours -> "${duration.inWholeMinutes}m ago"
        duration < 1.days -> "${duration.inWholeHours}h ago"
        duration < 7.days -> "${duration.inWholeDays}d ago"
        else -> "${duration.inWholeDays / 7}w ago"
    }
}

private fun hasVoterWarning(
    voter: Voter,
    globalApy: Double,
    allVoters: List<Voter>,
): Boolean {
    val now = Clock.System.now()
    val hasNotVotedIn24H = (now - voter.lastVotedAt) > 1.days
    val entityWeight = voter.calculateEntityWeightPercentage(allVoters)
    val weightAbove1Percent = entityWeight.doubleValue(false) > 1.0
    val voterApy = globalApy * voter.sharePercentage / 100.0
    val apyIsZero = voterApy == 0.0
    return hasNotVotedIn24H || weightAbove1Percent || apyIsZero
}
