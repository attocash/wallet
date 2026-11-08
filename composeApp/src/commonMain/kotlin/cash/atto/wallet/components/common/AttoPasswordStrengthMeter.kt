package cash.atto.wallet.components.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.password_length_guidance
import attowallet.composeapp.generated.resources.password_length_progress
import attowallet.composeapp.generated.resources.password_length_title
import attowallet.composeapp.generated.resources.password_length_too_short
import attowallet.composeapp.generated.resources.password_strength_fair
import attowallet.composeapp.generated.resources.password_strength_good
import attowallet.composeapp.generated.resources.password_strength_guidance
import attowallet.composeapp.generated.resources.password_strength_strong
import attowallet.composeapp.generated.resources.password_strength_title
import attowallet.composeapp.generated.resources.password_strength_too_weak
import attowallet.composeapp.generated.resources.password_strength_very_weak
import attowallet.composeapp.generated.resources.password_strength_weak
import cash.atto.wallet.model.PasswordStrength
import cash.atto.wallet.ui.dark_accent
import cash.atto.wallet.ui.dark_border
import cash.atto.wallet.ui.dark_danger
import cash.atto.wallet.ui.dark_success
import cash.atto.wallet.ui.dark_text_muted
import cash.atto.wallet.ui.dark_text_secondary
import cash.atto.wallet.ui.dark_warning
import org.jetbrains.compose.resources.stringResource

@Composable
fun AttoPasswordStrengthMeter(assessment: PasswordStrength) {
    val isEmpty = assessment.characterCount == 0
    val showStrength = assessment.minimumLengthMet
    val title = stringResource(if (showStrength) Res.string.password_strength_title else Res.string.password_length_title)
    val label =
        if (showStrength) {
            stringResource(passwordStrengthLabel(assessment.score))
        } else {
            stringResource(Res.string.password_length_progress, assessment.characterCount, PasswordStrength.MINIMUM_LENGTH)
        }
    val meterColor =
        when {
            !showStrength -> dark_text_muted
            assessment.score <= 1 -> dark_danger
            assessment.score == 2 -> dark_warning
            assessment.score == 3 -> dark_accent
            else -> dark_success
        }
    val targetProgress =
        if (showStrength) {
            (assessment.score + 1) / 5f
        } else {
            assessment.characterCount.toFloat() / PasswordStrength.MINIMUM_LENGTH
        }
    val progress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 200),
        label = "Password progress",
    )
    val showLengthWarning = !isEmpty && !assessment.minimumLengthMet
    val showStrengthWarning = !isEmpty && assessment.minimumLengthMet && assessment.score < PasswordStrength.MINIMUM_SCORE
    val guidanceColor =
        when {
            showLengthWarning -> dark_warning
            showStrengthWarning -> dark_danger
            else -> dark_text_secondary
        }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(
            modifier =
                Modifier.semantics(mergeDescendants = true) {
                    // Web Compose currently exposes merged text, but not progress/state properties.
                    stateDescription = label
                    progressBarRangeInfo =
                        ProgressBarRangeInfo(targetProgress, 0f..1f, if (showStrength) 4 else PasswordStrength.MINIMUM_LENGTH - 1)
                },
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    color = dark_text_secondary,
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    text = label,
                    color = meterColor,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(dark_border, RoundedCornerShape(4.dp)),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth(progress)
                            .height(4.dp)
                            .background(meterColor, RoundedCornerShape(4.dp)),
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showLengthWarning || showStrengthWarning) {
                Icon(
                    imageVector = Icons.Outlined.WarningAmber,
                    contentDescription = null,
                    tint = guidanceColor,
                    modifier = Modifier.size(14.dp),
                )
            }
            Text(
                text =
                    if (!assessment.minimumLengthMet) {
                        stringResource(
                            if (showLengthWarning) Res.string.password_length_too_short else Res.string.password_length_guidance,
                            PasswordStrength.MINIMUM_LENGTH,
                        )
                    } else {
                        stringResource(
                            if (showStrengthWarning) Res.string.password_strength_too_weak else Res.string.password_strength_guidance,
                            stringResource(passwordStrengthLabel(PasswordStrength.MINIMUM_SCORE)),
                            PasswordStrength.MINIMUM_SCORE,
                        )
                    },
                color = guidanceColor,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private fun passwordStrengthLabel(score: Int) =
    when (score) {
        0 -> Res.string.password_strength_very_weak
        1 -> Res.string.password_strength_weak
        2 -> Res.string.password_strength_fair
        3 -> Res.string.password_strength_good
        else -> Res.string.password_strength_strong
    }
