package cash.atto.wallet.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.SyncAlt
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cash.atto.commons.AttoNetwork
import cash.atto.commons.worker.evaluate
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.components.common.AttoButtonVariant
import cash.atto.wallet.components.common.AttoCapsLabel
import cash.atto.wallet.components.common.AttoCard
import cash.atto.wallet.components.common.AttoCheckbox
import cash.atto.wallet.components.common.AttoModal
import cash.atto.wallet.components.common.AttoPageFrame
import cash.atto.wallet.components.common.AttoPanelCard
import cash.atto.wallet.components.common.AttoTag
import cash.atto.wallet.config.AppVersion
import cash.atto.wallet.model.WorkSourcePreference
import cash.atto.wallet.ui.dark_accent
import cash.atto.wallet.ui.dark_accent_on
import cash.atto.wallet.ui.dark_bg
import cash.atto.wallet.ui.dark_border
import cash.atto.wallet.ui.dark_border_muted
import cash.atto.wallet.ui.dark_danger
import cash.atto.wallet.ui.dark_success
import cash.atto.wallet.ui.dark_text_dim
import cash.atto.wallet.ui.dark_text_primary
import cash.atto.wallet.ui.dark_text_secondary
import cash.atto.wallet.ui.dark_warning
import cash.atto.wallet.uistate.settings.SettingsUiState
import cash.atto.wallet.worker.createLocalWorker
import cash.atto.wallet.worker.isLocalWorkerSupported
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

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

        uiState.preferencesMessage?.let { preferencesMessage ->
            AttoModal(
                title = "Preferences",
                onDismiss = onDismissPreferencesMessage,
            ) {
                Text(
                    text = preferencesMessage,
                    color = dark_text_secondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
                AttoButton(
                    text = "Close",
                    onClick = onDismissPreferencesMessage,
                    modifier = Modifier.fillMaxWidth(),
                    variant = AttoButtonVariant.Outlined,
                )
            }
        }
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
private fun LocalWorkerEvaluationDialog(
    localWorkerSupported: Boolean?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    var evaluationRun by remember { mutableStateOf(0) }
    var status by remember { mutableStateOf(WorkerEvaluationStatus.IDLE) }
    var targetWps by remember { mutableStateOf<Double?>(null) }
    var displayedWps by remember { mutableStateOf<Double?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var acknowledged by remember { mutableStateOf(false) }
    var elapsedMs by remember { mutableStateOf(0L) }

    LaunchedEffect(evaluationRun) {
        if (evaluationRun == 0) {
            return@LaunchedEffect
        }

        status = WorkerEvaluationStatus.EVALUATING
        targetWps = null
        displayedWps = null
        errorMessage = null
        acknowledged = false
        elapsedMs = 0L

        if (localWorkerSupported != true) {
            errorMessage = "Local web worker is unavailable in this browser."
            status = WorkerEvaluationStatus.COMPLETE
            return@LaunchedEffect
        }

        val worker = createLocalWorker()
        try {
            worker
                .evaluate(
                    network = AttoNetwork.LIVE,
                    maxDuration = LOCAL_WORKER_EVALUATION_DURATION,
                ).collect { evaluation ->
                    targetWps = evaluation.wps
                    errorMessage = null
                }

            if (targetWps == null) {
                errorMessage = "No work sample finished within ${LOCAL_WORKER_EVALUATION_DURATION.inWholeSeconds}s."
            }
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: Exception) {
            errorMessage = ex.message ?: "Unable to evaluate the local worker."
        } finally {
            worker.close()
        }

        status = WorkerEvaluationStatus.COMPLETE
    }

    LaunchedEffect(status, evaluationRun) {
        if (status != WorkerEvaluationStatus.EVALUATING) {
            return@LaunchedEffect
        }

        val started = TimeSource.Monotonic.markNow()
        while (true) {
            elapsedMs = started.elapsedNow().inWholeMilliseconds
            delay(10)
        }
    }

    LaunchedEffect(targetWps) {
        val target = targetWps ?: return@LaunchedEffect
        val start = displayedWps
        if (start == null) {
            displayedWps = target
            return@LaunchedEffect
        }

        val started = TimeSource.Monotonic.markNow()
        while (true) {
            val elapsed = started.elapsedNow().inWholeMilliseconds
            val progress = (elapsed.toDouble() / WPS_INTERPOLATION_MILLIS).coerceIn(0.0, 1.0)
            displayedWps = start + (target - start) * progress
            if (progress >= 1.0) {
                break
            }
            delay(16)
        }
    }

    val fit =
        if (status == WorkerEvaluationStatus.COMPLETE) {
            if (errorMessage != null) {
                WorkerHardwareFit.SLOW
            } else {
                targetWps?.hardwareFit()
            }
        } else {
            null
        }
    val failed = status == WorkerEvaluationStatus.COMPLETE && errorMessage != null
    val requiresAcknowledgement = !failed && (fit == WorkerHardwareFit.LIMITED || fit == WorkerHardwareFit.SLOW)
    val canConfirm = !failed && fit != null && (!requiresAcknowledgement || acknowledged)

    AttoModal(
        title = "Local Worker Preview",
        onDismiss = onDismiss,
    ) {
        Text(
            text =
                "Local worker computes proof-of-work on this device. This check estimates whether the hardware can keep up with Live network work before switching.",
            color = dark_text_secondary,
            style = MaterialTheme.typography.bodyMedium,
        )

        LocalWorkerStatusCard(
            wps = displayedWps,
            fit = fit,
            status = status,
            errorMessage = errorMessage,
        )

        WorkerEvaluationScale(activeFit = fit)

        if (requiresAcknowledgement) {
            LocalWorkerAcknowledgement(
                checked = acknowledged,
                onCheckedChange = { acknowledged = it },
            )
        }

        LocalWorkerDialogActions(
            status = status,
            elapsedMs = elapsedMs,
            fit = fit,
            canConfirm = canConfirm,
            localWorkerSupported = localWorkerSupported == true,
            onStart = { evaluationRun += 1 },
            onDismiss = onDismiss,
            onConfirm = onConfirm,
        )
    }
}

@Composable
private fun LocalWorkerStatusCard(
    wps: Double?,
    fit: WorkerHardwareFit?,
    status: WorkerEvaluationStatus,
    errorMessage: String?,
) {
    val color = fit?.color() ?: dark_accent
    val icon = fit?.icon()
    val title =
        when {
            errorMessage != null -> "Evaluation failed"
            fit != null -> fit.title()
            status == WorkerEvaluationStatus.EVALUATING -> "Evaluating hardware"
            status == WorkerEvaluationStatus.COMPLETE -> "Evaluation complete"
            else -> "Ready to evaluate"
        }
    val description =
        errorMessage
            ?: fit?.description()
            ?: when (status) {
                WorkerEvaluationStatus.IDLE -> "Start the check to estimate this device against Live network work."
                WorkerEvaluationStatus.EVALUATING -> "Measuring local proof-of-work speed. Results update as samples complete."
                WorkerEvaluationStatus.COMPLETE -> "Evaluation finished without enough data to classify this device."
            }

    AttoPanelCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(62.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(color.copy(alpha = 0.13f))
                        .border(1.dp, color.copy(alpha = 0.38f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (icon == null) {
                    if (status == WorkerEvaluationStatus.EVALUATING) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = dark_accent,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Memory,
                            contentDescription = title,
                            tint = dark_accent,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = color,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        color = dark_text_primary,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W700),
                    )
                    AttoTag(
                        text =
                            when (status) {
                                WorkerEvaluationStatus.IDLE -> "Idle"
                                WorkerEvaluationStatus.EVALUATING -> "Live"
                                WorkerEvaluationStatus.COMPLETE -> "Done"
                            },
                        color = if (status == WorkerEvaluationStatus.COMPLETE) color else dark_accent,
                    )
                }
                Text(
                    text = description,
                    color = dark_text_secondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            WorkerMetricTile(
                label = "Estimated rate",
                value = wps?.formatWps() ?: status.emptyMetricLabel(),
                valueColor = color,
                modifier = Modifier.weight(1f),
            )
            WorkerMetricTile(
                label = "Estimated time",
                value = wps?.formatSecondsPerWork() ?: status.emptyMetricLabel(),
                valueColor = color,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun LocalWorkerDialogActions(
    status: WorkerEvaluationStatus,
    elapsedMs: Long,
    fit: WorkerHardwareFit?,
    canConfirm: Boolean,
    localWorkerSupported: Boolean,
    onStart: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    when (status) {
        WorkerEvaluationStatus.IDLE -> {
            AttoButton(
                text = "Start Evaluation",
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
                enabled = localWorkerSupported,
            )
        }

        WorkerEvaluationStatus.EVALUATING -> {
            AttoButton(
                text = "${elapsedMs / 1000.0}s",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
            )
        }

        WorkerEvaluationStatus.COMPLETE -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AttoButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    variant = AttoButtonVariant.Outlined,
                )
                LocalWorkerResultButton(
                    text = "Use Local",
                    onClick = onConfirm,
                    enabled = canConfirm,
                    fit = fit ?: WorkerHardwareFit.SLOW,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun LocalWorkerResultButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    fit: WorkerHardwareFit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    val color = fit.color()
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val backgroundColor =
        when {
            !enabled -> color.copy(alpha = 0.34f)
            hovered -> color.copy(alpha = 0.88f)
            else -> color
        }

    Row(
        modifier =
            modifier
                .height(56.dp)
                .clip(shape)
                .background(backgroundColor)
                .then(
                    if (enabled) {
                        Modifier.pointerHoverIcon(PointerIcon.Hand)
                    } else {
                        Modifier
                    },
                ).clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                ).padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            color = dark_accent_on.copy(alpha = if (enabled) 1f else 0.7f),
            style =
                MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.W700,
                    fontSize = 15.sp,
                ),
        )
    }
}

@Composable
private fun WorkerMetricTile(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(8.dp))
                .background(dark_bg)
                .border(1.dp, dark_border_muted, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            color = dark_text_secondary,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.W500),
        )
        Text(
            text = value,
            color = valueColor,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.W700,
                ),
        )
    }
}

