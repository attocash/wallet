package cash.atto.wallet.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.secret_import_hint
import attowallet.composeapp.generated.resources.secret_import_paste
import attowallet.composeapp.generated.resources.secret_import_paste_hint
import attowallet.composeapp.generated.resources.secret_import_paste_success
import attowallet.composeapp.generated.resources.secret_import_paste_title
import attowallet.composeapp.generated.resources.secret_import_progress
import attowallet.composeapp.generated.resources.secret_import_submit
import attowallet.composeapp.generated.resources.secret_import_title
import attowallet.composeapp.generated.resources.secret_import_word_placeholder
import cash.atto.commons.AttoMnemonic
import cash.atto.commons.AttoMnemonicDictionary
import cash.atto.commons.AttoMnemonicException
import cash.atto.wallet.clipboard.readClipboardText
import cash.atto.wallet.components.common.AttoBackButton
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.dark_accent
import cash.atto.wallet.ui.dark_accent_soft
import cash.atto.wallet.ui.dark_bg
import cash.atto.wallet.ui.dark_border
import cash.atto.wallet.ui.dark_border_muted
import cash.atto.wallet.ui.dark_danger
import cash.atto.wallet.ui.dark_success
import cash.atto.wallet.ui.dark_surface
import cash.atto.wallet.ui.dark_surface_deep
import cash.atto.wallet.ui.dark_text_muted
import cash.atto.wallet.ui.dark_text_secondary
import cash.atto.wallet.uistate.secret.ImportSecretUiState
import cash.atto.wallet.viewmodel.ImportSecretViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

private val ImportPageBackground = dark_bg
private val ImportSurface = dark_surface
private val ImportSurfaceRaised = dark_surface_deep
private val ImportBorder = dark_border
private val ImportBorderMuted = dark_border_muted
private val ImportTextPrimary = Color.White
private val ImportTextSecondary = dark_text_secondary
private val ImportTextTertiary = dark_text_muted
private val ImportGold = dark_accent
private val ImportGoldDark = dark_bg
private val ImportGoldSoft = dark_accent_soft
private val ImportDanger = dark_danger
private val ImportSuccess = dark_success

private const val IMPORT_WORD_COUNT = 24
private val IMPORT_DICTIONARY_WORDS = AttoMnemonicDictionary.list
private val IMPORT_DICTIONARY_SET = AttoMnemonicDictionary.set
private const val IMPORT_MNEMONIC_INVALID_MESSAGE = "Mnemonic is invalid"

private fun splitMnemonicWords(value: String?): List<String> =
    value
        .orEmpty()
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(IMPORT_WORD_COUNT)

private fun toWordSlots(words: List<String>): List<String> = List(IMPORT_WORD_COUNT) { index -> words.getOrNull(index).orEmpty() }

private fun normalizeWordInput(value: String): String = value.lowercase().filter { it.isLetter() }

private fun joinMnemonicWords(words: List<String>): String =
    words
        .map { it.trim().lowercase() }
        .filter { it.isNotBlank() }
        .joinToString(" ")

@Composable
fun ImportPhraseScreen(
    onBackNavigation: () -> Unit,
    onImportAccount: () -> Unit,
) {
    val viewModel = koinViewModel<ImportSecretViewModel>()
    val uiState = viewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    ImportPhrase(
        uiState = uiState.value,
        onBackNavigation = onBackNavigation,
        onInputChanged = {
            coroutineScope.launch {
                viewModel.updateInput(it)
            }
        },
        onDoneClicked = {
            coroutineScope.launch {
                if (viewModel.importWallet()) {
                    onImportAccount()
                }
            }
        },
    )
}

