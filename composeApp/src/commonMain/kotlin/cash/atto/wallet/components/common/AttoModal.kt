package cash.atto.wallet.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cash.atto.wallet.ui.attoHoverTint
import cash.atto.wallet.ui.dark_border
import cash.atto.wallet.ui.dark_surface
import cash.atto.wallet.ui.dark_text_secondary

private val attoModalWidth = 520.dp
private val attoModalContentPadding = PaddingValues(20.dp)
private val attoModalContentSpacing = 24.dp

@Composable
fun AttoModal(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
            ),
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onDismiss,
                        ),
            )

            Column(
                modifier =
                    modifier
                        .then(
                            Modifier.width(attoModalWidth),
                        ).background(
                            color = dark_surface,
                            shape = RoundedCornerShape(16.dp),
                        ).clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},
                        ),
            ) {
                val closeInteractionSource = remember { MutableInteractionSource() }
                val closeHovered by closeInteractionSource.collectIsHoveredAsState()
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        style =
                            MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.W600,
                                fontSize = 20.sp,
                            ),
                    )
                    Box(
                        modifier =
                            Modifier
                                .size(28.dp)
                                .clickable(
                                    interactionSource = closeInteractionSource,
                                    indication = null,
                                    onClick = onDismiss,
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close",
                            tint = attoHoverTint(dark_text_secondary, closeHovered),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }

                HorizontalDivider(color = dark_border)

                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .then(
                                if (scrollable) {
                                    Modifier.verticalScroll(rememberScrollState())
                                } else {
                                    Modifier
                                },
                            ).padding(attoModalContentPadding),
                    verticalArrangement = Arrangement.spacedBy(attoModalContentSpacing),
                ) {
                    content()
                }
            }
        }
    }
}
