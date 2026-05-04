# Atto Wallet

Atto Wallet is a Kotlin Multiplatform wallet for the Atto network, built with Compose Multiplatform.
It covers wallet onboarding, local key storage, send/receive flows, transaction history, staking/voter selection, and a web PWA build.

## Support Status

This repository contains multiple targets, but they are not all in the same state:

| Target | Status | Notes |
| --- | --- | --- |
| WebAssembly (`wasmJs`) | Actively supported | Main target, main deployment target, and the one to prefer for development and release validation. |
| Desktop (`desktop`) | Best effort | Should build and run, but is not actively tested as a supported product target. |
| Android (`android`) | Best effort | Should build and run, but is not actively tested as a supported product target. |
| iOS | Not currently supported | An `iosApp/` scaffold exists, but there is no active iOS Gradle target in the current build. |

If you are deciding where to spend time, assume the web build is the source of truth.

## Features

- Create a new wallet with a 24-word recovery phrase
- Import an existing wallet from its recovery phrase
- Protect local access with a password
- Send Atto to an address or payment request
- Receive Atto via address sharing and QR codes
- Scan QR codes for payments
- View recent activity and full transaction history
- Export transactions as CSV
- Delegate voting weight to voters and inspect staking metrics
- Show live network metrics such as price, market data, and confirmation speed
- Auto-receive pending receivables in the background
- Install the web build as a PWA

## Tech Stack

- Kotlin `2.3.20`
- Compose Multiplatform `1.10.3`
- Android Gradle Plugin `8.13.0`
- Koin for DI and view model wiring
- Decompose for navigation/state routing
- Ktor for HTTP access to Atto projections
- `cash.atto:commons-*` for wallet, node, worker, and network integration
- Room 3 plus SQLite for local persistence
- `androidx.sqlite:sqlite-web` plus a web worker for the browser target
- Firebase Hosting for web deployment

## Runtime Model

- The app is currently hard-wired to `AttoNetwork.LIVE`.
- Network projection data is refreshed every minute.
- Pending receivables are processed in the background roughly every 10 seconds.
- Session unlock currently expires after 20 minutes.
- Desktop packaging is configured for `deb`, `msi`, `dmg`, and `rpm`, but only the web target should be treated as actively supported.

## Repository Layout

```text
.
├── composeApp/          Main Kotlin Multiplatform application
│   ├── src/commonMain/  Shared UI, view models, repositories, and domain logic
│   ├── src/wasmJsMain/  Browser/PWA entry point and web-specific integrations
│   ├── src/desktopMain/ Desktop entry point and platform storage/export code
│   ├── src/androidMain/ Android entry point and platform storage/export code
│   └── schemas/         Room schema snapshots
├── design/              Design reference app and UI exploration workspace
├── iosApp/              Legacy iOS scaffold, not an active target today
├── firebase.json        Hosting configuration for the web artifact
└── .github/workflows/   CI, build, preview, release, and deploy workflows
```

## Prerequisites

Recommended local setup:

- JDK 17
- Android Studio or Android SDK, if you want to run the Android target
- Node.js and npm, if you want to run or build the WASM target
- A modern browser with camera support, if you want to test QR scanning on web

Notes:

- CI uses Temurin Java 17.
- Android is configured with `minSdk 26`, `targetSdk 36`, and `compileSdk 36`.
- The web target depends on npm packages under `composeApp/sqlite-web-worker/`.
- Camera-based QR scanning on web generally needs `localhost` or HTTPS.

## Getting Started

### 1. Clone and enter the repository

```bash
git clone <your-fork-or-remote>
cd wallet
```

### 2. Confirm Android SDK setup if needed

For Android work, make sure `local.properties` points at your SDK, for example:

```properties
sdk.dir=/path/to/Android/sdk
```

### 3. Use the target you care about

The web target is the recommended default.

## Running From Source

### Web (recommended)

Start the development server:

```bash
./gradlew wasmJsBrowserDevelopmentRun
```

Gradle will print the local URL for the dev server. Open that URL in your browser.

If a Kotlin/Wasm Gradle task fails with an internal compiler error during incremental compilation, rerun it once with `--rerun-tasks`. For example:

```bash
./gradlew :composeApp:compileKotlinWasmJs --rerun-tasks
```

The same workaround also applies to other web tasks such as `./gradlew wasmJsBrowserDevelopmentRun --rerun-tasks`.

Useful screen deep-links for UI work:

