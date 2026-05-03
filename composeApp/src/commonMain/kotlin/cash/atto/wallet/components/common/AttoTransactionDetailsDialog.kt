package cash.atto.wallet.components.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cash.atto.wallet.model.getAddressLabel
import cash.atto.wallet.model.getVoterLabel
import cash.atto.wallet.repository.HomeRepository
import cash.atto.wallet.repository.PreferencesRepository
import cash.atto.wallet.ui.attoFontFamily
import cash.atto.wallet.ui.attoHoverTint
import cash.atto.wallet.ui.dark_accent
import cash.atto.wallet.ui.dark_text_primary
import cash.atto.wallet.ui.dark_text_secondary
import cash.atto.wallet.ui.dark_text_tertiary
import cash.atto.wallet.uistate.overview.TransactionType
import cash.atto.wallet.uistate.overview.TransactionUiState
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun AttoTransactionDetailsDialog(
    transaction: TransactionUiState,
    onDismiss: () -> Unit,
) {
    val preferencesRepository = koinInject<PreferencesRepository>()
    val homeRepository = koinInject<HomeRepository>()
    val preferences by preferencesRepository.state.collectAsState()
    val uriHandler = LocalUriHandler.current
    val hash = transaction.hash
    val explorerUrl = hash?.let { "https://atto.cash/explorer/transactions/$it" }
    val coroutineScope = rememberCoroutineScope()
    val savedAddressLabel = preferences.addressLabel(transaction.source).orEmpty()
    val savedTransactionLabel = hash?.let(preferences::hashLabel).orEmpty()
    var addressLabel by remember(transaction.source) { mutableStateOf(savedAddressLabel) }
    var transactionLabel by remember(hash) { mutableStateOf(savedTransactionLabel) }
    var editingTransactionLabel by remember(hash) { mutableStateOf(false) }
    var editingAddressLabel by remember(transaction.source) { mutableStateOf(false) }
    val resolvedAddressLabel =
        when (transaction.type) {
            TransactionType.CHANGE -> {
                preferencesRepository.getAddressLabel(transaction.source)
                    ?: homeRepository.homeResponse.value?.getVoterLabel(transaction.source)
            }

            else -> {
                preferencesRepository.getAddressLabel(transaction.source)
                    ?: homeRepository.homeResponse.value?.getAddressLabel(transaction.source)
            }
        }

    LaunchedEffect(savedAddressLabel, transaction.source) {
        addressLabel = savedAddressLabel
    }

    LaunchedEffect(savedTransactionLabel, hash) {
        transactionLabel = savedTransactionLabel
    }

    AttoModal(
        title = "Transaction Details",
        onDismiss = onDismiss,
    ) {
        AttoEditableCopyField(
            label = "Transaction Hash",
            fieldLabel = "Transaction Hash Label",
            value = transactionLabel,
            displayValueWhenEmpty = hash ?: "Unavailable",
            displayValueIsRawValue = true,
            sourceValue = hash ?: "Unavailable",
            isEditing = editingTransactionLabel,
            editable = hash != null,
            helperText =
                if (hash == null) {
                    "This transaction does not have a hash yet."
                } else {
                    null
                },
            onStartEditing = { editingTransactionLabel = true },
            onValueChange = { transactionLabel = it },
            onConfirm = {
                val transactionHash = hash ?: return@AttoEditableCopyField
                coroutineScope.launch {
                    preferencesRepository.saveHashLabel(
                        hash = transactionHash,
                        label = transactionLabel,
                    )
                    editingTransactionLabel = false
                }
            },
            onClear = {
                val transactionHash = hash ?: return@AttoEditableCopyField
                coroutineScope.launch {
                    transactionLabel = ""
                    preferencesRepository.saveHashLabel(
                        hash = transactionHash,
                        label = "",
                    )
                    editingTransactionLabel = false
                }
            },
        )
        AttoDetailField(
            label = "Type",
            value =
                transaction.type.name
                    .lowercase()
                    .replaceFirstChar { it.uppercase() },
        )
        AttoDetailField(
            label = "Amount",
            value = transaction.shownAmount.ifBlank { "Unavailable" },
        )
        AttoEditableCopyField(
            label =
                when (transaction.type) {
                    TransactionType.OPEN -> "Voter"
                    TransactionType.SEND -> "Receiver"
                    TransactionType.RECEIVE -> "Sender"
                    TransactionType.CHANGE -> "Voter"
                },
            fieldLabel = "Address Label",
            value = addressLabel,
            displayValueWhenEmpty = resolvedAddressLabel ?: transaction.source,
            displayValueIsRawValue = resolvedAddressLabel == null,
            sourceValue = transaction.source,
            isEditing = editingAddressLabel,
            onStartEditing = { editingAddressLabel = true },
            onValueChange = { addressLabel = it },
            onConfirm = {
                coroutineScope.launch {
                    preferencesRepository.saveAddressLabel(
                        address = transaction.source,
                        label = addressLabel,
                    )
                    editingAddressLabel = false
                }
            },
            onClear = {
                coroutineScope.launch {
                    addressLabel = ""
                    preferencesRepository.saveAddressLabel(
                        address = transaction.source,
                        label = "",
                    )
                    editingAddressLabel = false
                }
            },
        )
        AttoDetailField(
            label = "Height",
            value = "#${transaction.shownHeight}",
        )
        AttoDetailField(
            label = "Timestamp",
            value = transaction.formattedTimestamp,
        )

        if (explorerUrl != null) {
            AttoButton(
                text = "View in Explorer",
                onClick = { uriHandler.openUri(explorerUrl) },
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.AutoMirrored.Outlined.ArrowForward,
            )
        }
    }
}

