package cash.atto.wallet.components.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.components.common.AttoButtonVariant
import cash.atto.wallet.components.common.AttoModal
import cash.atto.wallet.model.TermsAndConditions
import cash.atto.wallet.model.TermsAndConditionsSection
import cash.atto.wallet.ui.dark_text_muted
import cash.atto.wallet.ui.dark_text_primary
import cash.atto.wallet.ui.dark_text_secondary

@Composable
fun TermsAndConditionsDialog(
    effectiveDate: String,
    accepted: Boolean,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
) {
    AttoModal(
        title = "Terms and Conditions",
        onDismiss = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Last updated $effectiveDate",
                color = dark_text_muted,
                style =
                    MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.W600,
                        fontSize = 12.sp,
                    ),
            )

            Text(
                text = "Please read these terms carefully before using Atto Wallet.",
                color = dark_text_primary,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                    ),
            )

            TermsAndConditions.sections.forEach { section ->
                TermsSection(section)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AttoButton(
                text = "Close",
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                variant = AttoButtonVariant.Outlined,
            )

            AttoButton(
                text = if (accepted) "Accepted" else "Accept",
                onClick = onAccept,
                modifier = Modifier.weight(1f),
                enabled = !accepted,
            )
        }
    }
}

@Composable
private fun TermsSection(section: TermsAndConditionsSection) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = section.title,
            color = dark_text_primary,
            style =
                MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.W600,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                ),
        )

        Text(
            text = section.body,
            color = dark_text_secondary,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                ),
        )
    }
}
