package cash.atto.wallet.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import attowallet.composeapp.generated.resources.*
import cash.atto.wallet.components.common.AttoBackButton
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.components.common.AttoModal
import cash.atto.wallet.components.common.AttoRoundButton
import cash.atto.wallet.components.common.AttoWordChip
import cash.atto.wallet.platform.setText
import cash.atto.wallet.ui.*
import cash.atto.wallet.uistate.secret.SecretPhraseUiState
import cash.atto.wallet.viewmodel.SecretPhraseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

private enum class RecoveryPhraseTab {
    View,
    Confirm,
}

@Composable
fun RecoveryPhraseScreen(
    onBackNavigation: () -> Unit,
    onBackupConfirmClicked: () -> Unit,
) {
    val viewModel = koinViewModel<SecretPhraseViewModel>()
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val uiState = viewModel.state.collectAsState()

    RecoveryPhrase(
        uiState = uiState.value,
        onBackNavigation = onBackNavigation,
        onBackupConfirmClicked = onBackupConfirmClicked,
        onCopyClick = {
            coroutineScope.launch {
                clipboard.setText(uiState.value.words.joinToString(" "))
            }
        },
    )
}

@Composable
fun RecoveryPhrase(
    uiState: SecretPhraseUiState,
    onBackNavigation: () -> Unit,
    onBackupConfirmClicked: () -> Unit,
    onCopyClick: () -> Unit,
) {
    var activeTab by remember { mutableStateOf(RecoveryPhraseTab.View) }
    var copied by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    val checkedItems = remember { mutableStateListOf<Int>() }
    val allChecked = checkedItems.size == 3
    val scrollState = rememberScrollState()
    val compact = isCompactWidth()

    LaunchedEffect(copied) {
        if (copied) {
            delay(2000)
            copied = false
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(dark_bg),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(
                        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 24.dp,
                        bottom = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding() + 40.dp,
                        start = 24.dp,
                        end = 24.dp,
                    ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter,
            ) {
                val shellWidth = if (maxWidth > 896.dp) 896.dp else maxWidth
                val tabsWidth = if (shellWidth > 448.dp) 448.dp else shellWidth

                Column(
                    modifier = Modifier.width(shellWidth),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    RecoveryPhraseHeader(
                        onBackNavigation = onBackNavigation,
                        onHelpClick = { showHelp = true },
                    )

                    Text(
                        text = stringResource(Res.string.secret_recovery_title),
                        color = dark_text_primary,
                        textAlign = TextAlign.Center,
                        style =
                            MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.W600,
                                fontSize = 36.sp,
                                lineHeight = 39.6.sp,
                                letterSpacing = (-0.72).sp,
                            ),
                    )

                    RecoveryPhraseTabs(
                        selected = activeTab,
                        onSelected = { activeTab = it },
                        modifier =
                            Modifier
                                .width(tabsWidth)
                                .padding(top = 32.dp),
                    )

                    if (activeTab == RecoveryPhraseTab.View) {
                        RecoveryPhraseViewContent(
                            uiState = uiState,
                            copied = copied,
                            onCopyClick = {
                                onCopyClick()
                                copied = true
                            },
                            onContinueClick = { activeTab = RecoveryPhraseTab.Confirm },
                        )
                    } else {
                        RecoveryPhraseConfirmContent(
                            checkedItems = checkedItems,
                            allChecked = allChecked,
                            onToggle = { step ->
                                if (checkedItems.contains(step)) {
                                    checkedItems.remove(step)
                                } else {
                                    checkedItems.add(step)
                                }
                            },
                            onContinueClick = onBackupConfirmClicked,
                        )
                    }
                }
            }
        }

        if (showHelp) {
            RecoveryPhraseHelpDialog(
                compact = compact,
                onDismiss = { showHelp = false },
            )
        }
    }
}

@Composable
private fun RecoveryPhraseHeader(
    onBackNavigation: () -> Unit,
    onHelpClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AttoBackButton(
            onClick = onBackNavigation,
        )
        AttoRoundButton(
            onClick = onHelpClick,
        ) {
            Text(
                text = "?",
                color = dark_text_primary,
                style =
                    MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.W600,
                        fontSize = 16.sp,
                    ),
            )
        }
    }
}

