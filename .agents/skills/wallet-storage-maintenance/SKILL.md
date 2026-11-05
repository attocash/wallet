---
name: "wallet-storage-maintenance"
version: "1.0.0"
description: "Use when changing this wallet's encrypted persistence, logout/reset, password rotation, terms-acceptance persistence, or browser storage migrations. Excludes password-strength scoring, visual-only password forms, and brand assets."
license: "BSD-3-Clause"
compatibility: "Kotlin Multiplatform wallet; JDK 17; Chromium for Wasm validation"
metadata:
  scope: "repo-local"
  workflow: "wallet-storage-maintenance"
---

# Wallet storage maintenance

## Prerequisites

Read `AGENTS.md`. Trace `AppStateRepository`, `WalletKeyStore`, `PreferencesRepository`, and the affected platform data sources. Use the repository's Kotlin and browser verification guidance for implementation and execution.

## Procedure

1. Reproduce the failing lifecycle before editing. Distinguish first creation, unlock, lock, logout, reimport, password rotation, and preference updates. An unreadable record alone does not prove development leftovers.
2. State the data boundary. Logout removes wallet credentials, labels/account preferences, and accepted terms, while retaining device work settings. Lock only revokes active access; it does not delete persistent data.
3. Treat the browser seed and encrypted preferences as one credential change. Prepare encryption before persistence; use the shared storage lock and expected snapshot for the final write. Reject stale-session writes even when the preference blob is empty by checking that the persisted wallet belongs to the active credentials.
4. Preserve recoverability on failure. Before deleting legacy aliases, make the original wallet authoritative in canonical storage. Commit the replacement or tombstone last, with no fallible secret cleanup afterward. An authentication failure must not silently erase unreadable data.
5. Revoke retained signers and running wallet work on lock/logout. Test caller cancellation and storage failures; do not report an empty wallet while persistent credentials still exist.
6. Cover logout followed by creation AND reimport using another password. Check fresh labels/accounts, new terms acceptance, preference updates, password rotation, reload, and stale-tab rejection. Include legacy and canonical storage, write/removal failures, and unrelated-setting retention.
7. For migration changes, create wallet records through a real previous build, then switch builds on the same browser origin without clearing storage. Use isolated synthetic wallets and block node/API requests. Encrypted fixtures complement this test; they do not replace it.

## Verification pitfalls

- Preference state loads asynchronously. An immediate empty export can precede successful decryption. Check settled UI state or independently decrypt saved synthetic snapshots before claiming stored data loss.
- Compare preference semantics: serialization can omit default account statuses and empty arrays, and labels can be sorted during normalization.
- Compose accessibility text may merge modal title/body/error. Wait for the intended state and modal exit before clicking underlying controls; cached control coordinates can help when semantics disappear after dismissal.
- Older Compose password fields can drop automated keystrokes under load. Verify the intended password can decrypt the old wallet before classifying an upgrade unlock failure.
- Keep real recovery phrases, passwords, ciphertext, and user labels out of logs, prompts, and test fixtures. Metadata-only diagnostics can report versions, purposes, KDF parameters, and byte lengths.
- An incremental build can retain an old displayed version when generation lacks version inputs. Record the checked-out source revision and inspect build outputs instead of relying only on the displayed label.

Run focused behavior tests and the affected Wasm build. Report automated tests, actual-browser lifecycle checks, platform limits, and data-preservation evidence separately. Do not run Android tests for this repository.
