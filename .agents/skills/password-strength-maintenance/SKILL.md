---
name: "password-strength-maintenance"
version: "1.3.0"
description: "Use when changing or reviewing this wallet's shared Kotlin password-strength algorithm, frozen dictionaries or keyboard graphs, reference fixtures, numeric compatibility, or password-creation acceptance policy. Skip visual-only meter styling that leaves scoring unchanged."
license: "MIT"
compatibility: "Kotlin Multiplatform wallet with JVM and Wasm browser tests"
metadata:
  scope: "repo-local"
  workflow: "password-strength-maintenance"
---

# Password strength maintenance

Keep the wallet's strength estimator local, inspectable, and compatible with its frozen reference.

## Establish the contract

1. Read the source versions and attribution in `composeApp/src/commonMain/composeResources/files/password-strength-estimator-notices.txt`, the current creation policy in `CheckPasswordInteractor` and `PasswordStrength`, and the relevant tests.
2. Identify whether the requested change affects policy, the algorithm, data, or presentation. Preserve existing-wallet unlock when changing creation requirements. Password-manager generation hints are a separate contract from acceptance.
3. Establish the baseline before changing behavior. Keep scoring configuration, context terms, reference year, UTF-16 indexing, input bounds, and numeric behavior explicit.

## Change the owning layer

- Matching, scoring, and immutable match types live in `commonMain/kotlin/cash/atto/wallet/password`. Use the sealed match hierarchy and exhaustive scoring; keep per-password search state local to an analysis.
- Platform code supplies the local reference year only. Do not reintroduce an estimator npm package, JVM library, remote scoring service, raw-password logging, or a persistent password-result cache as a convenience.
- Frozen data lives in checked-in readable Kotlin under `composeApp/src/commonMain/kotlin/cash/atto/wallet/password/data/`. The six large word-list files use one word per line in multiline strings split below JVM literal limits; `WordSequenceWords.kt` contains the small ordered lists and `PasswordKeyboardGraphs.kt` contains direct adjacency maps. `BundledPasswordData` builds ranked dictionaries from these sequences. Do not reintroduce JSON input, Gradle data generation or runtime JSON parsing. Preserve ranks, duplicate ordering, word case, graph entry/neighbor order, null slots and source/license attribution when updating data.
- Target roughly 48 KB per raw-string block, leaving room below the JVM's 65,535-byte modified UTF-8 constant limit. Write dollar signs in raw word literals as `${'$'}` to prevent Kotlin interpolation. Check complete data fingerprints after formatting or moving literals; visible source alone does not establish preservation.
- Follow the documented compatibility behavior even for unusual dictionary keys or numeric edge cases. Changes that intentionally alter results need an explicit requested behavior change, not a looser parity assertion.
- Keep original dataset attribution in the bundled notices and dictionary headers, alongside the algorithm's upstream copyright and license. Trace publisher data through the actual generation source and compare complete ordered lists when possible; matching counts or a dataset name do not establish origin. Describe inherited normalization, filtering or substitutions rather than claiming an unmodified source copy. Retain upstream credit for curated lists with no separate external source.

## Maintain the independent oracle

- Refactoring Kotlin must not regenerate expected results from the changed Kotlin implementation.
- Reference refresh is a manual development operation using an explicitly supplied, already reviewed reference installation. Run `node scripts/generate-password-strength-reference.mjs /absolute/path/to/reference/node_modules`; it checks the package versions. Ordinary builds and CI must not install or execute that reference. Shared fingerprint tests independently preserve the checked-in Kotlin dictionaries and graphs. Do not derive new expected fingerprints from changed production data.
- The manual numeric generator uses thirteen fixed bases and the exact Node/V8 engine declared in `scripts/generate-password-math.mjs`, with no JSON input. Keep every numeric bit fingerprint and graph-degree coverage assertion; a source-comment edit can change the full-file hash without changing numeric values.
- Keep fixture strings ASCII-escaped by UTF-16 unit. Raw BOM characters can be removed by formatting even inside a string, and supplementary characters must retain both surrogate units.
- Compare both score and guess count exactly against the frozen reference. Investigate floating-point primitive differences instead of adding tolerances to make a port pass.

## Verify

1. Format touched algorithm and test Kotlin files using the repository's scoped formatter workflow. Exclude the large checked-in word-list literals and numeric oracle from broad formatter passes; preserve literal content and chunk boundaries. Keep frozen fixture strings ASCII-escaped.
2. Run the relevant shared policy, matcher, scoring, reference and complete bundled-data fingerprint tests on JVM and Wasm. Preserve coverage for Unicode, bounds, case mappings, repetitions, contextual words, dictionary-key quirks, and score boundaries.
3. Confirm the dependency/catalog/lock changes remove only the intended estimator dependencies. Keep unrelated wallet dependencies and worktree changes intact.
4. For changes reaching the form, use the browser verification skill with synthetic wallet data: check weak rejection, strong passphrases, manager-style values, silent autofill, confirmation, creation, reload, and old-password unlock. Report browser checks separately from automated tests.
5. Review the final diff and attribution. Report the tested reference coverage and remaining limits; sampled parity is not a formal proof or a security guarantee.