@Composable
private fun WorkerEvaluationScale(activeFit: WorkerHardwareFit?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AttoCapsLabel("Hardware fit")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            WorkerScaleSegment(
                label = "Green",
                detail = "<=5s / work",
                color = dark_success,
                active = activeFit == WorkerHardwareFit.GOOD,
                modifier = Modifier.weight(1f),
            )
            WorkerScaleSegment(
                label = "Yellow",
                detail = "<=10s / work",
                color = dark_warning,
                active = activeFit == WorkerHardwareFit.LIMITED,
                modifier = Modifier.weight(1f),
            )
            WorkerScaleSegment(
                label = "Red",
                detail = ">10s / work",
                color = dark_danger,
                active = activeFit == WorkerHardwareFit.SLOW,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun WorkerScaleSegment(
    label: String,
    detail: String,
    color: Color,
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .height(68.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = if (active) 0.16f else 0.07f))
                .border(
                    width = 1.dp,
                    color = color.copy(alpha = if (active) 0.62f else 0.22f),
                    shape = RoundedCornerShape(8.dp),
                ).padding(10.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.W800),
        )
        Text(
            text = detail,
            color = dark_text_secondary,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
        )
    }
}

@Composable
private fun LocalWorkerAcknowledgement(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(dark_warning.copy(alpha = 0.09f))
                .border(1.dp, dark_warning.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AttoCheckbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
        Text(
            text = "I understand that choosing local worker on this hardware may make the wallet experience worse.",
            color = dark_text_secondary,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
        )
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

private enum class WorkerHardwareFit {
    GOOD,
    LIMITED,
    SLOW,
}

private enum class WorkerEvaluationStatus {
    IDLE,
    EVALUATING,
    COMPLETE,
}

private fun WorkSourcePreference.label(): String =
    when (this) {
        WorkSourcePreference.REMOTE -> "Remote"
        WorkSourcePreference.LOCAL -> "Local"
    }

private fun Double.hardwareFit(): WorkerHardwareFit =
    when {
        this >= GREEN_WORKS_PER_SECOND -> WorkerHardwareFit.GOOD
        this >= YELLOW_WORKS_PER_SECOND -> WorkerHardwareFit.LIMITED
        else -> WorkerHardwareFit.SLOW
    }

private fun WorkerHardwareFit.color(): Color =
    when (this) {
        WorkerHardwareFit.GOOD -> dark_success
        WorkerHardwareFit.LIMITED -> dark_warning
        WorkerHardwareFit.SLOW -> dark_danger
    }

private fun WorkerHardwareFit.icon(): ImageVector =
    when (this) {
        WorkerHardwareFit.GOOD -> Icons.Outlined.CheckCircle
        WorkerHardwareFit.LIMITED -> Icons.Outlined.WarningAmber
        WorkerHardwareFit.SLOW -> Icons.Outlined.ErrorOutline
    }

private fun WorkerHardwareFit.title(): String =
    when (this) {
        WorkerHardwareFit.GOOD -> "Good fit"
        WorkerHardwareFit.LIMITED -> "Borderline fit"
        WorkerHardwareFit.SLOW -> "Poor fit"
    }

private fun WorkerHardwareFit.description(): String =
    when (this) {
        WorkerHardwareFit.GOOD -> "This device estimates faster than 1 work every 5s."
        WorkerHardwareFit.LIMITED -> "This device estimates between 1 work every 5s and 10s."
        WorkerHardwareFit.SLOW -> "This device estimates slower than 1 work every 10s."
    }

private fun WorkerEvaluationStatus.emptyMetricLabel(): String =
    when (this) {
        WorkerEvaluationStatus.IDLE -> "Not started"
        WorkerEvaluationStatus.EVALUATING -> "Measuring"
        WorkerEvaluationStatus.COMPLETE -> "Unavailable"
    }

private fun Double.formatWps(): String =
    when {
        this <= 0.0 -> "0 wps"
        this < 0.01 -> "<0.01 wps"
        this < 1.0 -> "${formatDecimal(2)} wps"
        else -> "${formatDecimal(1)} wps"
    }

private fun Double.formatSecondsPerWork(): String {
    if (this <= 0.0) {
        return "Measuring"
    }

    val seconds = 1.0 / this
    return when {
        seconds < 1.0 -> "<1s / work"
        seconds < 60.0 -> "${seconds.formatDecimal(1)}s / work"
        else -> "${(seconds / 60.0).formatDecimal(1)}m / work"
    }
}

private fun Double.formatDecimal(decimalPlaces: Int): String {
    val factor =
        when (decimalPlaces) {
            1 -> 10
            2 -> 100
            else -> 1
        }
    val scaled = (this * factor).roundToInt()
    val whole = scaled / factor
    val fraction =
        abs(scaled % factor)
            .toString()
            .padStart(decimalPlaces, '0')
            .trimEnd('0')

    return if (fraction.isEmpty()) {
        whole.toString()
    } else {
        "$whole.$fraction"
    }
}

private const val GREEN_WORKS_PER_SECOND = 0.2
private const val YELLOW_WORKS_PER_SECOND = 0.1
private const val WPS_INTERPOLATION_MILLIS = 1_000.0
private val LOCAL_WORKER_EVALUATION_DURATION = 2.minutes
