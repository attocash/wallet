package cash.atto.wallet.components.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cash.atto.wallet.components.common.AttoCheckbox
import cash.atto.wallet.model.TermsAndConditions
import cash.atto.wallet.ui.dark_accent
import cash.atto.wallet.ui.dark_text_secondary

@Composable
fun AttoTermsAcceptance(
    accepted: Boolean,
    onAcceptedChange: (Boolean) -> Unit,
    effectiveDate: String = TermsAndConditions.EFFECTIVE_DATE,
    modifier: Modifier = Modifier,
) {
    var showTerms by remember { mutableStateOf(false) }
    val nextAccepted = !accepted

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        AttoCheckbox(
            checked = accepted,
            onCheckedChange = onAcceptedChange,
            modifier = Modifier.padding(top = 1.dp),
        )

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "I accept the ",
                    color = dark_text_secondary,
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onAcceptedChange(nextAccepted) },
                            ),
                    style =
                        MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                        ),
                )

                Text(
                    text = "Terms and Conditions",
                    color = dark_accent,
                    modifier =
                        Modifier
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { showTerms = true },
                            ),
                    style =
                        MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.W600,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                        ),
                )
            }
        }
    }
    if (showTerms) {
        TermsAndConditionsDialog(
            effectiveDate = effectiveDate,
            accepted = accepted,
            onAccept = {
                onAcceptedChange(true)
                showTerms = false
            },
            onDismiss = { showTerms = false },
        )
    }
}
