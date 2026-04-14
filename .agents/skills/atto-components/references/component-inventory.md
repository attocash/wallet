# Component Inventory

Shared reusable components live in `composeApp/src/commonMain/kotlin/cash/atto/wallet/components/common`.

## Common `Atto*` components

- `AttoAmountField`: amount display field for wallet amounts.
- `AttoAmountInputField`: numeric amount entry with ATTO/USD mode switch and supporting text.
- `AttoBackButton`: wallet-styled back navigation control.
- `AttoButton`: primary shared action button with `Accent`, `Secondary`, `Outlined`, and `Danger` variants.
- `AttoCard`: reusable surfaced card container used for grouped content and clickable cards.
- `AttoCopyButton`: wallet-styled copy action.
- `AttoCopyField`: display field with copy affordance.
- `AttoLoader`: loading indicator component.
- `AttoModal`: shared modal container.
- `AttoOnboardingContainer`: layout wrapper for onboarding flows.
- `AttoOutlinedButton`: legacy outlined button wrapper; check `AttoButton` variants before using.
- `AttoOutlinedTextCard`: outlined text card variant.
- `AttoPasswordField`: password entry built on the project field styling.
- `AttoReceiveButton`: receive-specific CTA.
- `AttoScreenTitle`: screen heading component.
- `AttoSendButton`: send-specific CTA.
- `AttoTextField`: shared text field wrapper with done handling and error slot.
- `AttoTransactionCard`: transaction summary row/card for overview and history screens.
- `AttoTransactionDetailsDialog`: transaction detail presentation dialog.
- `AttoWallet`: wallet summary/presentation component.
- `AttoWordChip`: mnemonic/recovery word chip.

## Related non-`Atto` shared components

- `AppBar`: shared top bar helper.
- `BottomSheet` and `BottomSheetStud`: bottom-sheet primitives.
- `OutlinedTextCard` and `TextCard`: card-style text presentation helpers.
- `QrCodeImage` and `QrScanner`: QR-specific UI.

## Theme references

- `composeApp/src/commonMain/kotlin/cash/atto/wallet/ui/AttoWalletTheme.kt`: theme entry point, color scheme, shapes.
- Search the same `ui` package for typography, colors, and formatters before introducing new visual tokens.

## Practical guidance

- Search call sites before changing a shared component API.
- Prefer adding parameters with safe defaults over forking a near-duplicate component.
- Keep previews wrapped in `AttoWalletTheme`.