@Composable
fun ImportPhrase(
    uiState: ImportSecretUiState,
    onBackNavigation: () -> Unit,
    onInputChanged: (String) -> Unit,
    onDoneClicked: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val words =
        remember {
            mutableStateListOf<String>().apply {
                repeat(IMPORT_WORD_COUNT) { add("") }
            }
        }
    var pasted by remember { mutableStateOf(false) }
    var focusedIndex by remember { mutableStateOf<Int?>(null) }
    var didLoadInitialInput by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.input) {
        if (!didLoadInitialInput) {
            val inputWords = toWordSlots(splitMnemonicWords(uiState.input))
            repeat(IMPORT_WORD_COUNT) { index ->
                words[index] = inputWords[index]
            }
            didLoadInitialInput = true
        }
    }

    LaunchedEffect(pasted) {
        if (pasted) {
            delay(1800)
            pasted = false
        }
    }

    val normalizedWords = words.map(::normalizeWordInput)
    val filledCount = normalizedWords.count { it.isNotBlank() }
    val hasCompletePhrase = filledCount == IMPORT_WORD_COUNT
    val invalidWordIndices =
        if (hasCompletePhrase) {
            normalizedWords
                .mapIndexedNotNull { index, word ->
                    if (word !in IMPORT_DICTIONARY_SET) index else null
                }.toSet()
        } else {
            emptySet()
        }
    val mnemonicError =
        remember(normalizedWords) {
            if (!hasCompletePhrase || invalidWordIndices.isNotEmpty()) {
                null
            } else {
                try {
                    AttoMnemonic(normalizedWords)
                    null
                } catch (exception: AttoMnemonicException) {
                    exception.message ?: "Invalid mnemonic"
                }
            }
        }
    val showInvalidState = mnemonicError != null
    val progress = filledCount / IMPORT_WORD_COUNT.toFloat()
    val canSubmit = hasCompletePhrase && invalidWordIndices.isEmpty() && mnemonicError == null

    fun syncWords(nextWords: List<String>) = onInputChanged(joinMnemonicWords(nextWords))

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(ImportPageBackground),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(
                        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 32.dp,
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
                val shellWidth = if (maxWidth > 736.dp) 736.dp else maxWidth

                Column(
                    modifier = Modifier.width(shellWidth),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 32.dp),
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        AttoBackButton(
                            onClick = onBackNavigation,
                        )
                    }

                    Text(
                        text = stringResource(Res.string.secret_import_title),
                        color = ImportTextPrimary,
                        textAlign = TextAlign.Center,
                        style =
                            MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.W600,
                                fontSize = 36.sp,
                                lineHeight = 39.6.sp,
                                letterSpacing = (-0.72).sp,
                            ),
                    )

                    Text(
                        text = stringResource(Res.string.secret_import_hint),
                        modifier =
                            Modifier
                                .padding(top = 12.dp)
                                .padding(horizontal = 18.dp),
                        color = ImportTextSecondary,
                        textAlign = TextAlign.Center,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.W400,
                                fontSize = 14.sp,
                                lineHeight = 21.sp,
                            ),
                    )

                    ImportPasteCard(
                        pasted = pasted,
                        onPasteClick = {
                            coroutineScope.launch {
                                val clipboardWords =
                                    readClipboardText()
                                        .orEmpty()
                                        .trim()
                                        .lowercase()
                                        .split(Regex("[^a-z]+"))
                                        .filter { it.isNotBlank() }
                                        .take(IMPORT_WORD_COUNT)

                                if (clipboardWords.isEmpty()) {
                                    return@launch
                                }

                                repeat(IMPORT_WORD_COUNT) { index ->
                                    words[index] = clipboardWords.getOrNull(index).orEmpty()
                                }

                                syncWords(words.toList())
                                pasted = true
                            }
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
                    )

                    ImportProgressCard(
                        filledCount = filledCount,
                        progress = progress,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                    )

                    if (showInvalidState) {
                        ImportInvalidCard(
                            message = IMPORT_MNEMONIC_INVALID_MESSAGE,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                        )
                    }

                    ImportWordsGrid(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp),
                        words = words,
                        invalidWordIndices = emptySet(),
                        focusedIndex = focusedIndex,
                        onFocusedIndexChange = { focusedIndex = it },
                        onWordChange = { index, next ->
                            val pastedWords =
                                if (next.any { !it.isLetter() }) {
                                    next
                                        .trim()
                                        .lowercase()
                                        .split(Regex("[^a-z]+"))
                                        .filter { it.isNotBlank() }
                                } else {
                                    emptyList()
                                }

                            if (pastedWords.size > 1) {
                                pastedWords.forEachIndexed { offset, pastedWord ->
                                    val targetIndex = index + offset
                                    if (targetIndex < IMPORT_WORD_COUNT) {
                                        words[targetIndex] = pastedWord
                                    }
                                }
                            } else {
                                words[index] = normalizeWordInput(next)
                            }

                            syncWords(words.toList())
                        },
                    )

                    AttoButton(
                        text = stringResource(Res.string.secret_import_submit),
                        enabled = canSubmit,
                        onClick = onDoneClicked,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 28.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportWordsGrid(
    words: List<String>,
    invalidWordIndices: Set<Int>,
    focusedIndex: Int?,
    onFocusedIndexChange: (Int?) -> Unit,
    onWordChange: (Int, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val leftColumn = words.take(IMPORT_WORD_COUNT / 2)
    val rightColumn = words.drop(IMPORT_WORD_COUNT / 2)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            leftColumn.forEachIndexed { index, word ->
                ImportWordField(
                    index = index,
                    value = word,
                    isInvalid = index in invalidWordIndices,
                    focused = focusedIndex == index,
                    onFocusChange = { isFocused -> onFocusedIndexChange(if (isFocused) index else focusedIndex?.takeIf { it != index }) },
                    onValueChange = { onWordChange(index, it) },
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            rightColumn.forEachIndexed { index, word ->
                val absoluteIndex = index + (IMPORT_WORD_COUNT / 2)
                ImportWordField(
                    index = absoluteIndex,
                    value = word,
                    isInvalid = absoluteIndex in invalidWordIndices,
                    focused = focusedIndex == absoluteIndex,
                    onFocusChange = { isFocused ->
                        onFocusedIndexChange(
                            if (isFocused) {
                                absoluteIndex
                            } else {
                                focusedIndex?.takeIf {
                                    it !=
                                        absoluteIndex
                                }
                            },
                        )
                    },
                    onValueChange = { onWordChange(absoluteIndex, it) },
                )
            }
        }
    }
}

@Composable
private fun ImportPasteCard(
    pasted: Boolean,
    onPasteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .background(ImportSurface, RoundedCornerShape(16.dp))
                .border(1.dp, ImportBorder, RoundedCornerShape(16.dp))
                .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(44.dp)
                    .background(ImportGoldSoft, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (pasted) Icons.Outlined.Check else Icons.Outlined.ContentPaste,
                contentDescription = null,
                tint = if (pasted) ImportSuccess else ImportGold,
                modifier = Modifier.size(20.dp),
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text =
                    if (pasted) {
                        stringResource(Res.string.secret_import_paste_success)
                    } else {
                        stringResource(Res.string.secret_import_paste_title)
                    },
                color = ImportTextPrimary,
                style =
                    MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.W600,
                        fontSize = 14.sp,
                    ),
            )

            Text(
                text = stringResource(Res.string.secret_import_paste_hint),
                color = if (pasted) ImportSuccess else ImportTextSecondary,
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.W400,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                    ),
            )
        }

        Box(
            modifier =
                Modifier
                    .background(ImportGold, RoundedCornerShape(10.dp))
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onPasteClick,
                    ).padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(Res.string.secret_import_paste),
                color = ImportGoldDark,
                style =
                    MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.W600,
                        fontSize = 13.sp,
                    ),
            )
        }
    }
}