- `?screen=welcome`
- `?screen=overview`
- `?screen=send`
- `?screen=receive`
- `?screen=transactions`
- `?screen=settings`
- `?screen=staking`
- `?screen=recovery-phrase`
- `?screen=import-phrase`
- `?screen=create-password`
- `?screen=og-image`

### Desktop

Run the desktop target:

```bash
./gradlew composeApp:run
```

This target should still work technically, but it is not actively tested.

### Android

Use Android Studio for the smoothest workflow, or install the debug build from Gradle:

```bash
./gradlew composeApp:installDebug
```

Android is present in the codebase, but it is not actively tested as a maintained target.

## Building

### Web production bundle

```bash
./gradlew wasmJsBrowserDistribution
```

Output:

```text
composeApp/build/dist/wasmJs/productionExecutable
```

### Desktop packages

Build platform-specific desktop artifacts:

```bash
./gradlew packageDeb
./gradlew packageDmg
./gradlew packageMsi
./gradlew packageRpm
```

These packaging tasks exist in the build, but desktop is not an actively supported release target right now.

### Android APK

```bash
./gradlew composeApp:assembleDebug
```

## Testing And Quality

Run the main test suite:

```bash
./gradlew allTests
```

Run lint/format tasks:

```bash
./gradlew ktlintCheck
./gradlew ktlintFormat
```

Current test coverage is limited. There is at least one Android instrumented test in the repo, but the project should not be treated as broadly covered across all targets.

## Storage And Security Notes

The storage model differs by platform:

| Platform | Seed storage | Password storage | Local app data |
| --- | --- | --- | --- |
| Web | Stored in `localStorage` after being encrypted with the user password | Kept in memory only during the running session | Browser-local SQLite via `sqlite-web` and a web worker |
| Android | Encrypted and stored locally via DataStore/Keystore-backed utilities | Encrypted and stored locally | Bundled SQLite |
| Desktop | Delegated to platform-specific desktop storage implementations | Delegated to platform-specific desktop storage implementations | Bundled SQLite |

Important behavior:

- Logging out deletes the locally stored wallet keys from the device.
- Recovery depends on the 24-word phrase. If that phrase is gone, funds cannot be recovered through the app.
- The web target keeps a salt in browser storage and uses Web Crypto for seed encryption/decryption.

## Network And Data Flow

The app pulls projection data from Atto backend services and combines that with local wallet state:

- `HomeRepository` fetches home/market/address data
- `MetricsRepository` fetches network metrics
- `VotersRepository` fetches staking/voter data
- `WalletManagerRepository` manages wallet state, pending receivables, and background receive
- `AppStateRepository` owns auth/session state and the local seed/password flow

At the moment, switching between live/dev networks is not exposed as a runtime setting. If you need that, the current starting point is the DI module in `composeApp/src/commonMain/kotlin/cash/atto/wallet/di/Modules.kt`.

## Web App And Deployment

The web target includes:

- A PWA manifest
- A service worker that caches the app shell
- Hosting headers for CSP, COOP/COEP, permissions policy, and asset caching
- Firebase Hosting configuration

Repository deployment shape:

- CI builds the web artifact with `./gradlew wasmJsBrowserDistribution`
- GitHub Actions uploads the artifact from `composeApp/build/dist/wasmJs/productionExecutable`
- Firebase Hosting is configured to serve a prepared artifact directory at `artifacts/web`

If you are wiring up manual deployment outside CI, keep that artifact path mismatch in mind.

The social preview image is generated from a hidden Compose route so it stays in the same design system as the wallet. Start the web dev server, open or capture `?screen=og-image` at `1200x630`, then write the result to `composeApp/src/wasmJsMain/resources/og-image.png`:

```bash
./gradlew wasmJsBrowserDevelopmentRun
node scripts/generate-og-image.mjs
```

That workspace is useful for UI iteration, but it is not the shipping wallet runtime.

## CI Overview

The repository already includes GitHub Actions workflows for:

- running `allTests`
- building the Linux desktop package
- building the web distribution
- creating draft GitHub releases
- deploying preview and live Firebase hosting channels

## Known Limitations

- WebAssembly is the only actively supported target.
- Desktop and Android are kept in the codebase, but they are not regularly tested.
- iOS scaffolding exists, but iOS is not currently configured as an active Gradle target.
- The wallet is currently pinned to the live network in code.
- Cross-target behavior should be validated manually before depending on non-web releases.
\\