@Composable
private fun RecoveryPhraseTabs(
    selected: RecoveryPhraseTab,
    onSelected: (RecoveryPhraseTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .background(dark_surface, RoundedCornerShape(12.dp))
                .border(1.dp, dark_border, RoundedCornerShape(12.dp))
                .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        RecoveryTabButton(
            modifier = Modifier.weight(1f),
            selected = selected == RecoveryPhraseTab.View,
            text = stringResource(Res.string.secret_view_tab),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (selected == RecoveryPhraseTab.View) dark_bg else dark_text_muted,
                )
            },
            onClick = { onSelected(RecoveryPhraseTab.View) },
        )

        RecoveryTabButton(
            modifier = Modifier.weight(1f),
            selected = selected == RecoveryPhraseTab.Confirm,
            text = stringResource(Res.string.secret_confirm_tab),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.VerifiedUser,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (selected == RecoveryPhraseTab.Confirm) dark_bg else dark_text_muted,
                )
            },
            onClick = { onSelected(RecoveryPhraseTab.Confirm) },
        )
    }
}

@Composable
private fun RecoveryTabButton(
    selected: Boolean,
    text: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .height(44.dp)
                .background(
                    color = if (selected) dark_accent else Color.Transparent,
                    shape = RoundedCornerShape(8.dp),
                ).pointerHoverIcon(PointerIcon.Hand)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon()
        Text(
            text = text,
            modifier = Modifier.padding(start = 8.dp),
            color = if (selected) dark_bg else dark_text_muted,
            style =
                MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.W600,
                    fontSize = 13.sp,
                ),
        )
    }
}

@Composable
private fun RecoveryPhraseViewContent(
    uiState: SecretPhraseUiState,
    copied: Boolean,
    onCopyClick: () -> Unit,
    onContinueClick: () -> Unit,
) {
    RecoverySectionIntro(
        label = stringResource(Res.string.secret_view_title),
        description = stringResource(Res.string.secret_view_hint),
    )

    RecoveryPhraseGrid(
        words = uiState.words,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
    )

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RecoverySecondaryButton(
            modifier = Modifier.weight(1f),
            text =
                if (copied) {
                    stringResource(Res.string.secret_copied)
                } else {
                    stringResource(Res.string.secret_copy)
                },
            accent = if (copied) dark_success else dark_text_secondary,
            icon = {
                Icon(
                    imageVector = if (copied) Icons.Outlined.Check else Icons.Outlined.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (copied) dark_success else dark_text_secondary,
                )
            },
            onClick = onCopyClick,
        )

        RecoveryPrimarySoftButton(
            modifier = Modifier.weight(1f),
            text = stringResource(Res.string.secret_written_down),
            onClick = onContinueClick,
        )
    }
}

@Composable
private fun RecoveryPhraseConfirmContent(
    checkedItems: List<Int>,
    allChecked: Boolean,
    onToggle: (Int) -> Unit,
    onContinueClick: () -> Unit,
) {
    RecoverySectionIntro(
        label = stringResource(Res.string.secret_confirm_backup_title),
        description = stringResource(Res.string.secret_confirm_backup_hint),
    )

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .heightIn(min = 600.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RecoveryChecklistCard(
            checked = checkedItems.contains(1),
            title = stringResource(Res.string.secret_checklist_one_title),
            description = stringResource(Res.string.secret_checklist_one_description),
            onClick = { onToggle(1) },
        )
        RecoveryChecklistCard(
            checked = checkedItems.contains(2),
            title = stringResource(Res.string.secret_checklist_two_title),
            description = stringResource(Res.string.secret_checklist_two_description),
            onClick = { onToggle(2) },
        )
        RecoveryChecklistCard(
            checked = checkedItems.contains(3),
            title = stringResource(Res.string.secret_checklist_three_title),
            description = stringResource(Res.string.secret_checklist_three_description),
            onClick = { onToggle(3) },
        )
    }

    AttoButton(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
        text =
            if (allChecked) {
                stringResource(Res.string.secret_continue_create_password)
            } else {
                stringResource(Res.string.secret_check_all_items, checkedItems.size)
            },
        enabled = allChecked,
        onClick = onContinueClick,
    )
}

@Composable
private fun RecoverySectionIntro(
    label: String,
    description: String,
) {
    Column(
        modifier = Modifier.padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            color = dark_accent,
            textAlign = TextAlign.Center,
            style =
                MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.W600,
                    fontSize = 13.sp,
                    letterSpacing = 1.2.sp,
                ),
        )

        Text(
            text = description,
            modifier = Modifier.padding(top = 8.dp),
            color = dark_text_secondary,
            textAlign = TextAlign.Center,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.W400,
                    fontSize = 14.sp,
                    lineHeight = 22.4.sp,
                ),
        )
    }
}

