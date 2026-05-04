package cash.atto.wallet.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cash.atto.wallet.components.common.*
import cash.atto.wallet.model.UserPreferences
import cash.atto.wallet.model.defaultAccountName
import cash.atto.wallet.ui.*
import cash.atto.wallet.uistate.overview.OverviewUiState
import cash.atto.wallet.uistate.overview.TransactionUiState
import cash.atto.wallet.viewmodel.OverviewViewModel
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

private data class OverviewAccount(
    val id: UInt,
    val name: String,
    val address: String,
    val balance: Double,
    val color: Color,
    val active: Boolean,
)

@Composable
fun OverviewScreen(
    isWalletInitialized: Boolean,
    onSendClick: () -> Unit,
    onReceiveClick: () -> Unit,
    onTransactionsClick: () -> Unit,
    onStakingClick: () -> Unit,
) {
    KoinContext {
        val overviewViewModel = koinViewModel<OverviewViewModel>()
        val overviewUiState = overviewViewModel.state.collectAsState()

        OverviewContent(
            uiState = overviewUiState.value,
            isWalletInitialized = isWalletInitialized,
            onSendClick = onSendClick,
            onReceiveClick = onReceiveClick,
            onTransactionsClick = onTransactionsClick,
            onStakingClick = onStakingClick,
            onSelectAccount = overviewViewModel::selectAccount,
            onAddAccount = overviewViewModel::addAccount,
            onToggleAccount = overviewViewModel::setAccountActive,
            onNameAccount = overviewViewModel::nameAccount,
        )
    }
}