@Composable
private fun AttoEditableCopyField(
    label: String,
    fieldLabel: String,
    value: String,
    displayValueWhenEmpty: String,
    displayValueIsRawValue: Boolean,
    sourceValue: String,
    modifier: Modifier = Modifier,
    editable: Boolean = true,
    isEditing: Boolean,
    helperText: String? = null,
    onStartEditing: () -> Unit,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onClear: () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AttoCapsLabel(label)
        if (isEditing && editable) {
            AttoFieldSurface {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    InlineEditorField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            AttoFieldValueText(
                                text = displayValueWhenEmpty,
                                modifier = Modifier.fillMaxWidth(),
                                color = dark_text_secondary,
                                style = fieldTextStyle(displayValueIsRawValue),
                            )
                        },
                        onDone = onConfirm,
                    )
                    AttoInlineIconButton(
                        imageVector = Icons.Outlined.Check,
                        onClick = onConfirm,
                        contentDescription = "Save $fieldLabel",
                        tint = dark_text_primary,
                    )
                    AttoInlineIconButton(
                        imageVector = Icons.AutoMirrored.Outlined.Backspace,
                        onClick = onClear,
                        contentDescription = "Clear $fieldLabel",
                    )
                }
            }
        } else {
            AttoFieldSurface {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (value.isBlank()) {
                        AttoFieldValueText(
                            text = displayValueWhenEmpty,
                            modifier = Modifier.weight(1f),
                            style = fieldTextStyle(displayValueIsRawValue),
                        )
                    } else {
                        AttoFieldValueText(
                            text = value,
                            modifier = Modifier.weight(1f),
                            style = fieldTextStyle(isRawValue = false),
                        )
                    }
                    if (editable) {
                        AttoInlineIconButton(
                            imageVector = Icons.Outlined.Edit,
                            onClick = onStartEditing,
                            contentDescription = "Edit $fieldLabel",
                        )
                    }
                    AttoCopyButton(
                        text = sourceValue,
                        tint = dark_text_tertiary,
                        contentDescription = "Copy $label",
                    )
                }
            }
        }
        if (helperText != null) {
            Text(
                text = helperText,
                color = dark_text_secondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun InlineEditorField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable () -> Unit,
    onDone: () -> Unit,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        textStyle = fieldTextStyle(isRawValue = false).copy(color = dark_text_primary),
        cursorBrush = SolidColor(dark_accent),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions =
            KeyboardActions(
                onDone = { onDone() },
            ),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isBlank()) {
                    placeholder()
                }
                innerTextField()
            }
        },
    )
}

@Composable
private fun AttoInlineIconButton(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = dark_text_tertiary,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier =
            modifier
                .size(size)
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = attoHoverTint(tint, hovered),
            modifier = Modifier.size(size * 2 / 3),
        )
    }
}

@Composable
private fun fieldTextStyle(isRawValue: Boolean): TextStyle =
    MaterialTheme.typography.bodySmall.copy(
        fontFamily = if (isRawValue) FontFamily.Monospace else attoFontFamily(),
        fontWeight = FontWeight.W600,
        fontSize = 13.sp,
    )
