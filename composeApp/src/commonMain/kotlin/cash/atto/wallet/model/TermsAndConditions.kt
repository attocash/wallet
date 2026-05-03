package cash.atto.wallet.model

data class TermsAndConditionsSection(
    val title: String,
    val body: String,
)

object TermsAndConditions {
    // Change this when the terms content changes so older acceptances no longer match.
    const val EFFECTIVE_DATE = "2026-05-03"

    val sections =
        listOf(
            TermsAndConditionsSection(
                title = "Agreement",
                body =
                    "Atto Wallet is provided by Atto B.V. These Terms and Conditions govern your use " +
                        "of the wallet. By using the wallet, you confirm that you have read and accepted " +
                        "these terms. If you do not agree, do not use the wallet.",
            ),
            TermsAndConditionsSection(
                title = "Eligibility and lawful use",
                body =
                    "You are responsible for making sure that you may lawfully use Atto Wallet in your " +
                        "jurisdiction. You must not use the wallet for illegal activity, sanctions evasion, " +
                        "fraud, abuse, or to interfere with the app, network, infrastructure, or other users.",
            ),
            TermsAndConditionsSection(
                title = "Self-custody",
                body =
                    "Atto Wallet is non-custodial wallet software. Your Atto remains on the Atto network, " +
                        "and the wallet helps you create, store, sign, and broadcast transactions from your " +
                        "device. We do not custody your funds, control your private keys, or guarantee access " +
                        "to your wallet.",
            ),
            TermsAndConditionsSection(
                title = "Password, recovery phrase, and device security",
                body =
                    "You are solely responsible for keeping your password, recovery phrase, device, browser " +
                        "profile, and backups secure. Anyone with access to your recovery phrase may control " +
                        "your wallet. If you lose your recovery phrase or local wallet data, we may be unable " +
                        "to restore access.",
            ),
            TermsAndConditionsSection(
                title = "Transactions",
                body =
                    "You are responsible for checking addresses, amounts, voter changes, payment requests, " +
                        "and all other transaction details before you approve them. Transactions submitted to " +
                        "a blockchain or distributed ledger may be final, delayed, rejected, or impossible for " +
                        "us to reverse, cancel, or recover.",
            ),
            TermsAndConditionsSection(
                title = "Prices and market data",
                body =
                    "Prices, USD equivalents, market capitalization, APY values, and other market or reward " +
                        "figures are purely indicative and may be incorrect, delayed, unavailable, or outdated. " +
                        "Do not rely on them as the only source for a transaction, purchase, sale, tax report, " +
                        "or investment decision.",
            ),
            TermsAndConditionsSection(
                title = "Staking and rewards",
                body =
                    "Staking, voter, APY, and reward information is informational only. Rewards are not " +
                        "guaranteed and may vary based on network rules, voter behavior, market conditions, " +
                        "eligibility, software changes, or data errors. Choosing or changing a voter is your " +
                        "own decision.",
            ),
            TermsAndConditionsSection(
                title = "No financial advice",
                body =
                    "Atto Wallet does not provide financial, investment, legal, tax, accounting, or trading " +
                        "advice. Atto and other crypto assets may lose value, may be illiquid, and may not be " +
                        "covered by deposit insurance, investor compensation schemes, or similar protections.",
            ),
            TermsAndConditionsSection(
                title = "Service and third-party data",
                body =
                    "The wallet may depend on public networks, browser storage, local databases, Atto backend " +
                        "services, network projections, market data, and other infrastructure that can fail or " +
                        "change. We do not guarantee that the wallet or displayed data will be uninterrupted, " +
                        "accurate, complete, or available at all times.",
            ),
            TermsAndConditionsSection(
                title = "Taxes",
                body =
                    "You are responsible for determining, reporting, and paying any taxes, duties, or other " +
                        "obligations that may apply to your use of Atto Wallet or your Atto transactions. The " +
                        "wallet does not calculate or remit taxes for you.",
            ),
            TermsAndConditionsSection(
                title = "No warranty and limitation of liability",
                body =
                    "Atto Wallet is provided as is and as available. To the maximum extent permitted by law, " +
                        "we disclaim warranties and are not liable for losses caused by your use of the wallet, " +
                        "lost keys or recovery phrases, user error, malware, phishing, unauthorized access, " +
                        "network failures, incorrect data, market volatility, or unavailable services.",
            ),
            TermsAndConditionsSection(
                title = "Changes",
                body =
                    "We may update these terms or the wallet from time to time. If the terms change, you may " +
                        "be asked to accept the updated version before continuing to use the wallet.",
            ),
        )
}
