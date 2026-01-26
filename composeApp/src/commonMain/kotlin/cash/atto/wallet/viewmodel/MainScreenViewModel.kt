package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.commons.AttoAccount
import cash.atto.commons.AttoAddress
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.AttoUnit
import cash.atto.wallet.model.getStakingApy
import cash.atto.wallet.model.getVoter
import cash.atto.wallet.repository.HomeRepository
import cash.atto.wallet.repository.WalletManagerRepository
import cash.atto.wallet.uistate.desktop.BalanceChipUiState
import cash.atto.wallet.uistate.desktop.MainScreenUiState
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class MainScreenViewModel(
    private val walletManagerRepository: WalletManagerRepository,
    private val settingsViewModel: SettingsViewModel,
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MainScreenUiState.DEFAULT)
    val state = _state.asStateFlow()

    private var accountCollectorJob: Job? = null
    private var settingsCollectorJob: Job? = null
    private var homeCollectorJob: Job? = null
    private var currentBalance: BigDecimal? = null
    private var currentRepresentativeAddress: String? = null

    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        scope.launch {
            walletManagerRepository.state
                .filterNotNull()
                .collect { wallet ->
                    println(
                        "MainViewModel is collecting account information from wallet ${
                            AttoAddress(
                                AttoAlgorithm.V1,
                                wallet.publicKey
                            )
                        }"
                    )

                    accountCollectorJob?.cancel()
                    accountCollectorJob = scope.launch {
                        wallet.accountFlow.collect { account ->
                            handleAccount(account)
                        }
                    }
                }
        }

        settingsCollectorJob?.cancel()
        settingsCollectorJob = scope.launch {
            settingsViewModel.state.collect {
                _state.emit(
                    state.value.copy(
                        settingsUiState = it
                    )
                )
            }
        }

        homeCollectorJob?.cancel()
        homeCollectorJob = scope.launch {
            homeRepository.homeResponse.collect {
                updateBalanceWithUsd()
            }
        }
    }

    fun handleBackupNavigation() = settingsViewModel.handleBackupNavigation()
    fun hideLogoutDialog() = settingsViewModel.hideLogoutDialog()
    fun logout() = settingsViewModel.logout()

    private suspend fun handleAccount(account: AttoAccount) {
        println("Account $account")

        currentBalance = account.balance
            .toString(AttoUnit.ATTO)
            .toBigDecimal()

        currentRepresentativeAddress = AttoAddress(
            account.representativeAlgorithm,
            account.representativePublicKey
        ).toString()

        updateBalanceWithUsd()

        _state.emit(
            state.value.copy(
                isWalletInitialized = true
            )
        )
    }

    private suspend fun updateBalanceWithUsd() {
        val balance = currentBalance ?: return
        val priceUsd = homeRepository.getPriceUsd()
        val usdValue = priceUsd?.let {
            (balance * it).roundToDigitPositionAfterDecimalPoint(
                digitPosition = 2,
                roundingMode = RoundingMode.ROUND_HALF_AWAY_FROM_ZERO
            )
        }
        val apy = calculateApy()

        _state.emit(
            state.value.copy(
                balanceChipUiState = BalanceChipUiState(
                    attoCoins = balance,
                    usdValue = usdValue,
                    apy = apy
                )
            )
        )
    }

    private fun calculateApy(): BigDecimal {
        val homeResponse = homeRepository.homeResponse.value ?: return BigDecimal.ZERO
        val representativeAddress = currentRepresentativeAddress ?: return BigDecimal.ZERO

        val voter = homeResponse.getVoter(representativeAddress)
            ?: return BigDecimal.ZERO

        val globalApy = homeResponse.getStakingApy()?.let { BigDecimal.parseString(it) }
            ?: return BigDecimal.ZERO

        val sharePercentage = BigDecimal.fromInt(voter.sharePercentage)
        val hundred = BigDecimal.fromInt(100)

        return (globalApy * sharePercentage).divide(hundred).roundToDigitPositionAfterDecimalPoint(
            digitPosition = 2,
            roundingMode = RoundingMode.ROUND_HALF_AWAY_FROM_ZERO
        )
    }
}