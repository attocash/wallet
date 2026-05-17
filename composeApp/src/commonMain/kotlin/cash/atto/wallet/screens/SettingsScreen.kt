package cash.atto.wallet.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.SyncAlt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.components.common.AttoButtonVariant
import cash.atto.wallet.components.common.AttoCard
import cash.atto.wallet.components.common.AttoModal
import cash.atto.wallet.components.common.AttoPageFrame
import cash.atto.wallet.components.common.AttoPanelCard
import cash.atto.wallet.components.common.AttoTag
import cash.atto.wallet.config.AppVersion
import cash.atto.wallet.model.WorkSourcePreference
import cash.atto.wallet.ui.dark_accent
import cash.atto.wallet.ui.dark_border
import cash.atto.wallet.ui.dark_border_muted
import cash.atto.wallet.ui.dark_danger
import cash.atto.wallet.ui.dark_success
import cash.atto.wallet.ui.dark_text_dim
import cash.atto.wallet.ui.dark_text_primary
import cash.atto.wallet.ui.dark_text_secondary
import cash.atto.wallet.uistate.settings.SettingsUiState
import cash.atto.wallet.worker.isLocalWorkerSupported

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onBackClick: () -> Unit,
    onBackupClick: () -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onWorkSourceSelected: (WorkSourcePreference) -> Unit,
    onDismissPreferencesMessage: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    AttoPageFrame(
        title = "Settings",
        subtitle = "Manage wallet recovery, device security, and application information.",
        onBack = onBackClick,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val compact = maxWidth < 1040.dp

            if (compact) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    SettingsActionsPanel(
                        modifier = Modifier.fillMaxWidth(),
                        onBackupClick = onBackupClick,
                        onExportClick = onExportClick,
                        onImportClick = onImportClick,
                        workSource = uiState.workSource,
                        onWorkSourceSelected = onWorkSourceSelected,
                        onLogoutClick = onLogoutClick,
                    )
                    SettingsMetadataPanel(modifier = Modifier.fillMaxWidth())
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                ) {
                    SettingsActionsPanel(
                        modifier = Modifier.weight(5f).fillMaxSize(),
                        onBackupClick = onBackupClick,
                        onExportClick = onExportClick,
                        onImportClick = onImportClick,
                        workSource = uiState.workSource,
                        onWorkSourceSelected = onWorkSourceSelected,
                        onLogoutClick = onLogoutClick,
                    )
                    SettingsMetadataPanel(modifier = Modifier.weight(7f))
                }
            }
        }

        SettingsPreferencesMessageDialog(
            message = uiState.preferencesMessage,
            onDismiss = onDismissPreferencesMessage,
        )
    }
}

@Composable
private fun SettingsActionsPanel(
    modifier: Modifier,
    onBackupClick: () -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    workSource: WorkSourcePreference,
    onWorkSourceSelected: (WorkSourcePreference) -> Unit,
    onLogoutClick: () -> Unit,
) {
    var workerOptionsExpanded by remember { mutableStateOf(false) }
    var showLocalWorkerDialog by remember { mutableStateOf(false) }
    var localWorkerSupported by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        localWorkerSupported = isLocalWorkerSupported()
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AttoSettingsActionRow(
            icon = Icons.Outlined.Key,
            title = "Recovery Phrase",
            subtitle = "View your 24-word recovery phrase",
            onClick = onBackupClick,
        )

        WorkerSourceSection(
            selectedSource = workSource,
            expanded = workerOptionsExpanded,
            onToggleExpanded = { workerOptionsExpanded = !workerOptionsExpanded },
            onRemoteSelected = {
                onWorkSourceSelected(WorkSourcePreference.REMOTE)
            },
            onLocalSelected = {
                showLocalWorkerDialog = true
            },
            localWorkerSupported = localWorkerSupported,
        )

        AttoSettingsActionRow(
            icon = Icons.Outlined.Download,
            title = "Export Preferences",
            subtitle = "Download address and transaction labels",
            onClick = onExportClick,
        )

        AttoSettingsActionRow(
            icon = Icons.Outlined.SyncAlt,
            title = "Import Preferences",
            subtitle = "Replace saved address and transaction labels",
            onClick = onImportClick,
        )

        AttoSettingsActionRow(
            icon = Icons.AutoMirrored.Outlined.Logout,
            title = "Logout",
            subtitle = "Clear keys and return to welcome screen",
            accent = dark_danger,
            onClick = onLogoutClick,
        )
    }

    if (showLocalWorkerDialog) {
        LocalWorkerEvaluationDialog(
            localWorkerSupported = localWorkerSupported,
            onDismiss = { showLocalWorkerDialog = false },
            onConfirm = {
                onWorkSourceSelected(WorkSourcePreference.LOCAL)
                showLocalWorkerDialog = false
                workerOptionsExpanded = false
            },
        )
    }
}