@Composable
internal fun OverviewContent(
    uiState: OverviewUiState,
    isWalletInitialized: Boolean,
    onSendClick: () -> Unit,
    onReceiveClick: () -> Unit,
    onTransactionsClick: () -> Unit,
    onStakingClick: () -> Unit,
    onSelectAccount: (UInt) -> Unit,
    onAddAccount: (String) -> Unit,
    onToggleAccount: (UInt, Boolean) -> Unit,
    onNameAccount: (UInt, String) -> Unit,
) {
    val currentBalance =
        uiState.headerUiState.attoCoins
            ?.toStringExpanded()
            ?.toDoubleOrNull() ?: 0.0
    val priceUsd = uiState.priceUsd
    val incomingAmount = uiState.pendingReceivableAmount
    val stakingApy = uiState.apy?.toPlainString()
    val voterName = uiState.voterName
    val currentAddress = uiState.receiveAddress?.let(::normalizeAttoUri).orEmpty()

    val accounts =
        remember(uiState.accounts, currentBalance, currentAddress) {
            uiState.accounts
                .map { account ->
                    OverviewAccount(
                        id = account.index,
                        name = account.name,
                        address = account.address,
                        balance = account.balance?.toStringExpanded()?.toDoubleOrNull() ?: 0.0,
                        color = accountColor(account.index),
                        active = account.active,
                    )
                }.ifEmpty {
                    listOf(
                        OverviewAccount(
                            id = 0U,
                            name = defaultAccountName(0U),
                            address = currentAddress,
                            balance = currentBalance,
                            color = dark_accent,
                            active = true,
                        ),
                    )
                }
        }

    var modalOpen by remember { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<TransactionUiState?>(null) }
    val selectedAccount = accounts.firstOrNull { it.id == uiState.selectedAccountIndex } ?: accounts.first()
    val activeAccountCount = accounts.count { it.active }
    val globalBalance = accounts.filter { it.active }.sumOf { it.balance }
    val transactions =
        uiState.transactionListUiState.transactions
            .filterNotNull()
            .take(4)

    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxSize()
                .background(dark_bg),
    ) {
        val compact = isCompactWidth()
        val contentScroll = rememberScrollState()

        if (modalOpen) {
            AccountSwitcherDialog(
                accounts = accounts,
                selectedAccountId = selectedAccount.id,
                onDismiss = { modalOpen = false },
                onSelect = {
                    onSelectAccount(it)
                    modalOpen = false
                },
                onToggleAccount = onToggleAccount,
                onAddAccount = onAddAccount,
                onNameAccount = onNameAccount,
            )
        }
        selectedTransaction?.let { transaction ->
            AttoTransactionDetailsDialog(
                transaction = transaction,
                onDismiss = { selectedTransaction = null },
            )
        }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(contentScroll),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier =
                    Modifier
                        .widthIn(max = 1400.dp)
                        .fillMaxWidth()
                        .padding(bottom = 0.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                if (compact) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        OverviewLeftColumn(
                            globalBalance = globalBalance,
                            selectedAccount = selectedAccount,
                            accountCount = activeAccountCount,
                            incomingAmount = incomingAmount,
                            priceUsd = priceUsd,
                            stakingApy = stakingApy,
                            voterName = voterName,
                            isWalletInitialized = isWalletInitialized,
                            onSwitchClick = { modalOpen = true },
                            onStakingClick = onStakingClick,
                            onSendClick = onSendClick,
                            onReceiveClick = onReceiveClick,
                        )
                        OverviewRightColumn(
                            transactions = transactions,
                            onTransactionsClick = onTransactionsClick,
                            onTransactionClick = { selectedTransaction = it },
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        OverviewLeftColumn(
                            modifier = Modifier.weight(5f),
                            globalBalance = globalBalance,
                            selectedAccount = selectedAccount,
                            accountCount = activeAccountCount,
                            incomingAmount = incomingAmount,
                            priceUsd = priceUsd,
                            stakingApy = stakingApy,
                            voterName = voterName,
                            isWalletInitialized = isWalletInitialized,
                            onSwitchClick = { modalOpen = true },
                            onStakingClick = onStakingClick,
                            onSendClick = onSendClick,
                            onReceiveClick = onReceiveClick,
                        )
                        OverviewRightColumn(
                            modifier = Modifier.weight(7f),
                            transactions = transactions,
                            onTransactionsClick = onTransactionsClick,
                            onTransactionClick = { selectedTransaction = it },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewLeftColumn(
    globalBalance: Double,
    selectedAccount: OverviewAccount,
    accountCount: Int,
    incomingAmount: com.ionspin.kotlin.bignum.decimal.BigDecimal,
    priceUsd: com.ionspin.kotlin.bignum.decimal.BigDecimal?,
    stakingApy: String?,
    voterName: String?,
    isWalletInitialized: Boolean,
    onSwitchClick: () -> Unit,
    onStakingClick: () -> Unit,
    onSendClick: () -> Unit,
    onReceiveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stakingInteractionSource = remember { MutableInteractionSource() }
    val stakingHovered by stakingInteractionSource.collectIsHoveredAsState()
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        if (accountCount > 1) {
            OverviewCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OverviewCapsLabel("Global Balance")
                    OverviewSmallMeta("$accountCount accounts")
                }
                Text(
                    text = formatAmount(globalBalance),
                    color = dark_text_primary,
                    modifier = Modifier.padding(top = 16.dp),
                    style =
                        MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.W600,
                            fontSize = 40.sp,
                            lineHeight = 40.sp,
                            letterSpacing = (-0.8).sp,
                        ),
                )
                priceUsd?.let { price ->
                    val usdValue = globalBalance * price.doubleValue(exactRequired = false)
                    val usdFormatted =
                        (usdValue * 100).toLong().let { cents ->
                            "${cents / 100}.${(cents % 100).toString().padStart(2, '0')}"
                        }
                    Text(
                        text = "~ $$usdFormatted USD",
                        color = dark_text_muted,
                        modifier = Modifier.padding(top = 4.dp),
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.W400,
                                fontSize = 14.sp,
                            ),
                    )
                }
            }
        }

        OverviewCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(selectedAccount.color),
                    )
                    OverviewCapsLabel(selectedAccount.name)
                }

                val switchInteractionSource = remember { MutableInteractionSource() }
                val switchHovered by switchInteractionSource.collectIsHoveredAsState()
                Row(
                    modifier =
                        Modifier
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (switchHovered) dark_surface_alt else dark_border)
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable(
                                interactionSource = switchInteractionSource,
                                indication = null,
                                onClick = onSwitchClick,
                            ).padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "SWITCH",
                        color = dark_text_primary,
                        style =
                            MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.W600,
                                fontSize = 11.sp,
                                letterSpacing = 0.6.sp,
                            ),
                    )
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = "Switch account",
                        tint = dark_text_tertiary,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }

            Text(
                text = formatAmount(selectedAccount.balance),
                color = dark_text_primary,
                modifier = Modifier.padding(top = 16.dp),
                style =
                    MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.W600,
                        fontSize = 64.sp,
                        lineHeight = 64.sp,
                        letterSpacing = (-1.28).sp,
                    ),
            )
            priceUsd?.let { price ->
                val usdValue = selectedAccount.balance * price.doubleValue(exactRequired = false)
                val usdFormatted =
                    (usdValue * 100).toLong().let { cents ->
                        "${cents / 100}.${(cents % 100).toString().padStart(2, '0')}"
                    }
                Text(
                    text = "~ $$usdFormatted USD",
                    color = dark_text_muted,
                    modifier = Modifier.padding(top = 4.dp),
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.W400,
                            fontSize = 14.sp,
                        ),
                )
            }

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OverviewCapsLabel("Incoming")
                Text(
                    text = "+${AttoFormatter.format(incomingAmount)}",
                    color = dark_success,
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.W700,
                            fontSize = 15.sp,
                            lineHeight = 15.sp,
                        ),
                )
            }

            HorizontalDivider(color = dark_border)

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = selectedAccount.address,
                    modifier = Modifier.weight(1f),
                    color = dark_text_muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.W400,
                            fontSize = 13.sp,
                        ),
                )
                AttoCopyButton(
                    text = selectedAccount.address,
                    tint = dark_text_muted,
                    contentDescription = "Copy address",
                )
            }
        }

        OverviewCard(
            modifier =
                Modifier
                    .alpha(if (isWalletInitialized) 1f else 0.55f),
            interactionSource = if (isWalletInitialized) stakingInteractionSource else null,
            onClick = if (isWalletInitialized) onStakingClick else null,
            contentPadding = PaddingValues(24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(dark_accent_soft),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.TrendingUp,
                            contentDescription = null,
                            tint = dark_accent,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stakingApy?.let { "Earning $it% APY" } ?: "APY unavailable",
                            color = dark_success,
                            style =
                                MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.W600,
                                    fontSize = 13.sp,
                                ),
                        )
                        Text(
                            text = voterName ?: "Unknown node",
                            color = dark_text_tertiary,
                            style =
                                MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.W500,
                                    fontSize = 11.sp,
                                ),
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = attoHoverTint(dark_text_dim, stakingHovered),
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AttoButton(
                text = "Send",
                onClick = onSendClick,
                modifier =
                    Modifier
                        .weight(1f)
                        .alpha(if (isWalletInitialized) 1f else 0.55f),
                enabled = isWalletInitialized,
                icon = Icons.Outlined.ArrowUpward,
            )
            AttoButton(
                text = "Receive",
                onClick = onReceiveClick,
                modifier = Modifier.weight(1f),
                variant = AttoButtonVariant.Outlined,
                icon = Icons.Outlined.ArrowDownward,
            )
        }
    }
}

