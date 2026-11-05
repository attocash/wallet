package cash.atto.wallet.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.password_strength_empty
import attowallet.composeapp.generated.resources.password_strength_fair
import attowallet.composeapp.generated.resources.password_strength_good
import attowallet.composeapp.generated.resources.password_strength_guidance
import attowallet.composeapp.generated.resources.password_strength_strong
import attowallet.composeapp.generated.resources.password_strength_title
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
fun AttoPasswordStrengthMeter(
    assessment: PasswordStrength,
    isEmpty: Boolean,
) {
    val title = stringResource(Res.string.password_strength_title)
    val label =
        stringResource(if (isEmpty) Res.string.password_strength_empty else passwordStrengthLabel(assessment.score))
    val strengthColor =
        when {
            isEmpty -> dark_text_muted
            assessment.score <= 1 -> dark_danger
            assessment.score == 2 -> dark_warning
            assessment.score == 3 -> dark_accent
            else -> dark_success
        }
    val progress = if (isEmpty) 0f else (assessment.score + 1) / 5f

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(
            modifier =
                Modifier.semantics(mergeDescendants = true) {
                    // Web Compose currently exposes merged text, but not progress/state properties.
                    stateDescription = label
                    progressBarRangeInfo = ProgressBarRangeInfo(progress, 0f..1f, 4)
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
                    color = strengthColor,
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
                if (!isEmpty) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth(progress)
                                .height(4.dp)
                                .background(strengthColor, RoundedCornerShape(4.dp)),
                    )
                }
            }
        }
        Text(
            text =
                stringResource(
                    Res.string.password_strength_guidance,
                    PasswordStrength.MINIMUM_LENGTH,
                    stringResource(passwordStrengthLabel(PasswordStrength.MINIMUM_SCORE)),
                    PasswordStrength.MINIMUM_SCORE,
                ),
            color = dark_text_secondary,
            style = MaterialTheme.typography.bodySmall,
        )
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
