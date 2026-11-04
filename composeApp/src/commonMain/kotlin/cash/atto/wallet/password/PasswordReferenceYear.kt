package cash.atto.wallet.password

// Only the environment clock is platform-specific; matching and scoring stay shared.
internal expect fun currentPasswordReferenceYear(): Int