@Composable
private fun OverviewRightColumn(
    transactions: List<TransactionUiState>,
    onTransactionsClick: () -> Unit,
    onTransactionClick: (TransactionUiState) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Recent Activity",
                color = dark_text_primary,
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.W600,
                        fontSize = 20.sp,
                    ),
            )
            Row(
                modifier =
                    Modifier
                        .pointerHoverIcon(PointerIcon.Hand)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onTransactionsClick,
                        ),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "View all",
                    color = dark_accent,
                    style =
                        MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.W500,
                            fontSize = 15.sp,
                        ),
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = dark_accent,
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        if (transactions.isEmpty()) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(dark_surface)
                        .border(1.dp, dark_border, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Transactions will appear here once the wallet starts receiving or sending ATTO.",
                    color = dark_text_secondary,
                    style =
                        MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.W400,
                            fontSize = 12.sp,
                        ),
                )
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                transactions.forEach { transaction ->
                    AttoTransactionCard(
                        transaction = transaction,
                        onClick = { onTransactionClick(transaction) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountSwitcherDialog(
    accounts: List<OverviewAccount>,
    selectedAccountId: UInt,
    onDismiss: () -> Unit,
    onSelect: (UInt) -> Unit,
    onToggleAccount: (UInt, Boolean) -> Unit,
    onAddAccount: (String) -> Unit,
    onNameAccount: (UInt, String) -> Unit,
) {
    val activeAccountCount = accounts.count { it.active }

    AttoModal(
        title = "Switch Account",
        onDismiss = onDismiss,
        scrollable = false,
    ) {
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = accounts,
                key = { it.id },
            ) { account ->
                AccountSwitcherRow(
                    account = account,
                    selected = account.id == selectedAccountId,
                    activeAccountCount = activeAccountCount,
                    onClick = { if (account.active) onSelect(account.id) },
                    onToggleActive = { active -> onToggleAccount(account.id, active) },
                    onNameAccount = { name -> onNameAccount(account.id, name) },
                )
            }
        }

        AttoButton(
            text = "Add Account",
            onClick = { onAddAccount("") },
            modifier = Modifier.fillMaxWidth(),
            enabled = activeAccountCount < UserPreferences.MAX_ACTIVE_ACCOUNT_COUNT,
            icon = Icons.Outlined.Add,
        )
    }
}

@Composable
private fun AccountSwitcherRow(
    account: OverviewAccount,
    selected: Boolean,
    activeAccountCount: Int,
    onClick: () -> Unit,
    onToggleActive: (Boolean) -> Unit,
    onNameAccount: (String) -> Unit,
) {
    var editingName by remember(account.id) { mutableStateOf(false) }
    var editedName by remember(account.id) { mutableStateOf("") }
    val canToggle =
        if (account.active) {
            activeAccountCount > 1
        } else {
            activeAccountCount < UserPreferences.MAX_ACTIVE_ACCOUNT_COUNT
        }

    fun stopEditing() {
        editedName = ""
        editingName = false
    }

    OverviewCard(
        modifier =
            Modifier
                .fillMaxWidth()
                .alpha(if (account.active) 1f else 0.55f),
        contentPadding = PaddingValues(16.dp),
        background = if (selected) dark_surface_alt else dark_surface,
        borderColor = if (selected) dark_accent else dark_border,
        onClick = if (account.active && !editingName) onClick else null,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier =
                    Modifier
                        .padding(top = 5.dp)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(account.color),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "#${account.id}",
                        color = dark_text_muted,
                        style =
                            MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.W500,
                                fontSize = 11.sp,
                            ),
                    )
                    AccountNameEditor(
                        name = account.name,
                        value = editedName,
                        isEditing = editingName,
                        onValueChange = { editedName = it },
                        onStartEditing = {
                            editedName = ""
                            editingName = true
                        },
                        onConfirm = {
                            onNameAccount(editedName)
                            stopEditing()
                        },
                        onClear = {
                            onNameAccount("")
                            stopEditing()
                        },
                        modifier = Modifier.weight(1f),
                    )
                }

                Text(
                    text = account.address,
                    color = dark_text_muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style =
                        MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.W400,
                            fontSize = 11.sp,
                        ),
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = formatAmount(account.balance),
                        color = dark_text_primary,
                        style =
                            MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.W600,
                                fontSize = 20.sp,
                            ),
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AttoCopyButton(
                    text = account.address,
                    size = 20.dp,
                    tint = dark_text_tertiary,
                    contentDescription = "Copy address",
                )
                AccountVisibilityButton(
                    active = account.active,
                    enabled = canToggle,
                    onClick = { onToggleActive(!account.active) },
                )
            }
        }
    }
}