@Composable
private fun ImportProgressCard(
    filledCount: Int,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .background(ImportSurface, RoundedCornerShape(16.dp))
                .border(1.dp, ImportBorder, RoundedCornerShape(16.dp))
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.secret_import_progress),
                color = ImportTextSecondary,
                style =
                    MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.W500,
                        fontSize = 13.sp,
                    ),
            )

            Text(
                text = "$filledCount/$IMPORT_WORD_COUNT",
                color = ImportTextPrimary,
                style =
                    MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.W600,
                        fontSize = 13.sp,
                    ),
            )
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(ImportBorderMuted, RoundedCornerShape(999.dp)),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .height(8.dp)
                        .background(
                            brush =
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFFD666), ImportGold),
                                ),
                            shape = RoundedCornerShape(999.dp),
                        ),
            )
        }
    }
}

@Composable
private fun ImportInvalidCard(
    message: String?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .background(Color(0xFF211719), RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFF503033), RoundedCornerShape(16.dp))
                .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = ImportDanger,
            modifier = Modifier.size(18.dp),
        )

        Text(
            text = message ?: IMPORT_MNEMONIC_INVALID_MESSAGE,
            color = Color(0xFFF2B8BC),
            style =
                MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.W500,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                ),
        )
    }
}

@Composable
private fun ImportWordField(
    index: Int,
    value: String,
    isInvalid: Boolean,
    focused: Boolean,
    onFocusChange: (Boolean) -> Unit,
    onValueChange: (String) -> Unit,
) {
    val density = LocalDensity.current
    val normalized = value.trim().lowercase()
    val suggestions =
        remember(value) {
            if (normalized.length < 2) {
                emptyList()
            } else {
                IMPORT_DICTIONARY_WORDS
                    .asSequence()
                    .filter { it.startsWith(normalized) }
                    .toList()
            }
        }
    val showFieldInvalid = normalized.isNotBlank() && normalized !in IMPORT_DICTIONARY_SET && !focused
    var fieldWidth by remember { mutableStateOf(0.dp) }

    Box(
        modifier =
            Modifier
                .zIndex(if (focused && suggestions.isNotEmpty()) 1f else 0f),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(ImportSurfaceRaised, RoundedCornerShape(14.dp))
                    .border(
                        width = 1.dp,
                        color = if (isInvalid || showFieldInvalid) ImportDanger else ImportBorder,
                        shape = RoundedCornerShape(14.dp),
                    ).height(44.dp)
                    .padding(horizontal = 14.dp)
                    .onGloballyPositioned { coordinates ->
                        fieldWidth = with(density) { coordinates.size.width.toDp() }
                    },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${index + 1}.",
                modifier =
                    Modifier
                        .width(28.dp)
                        .paddingFromBaseline(top = 16.dp),
                color = if (isInvalid || showFieldInvalid) ImportDanger else ImportTextSecondary,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.W500,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                    ),
            )

            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .zIndex(if (focused && suggestions.isNotEmpty()) 1f else 0f),
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = { onValueChange(it) },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .onFocusChanged { onFocusChange(it.isFocused) },
                    singleLine = true,
                    cursorBrush = SolidColor(ImportGold),
                    textStyle =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.W500,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = ImportTextPrimary,
                        ),
                    keyboardOptions =
                        KeyboardOptions(
                            imeAction = if (index == IMPORT_WORD_COUNT - 1) ImeAction.Done else ImeAction.Next,
                            capitalization = KeyboardCapitalization.None,
                        ),
                    keyboardActions = KeyboardActions(),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (value.isBlank()) {
                                Text(
                                    text = stringResource(Res.string.secret_import_word_placeholder),
                                    color = ImportTextTertiary,
                                    style =
                                        MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.W400,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                        ),
                                )
                            }

                            innerTextField()
                        }
                    },
                )
            }

            Box(
                modifier = Modifier.width(18.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                if (showFieldInvalid) {
                    Box(
                        modifier =
                            Modifier
                                .size(16.dp)
                                .background(ImportDanger, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "!",
                            color = Color.White,
                            style =
                                MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.W700,
                                    fontSize = 10.sp,
                                ),
                        )
                    }
                }
            }
        }

        if (focused && suggestions.isNotEmpty()) {
            DropdownMenu(
                expanded = true,
                onDismissRequest = { onFocusChange(false) },
                modifier =
                    Modifier
                        .width(fieldWidth + 2.dp)
                        .background(ImportSurface, RoundedCornerShape(12.dp))
                        .border(1.dp, ImportBorder, RoundedCornerShape(12.dp))
                        .heightIn(max = 184.dp),
                containerColor = ImportSurface,
            ) {
                suggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = suggestion,
                                color = ImportTextPrimary,
                                style =
                                    MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.W500,
                                        fontSize = 13.sp,
                                    ),
                            )
                        },
                        onClick = {
                            onValueChange(suggestion)
                            onFocusChange(false)
                        },
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun ImportSecretPreview() {
    AttoWalletTheme {
        ImportPhrase(
            uiState =
                ImportSecretUiState(
                    input = (1..24).joinToString(" ") { "word$it" },
                    errorMessage = null,
                ),
            onBackNavigation = {},
            onInputChanged = {},
            onDoneClicked = {},
        )
    }
}