@Composable
private fun WorkerSourceSection(
    selectedSource: WorkSourcePreference,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onRemoteSelected: () -> Unit,
    onLocalSelected: () -> Unit,
    localWorkerSupported: Boolean?,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AttoSettingsActionRow(
            icon = Icons.Outlined.Memory,
            title = "Work Source",
            subtitle = "${selectedSource.label()} worker selected",
            trailingIcon = if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
            onClick = onToggleExpanded,
        )

        if (expanded) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                WorkerSourceOptionCard(
                    icon = Icons.Outlined.CloudQueue,
                    title = "Remote",
                    subtitle = "Very fast, but rate limited.",
                    selected = selectedSource == WorkSourcePreference.REMOTE,
                    tag = if (selectedSource == WorkSourcePreference.REMOTE) "Selected" else "Default",
                    accent = dark_accent,
                    onClick = onRemoteSelected,
                )

                WorkerSourceOptionCard(
                    icon = Icons.Outlined.Computer,
                    title = "Local (Preview)",
                    subtitle =
                        when (localWorkerSupported) {
                            true -> "Uses this browser for work after a hardware fit check."
                            false -> "Local web worker is unavailable in this browser."
                            null -> "Checking local web worker support."
                        },
                    selected = selectedSource == WorkSourcePreference.LOCAL,
                    tag =
                        when {
                            localWorkerSupported == null -> "Checking"
                            !localWorkerSupported -> "Unavailable"
                            selectedSource == WorkSourcePreference.LOCAL -> "Selected"
                            else -> "Check hardware"
                        },
                    accent = dark_success,
                    enabled = localWorkerSupported == true,
                    onClick = onLocalSelected,
                )
            }
        }
    }
}

@Composable
private fun WorkerSourceOptionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    tag: String,
    accent: Color,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) accent else dark_border
    val contentAlpha = if (enabled) 1f else 0.52f

    AttoCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        border = borderColor,
        hoverBorder = if (selected) accent else dark_border_muted,
        onClick = if (enabled) onClick else null,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accent.copy(alpha = if (selected) 0.16f else 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accent,
                    modifier = Modifier.size(19.dp),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        color = dark_text_primary.copy(alpha = contentAlpha),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.W700),
                    )
                    AttoTag(
                        text = tag,
                        color = if (selected && enabled) accent else dark_text_dim,
                    )
                }
                Text(
                    text = subtitle,
                    color = dark_text_secondary.copy(alpha = contentAlpha),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun SettingsPreferencesMessageDialog(
    message: String?,
    onDismiss: () -> Unit,
) {
    message?.let {
        AttoModal(
            title = "Preferences",
            onDismiss = onDismiss,
        ) {
            Text(
                text = it,
                color = dark_text_secondary,
                style = MaterialTheme.typography.bodyMedium,
            )
            AttoButton(
                text = "Close",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                variant = AttoButtonVariant.Outlined,
            )
        }
    }
}

@Composable
private fun SettingsMetadataPanel(modifier: Modifier) {
    AttoPanelCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        SettingsInfoRow(
            label = "Version",
            value = AppVersion.value,
            valueColor = dark_text_primary,
        )
        HorizontalDivider(color = dark_border)
        SettingsInfoRow(
            label = "Network",
            value = "Live",
            valueColor = dark_success,
        )
    }
}

@Composable
private fun SettingsInfoRow(
    label: String,
    value: String,
    valueColor: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            color = dark_text_secondary,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.W500),
        )
        Text(
            text = value,
            color = valueColor,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.W500),
        )
    }
}

@Composable
private fun AttoSettingsActionRow(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    accent: Color = dark_text_primary,
    trailingIcon: ImageVector = Icons.Outlined.ChevronRight,
    onClick: () -> Unit,
) {
    AttoCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accent,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    color = dark_text_primary,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W600),
                )
                subtitle?.let {
                    Text(
                        text = it,
                        color = dark_text_secondary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = if (accent == dark_danger) dark_danger else dark_text_dim,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

private fun WorkSourcePreference.label(): String =
    when (this) {
        WorkSourcePreference.REMOTE -> "Remote"
        WorkSourcePreference.LOCAL -> "Local"
    }