@Composable
private fun AccountVisibilityButton(
    active: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    AccountIconButton(
        imageVector = if (active) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
        contentDescription = if (active) "Deactivate account" else "Activate account",
        enabled = enabled,
        tint = dark_text_tertiary,
        disabledTint = dark_text_dim,
        onClick = onClick,
    )
}

@Composable
private fun AccountNameEditor(
    name: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit,
    onStartEditing: () -> Unit,
    onConfirm: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isEditing) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = accountNameTextStyle(dark_text_primary),
                cursorBrush = SolidColor(dark_text_primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onConfirm() }),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (value.isBlank()) {
                            Text(
                                text = name,
                                color = dark_text_muted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = accountNameTextStyle(dark_text_muted),
                            )
                        }
                        innerTextField()
                    }
                },
            )
            AccountIconButton(
                imageVector = Icons.Outlined.Check,
                contentDescription = "Save account name",
                tint = dark_text_primary,
                onClick = onConfirm,
            )
            AccountIconButton(
                imageVector = Icons.AutoMirrored.Outlined.Backspace,
                contentDescription = "Clear account name",
                onClick = onClear,
            )
        }
    } else {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = name,
                modifier = Modifier.weight(1f),
                color = dark_text_primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = accountNameTextStyle(dark_text_primary),
            )
            AccountIconButton(
                imageVector = Icons.Outlined.Edit,
                contentDescription = "Edit account name",
                onClick = onStartEditing,
            )
        }
    }
}

