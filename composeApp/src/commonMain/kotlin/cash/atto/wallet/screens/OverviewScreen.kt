package cash.atto.wallet.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cash.atto.wallet.components.common.*
import cash.atto.wallet.ui.*
import cash.atto.wallet.uistate.overview.OverviewUiState
import cash.atto.wallet.uistate.overview.TransactionUiState
import cash.atto.wallet.viewmodel.OverviewViewModel
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

private data class OverviewAccount(
    val id: Int,
    val name: String,
    val address: String,
    val balance: Double,
    val color: Color,
    val disabled: Boolean = false,
)

private val OverviewBg = dark_bg
private val OverviewSurface = dark_surface
private val OverviewSurfaceAlt = dark_surface_alt
private val OverviewBorder = dark_border
private val OverviewText = Color.White
private val OverviewTextSecondary = dark_text_secondary
private val OverviewTextTertiary = dark_text_tertiary
private val OverviewTextMuted = dark_text_muted
private val OverviewTextDim = dark_text_dim
private val OverviewAccent = dark_accent
private val OverviewAccentSoft = dark_accent_soft
private val OverviewSuccess = dark_success
private val OverviewDanger = Color(0xFFEF4444)
private val OverviewPurple = dark_violet

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
        )
    }
}

@Composable
private fun OverviewContent(
    uiState: OverviewUiState,
    isWalletInitialized: Boolean,
    onSendClick: () -> Unit,
    onReceiveClick: () -> Unit,
    onTransactionsClick: () -> Unit,
    onStakingClick: () -> Unit,
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
        remember(currentBalance, currentAddress) {
            listOf(
                OverviewAccount(
                    id = 1,
                    name = "Main Account",
                    address = currentAddress,
                    balance = currentBalance,
                    color = OverviewAccent,
                ),
            )
        }

    var selectedAccountId by remember { mutableIntStateOf(accounts.first().id) }
    var modalOpen by remember { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<TransactionUiState?>(null) }
    val selectedAccount = accounts.firstOrNull { it.id == selectedAccountId } ?: accounts.first()
    val globalBalance = accounts.filterNot { it.disabled }.sumOf { it.balance }
    val transactions =
        uiState.transactionListUiState.transactions
            .filterNotNull()
            .take(4)

    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxSize()
                .background(OverviewBg),
    ) {
        val compact = isCompactWidth()
        val contentScroll = rememberScrollState()

        if (modalOpen) {
            AccountSwitcherDialog(
                accounts = accounts,
                selectedAccountId = selectedAccountId,
                compact = compact,
                onDismiss = { modalOpen = false },
                onSelect = {
                    selectedAccountId = it
                    modalOpen = false
                },
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
                            accountCount = accounts.size,
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
                            accountCount = accounts.size,
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
        OverviewCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OverviewCapsLabel("Global Balance")
                OverviewSmallMeta("$accountCount account${if (accountCount == 1) "" else "s"}")
            }
            Text(
                text = formatAmount(globalBalance),
                color = OverviewText,
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
                    color = OverviewTextMuted,
                    modifier = Modifier.padding(top = 4.dp),
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.W400,
                            fontSize = 14.sp,
                        ),
                )
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
                            .background(if (switchHovered) OverviewSurfaceAlt else OverviewBorder)
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
                        color = OverviewText,
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
                        tint = OverviewTextTertiary,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }

            Text(
                text = formatAmount(selectedAccount.balance),
                color = OverviewText,
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
                    color = OverviewTextMuted,
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
                    color = OverviewSuccess,
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.W700,
                            fontSize = 15.sp,
                            lineHeight = 15.sp,
                        ),
                )
            }

            HorizontalDivider(color = OverviewBorder)

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
                    color = OverviewTextMuted,
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
                    tint = OverviewTextMuted,
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
                                .background(OverviewAccentSoft),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.TrendingUp,
                            contentDescription = null,
                            tint = OverviewAccent,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stakingApy?.let { "Earning $it% APY" } ?: "APY unavailable",
                            color = OverviewSuccess,
                            style =
                                MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.W600,
                                    fontSize = 13.sp,
                                ),
                        )
                        Text(
                            text = voterName ?: "Unknown node",
                            color = OverviewTextTertiary,
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
                    tint = attoHoverTint(OverviewTextDim, stakingHovered),
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
                color = OverviewText,
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.W600,
                        fontSize = 20.sp,
                    ),
            )
            Row(
                modifier =
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onTransactionsClick,
                    ),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "View all",
                    color = OverviewAccent,
                    style =
                        MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.W500,
                            fontSize = 15.sp,
                        ),
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = OverviewAccent,
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
                        .background(OverviewSurface)
                        .border(1.dp, OverviewBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Transactions will appear here once the wallet starts receiving or sending ATTO.",
                    color = OverviewTextSecondary,
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
    selectedAccountId: Int,
    compact: Boolean,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
) {
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
            items(accounts) { account ->
                AccountSwitcherRow(
                    account = account,
                    selected = account.id == selectedAccountId,
                    onClick = { onSelect(account.id) },
                )
            }
        }

        AttoButton(
            text = "Add Account",
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Outlined.Add,
        )
    }
}

@Composable
private fun AccountSwitcherRow(
    account: OverviewAccount,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (selected) OverviewSurfaceAlt else OverviewSurface)
                .border(
                    1.dp,
                    if (selected) OverviewAccent else OverviewBorder,
                    RoundedCornerShape(12.dp),
                ).clickable(
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
                    text = account.name,
                    color = OverviewText,
                    style =
                        MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.W600,
                            fontSize = 15.sp,
                        ),
                )
                Text(
                    text = "#${account.id - 1}",
                    color = OverviewTextMuted,
                    style =
                        MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.W500,
                            fontSize = 11.sp,
                        ),
                )
            }

            Text(
                text = account.address,
                color = OverviewTextMuted,
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
                    color = OverviewText,
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
                tint = OverviewTextTertiary,
                contentDescription = "Copy address",
            )
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = null,
                tint = OverviewTextTertiary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun OverviewCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    background: Color = OverviewSurface,
    borderColor: Color = OverviewBorder,
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
        color = OverviewTextTertiary,
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
        color = OverviewTextDim,
        style =
            MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.W500,
                fontSize = 11.sp,
            ),
    )
}

private fun formatAmount(amount: Double): String = AttoFormatter.format(amount.toString())

private fun normalizeAttoUri(address: String): String =
    when {
        address.startsWith("atto://") -> address
        address.startsWith("atto_") -> "atto://$address"
        else -> address
    }