@Composable
private fun RecoveryPhraseGrid(
    words: List<String>,
    modifier: Modifier = Modifier,
) {
    val midpoint = (words.size + 1) / 2
    val leftColumn = words.take(midpoint)
    val rightColumn = words.drop(midpoint)

    BoxWithConstraints(modifier = modifier) {
        val compact = maxWidth.isCompactWidth()

        if (compact) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RecoveryPhraseGridColumn(
                    words = leftColumn,
                    startingOrdinal = 1,
                    modifier = Modifier.fillMaxWidth(),
                )

                RecoveryPhraseGridColumn(
                    words = rightColumn,
                    startingOrdinal = midpoint + 1,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                RecoveryPhraseGridColumn(
                    words = leftColumn,
                    startingOrdinal = 1,
                    modifier = Modifier.weight(1f),
                )

                RecoveryPhraseGridColumn(
                    words = rightColumn,
                    startingOrdinal = midpoint + 1,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun RecoveryPhraseGridColumn(
    words: List<String>,
    startingOrdinal: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        words.forEachIndexed { index, word ->
            AttoWordChip(
                ordinal = startingOrdinal + index,
                word = word,
                hidden = false,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun RecoverySecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = dark_text_secondary,
    icon: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier =
            modifier
                .height(44.dp)
                .background(dark_surface, RoundedCornerShape(8.dp))
                .border(1.dp, dark_border, RoundedCornerShape(8.dp))
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.invoke()

        Text(
            text = text,
            modifier = Modifier.padding(start = if (icon != null) 8.dp else 0.dp),
            color = accent,
            style =
                MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.W500,
                    fontSize = 14.sp,
                ),
        )
    }
}

@Composable
private fun RecoveryPrimarySoftButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .height(44.dp)
                .background(dark_accent_soft, RoundedCornerShape(8.dp))
                .border(1.dp, dark_accent_border_hover, RoundedCornerShape(8.dp))
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = dark_accent,
            style =
                MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.W600,
                    fontSize = 14.sp,
                ),
        )
    }
}

@Composable
private fun RecoveryChecklistCard(
    checked: Boolean,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(dark_surface, RoundedCornerShape(8.dp))
                .border(1.dp, dark_border, RoundedCornerShape(8.dp))
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ).padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier =
                Modifier
                    .size(20.dp)
                    .background(
                        if (checked) dark_accent else Color.Transparent,
                        RoundedCornerShape(6.dp),
                    ).border(
                        2.dp,
                        if (checked) dark_accent else dark_border,
                        RoundedCornerShape(6.dp),
                    ),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = dark_bg,
                    modifier = Modifier.size(14.dp),
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                color = dark_text_primary,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.W500,
                        fontSize = 14.sp,
                    ),
            )
            Text(
                text = description,
                color = dark_text_muted,
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.W400,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                    ),
            )
        }
    }
}

@Composable
private fun RecoveryPhraseHelpDialog(
    compact: Boolean,
    onDismiss: () -> Unit,
) {
    AttoModal(
        title = stringResource(Res.string.secret_help_title),
        onDismiss = onDismiss,
    ) {
        Text(
            text = stringResource(Res.string.secret_help_body_one),
            color = dark_text_secondary,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 22.4.sp,
                ),
        )

        Text(
            text = stringResource(Res.string.secret_help_body_two),
            color = dark_text_secondary,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 22.4.sp,
                ),
        )

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(dark_accent_soft, RoundedCornerShape(12.dp))
                    .border(1.dp, dark_accent_border_hover, RoundedCornerShape(12.dp))
                    .padding(16.dp),
        ) {
            Text(
                text = stringResource(Res.string.secret_help_warning),
                color = dark_accent,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.W500,
                        fontSize = 14.sp,
                        lineHeight = 22.4.sp,
                    ),
            )
        }
    }
}

@Preview
@Composable
fun SecretPhraseCompactPreview() {
    AttoWalletTheme {
        RecoveryPhrase(
            uiState =
                SecretPhraseUiState(
                    words = (1..24).map { "word$it" },
                    hidden = false,
                ),
            onBackNavigation = {},
            onBackupConfirmClicked = {},
            onCopyClick = {},
        )
    }
}

@Preview
@Composable
fun SecretPhraseExpandedPreview() {
    AttoWalletTheme {
        RecoveryPhrase(
            uiState =
                SecretPhraseUiState(
                    words = (1..24).map { "word$it" },
                    hidden = false,
                ),
            onBackNavigation = {},
            onBackupConfirmClicked = {},
            onCopyClick = {},
        )
    }
}