@Composable
private fun AccountIconButton(
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = dark_text_tertiary,
    disabledTint: Color = dark_text_dim,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    Box(
        modifier =
            modifier
                .size(24.dp)
                .clip(RoundedCornerShape(8.dp))
                .then(
                    if (enabled) {
                        Modifier
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = onClick,
                            )
                    } else {
                        Modifier
                    },
                ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint =
                if (enabled) {
                    attoHoverTint(tint, hovered)
                } else {
                    disabledTint
                },
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun OverviewCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    background: Color = dark_surface,
    borderColor: Color = dark_border,
    interactionSource: MutableInteractionSource? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    AttoCard(
        modifier = modifier,
        background = background,
        border = borderColor,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Top,
            content = content,
        )
    }
}

@Composable
private fun OverviewCapsLabel(text: String) {
    Text(
        text = text,
        color = dark_text_tertiary,
        style =
            MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.W500,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
            ),
    )
}

@Composable
private fun OverviewSmallMeta(text: String) {
    Text(
        text = text,
        color = dark_text_dim,
        style =
            MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.W500,
                fontSize = 11.sp,
            ),
    )
}

@Composable
private fun accountNameTextStyle(color: Color) =
    MaterialTheme.typography.titleSmall.copy(
        color = color,
        fontWeight = FontWeight.W600,
        fontSize = 15.sp,
    )

private fun formatAmount(amount: Double): String = AttoFormatter.format(BigDecimal.fromDouble(amount))

private fun accountColor(index: UInt): Color =
    when (index % 5U) {
        0U -> dark_accent
        1U -> dark_violet
        2U -> dark_success
        3U -> dark_account_sky
        else -> dark_account_amber
    }

private fun normalizeAttoUri(address: String): String =
    when {
        address.startsWith("atto://") -> address
        address.startsWith("atto_") -> "atto://$address"
        else -> address
    }
