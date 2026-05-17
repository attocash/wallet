package cash.atto.wallet.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
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
import cash.atto.wallet.components.common.AttoCheckbox
import cash.atto.wallet.components.common.AttoModal
import cash.atto.wallet.components.common.AttoPanelCard
import cash.atto.wallet.ui.dark_accent
import cash.atto.wallet.ui.dark_accent_on
import cash.atto.wallet.ui.dark_bg
import cash.atto.wallet.ui.dark_border_muted
import cash.atto.wallet.ui.dark_danger
import cash.atto.wallet.ui.dark_success
import cash.atto.wallet.ui.dark_text_primary
import cash.atto.wallet.ui.dark_text_secondary
import cash.atto.wallet.ui.dark_warning
import cash.atto.wallet.worker.createLocalWorker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

@Composable
internal fun LocalWorkerEvaluationDialog(
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
private val LOCAL_WORKER_EVALUATION_DURATION = 30.seconds
