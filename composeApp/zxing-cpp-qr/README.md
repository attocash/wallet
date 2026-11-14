# ZXing-C++ QR decoder

This private local npm package bundles **our build of ZXing-C++'s own upstream Wasm wrapper**.
It is not an upstream-published npm package and does not use Sec-ant's `zxing-wasm` distribution.
The generated JavaScript and Wasm are checked in so normal Gradle/CI builds need no C++ toolchain,
GitHub Actions artifact access, or runtime CDN. Webpack includes the Wasm as a same-origin asset and injects its hashed URL into the existing
service-worker app-shell precache, preserving first-use scanning after the installed PWA goes offline.

Upstream sources:

- [ZXing-C++ v3.1.1](https://github.com/zxing-cpp/zxing-cpp/releases/tag/v3.1.1),
  revision `287c85df6f961c8efbfb5ffd736cd9457b8b890e`.
- [Official Wasm wrapper and build instructions](https://github.com/zxing-cpp/zxing-cpp/tree/v3.1.1/wrappers/wasm).
- [Upstream Wasm CI build](https://github.com/zxing-cpp/zxing-cpp/blob/v3.1.1/.github/workflows/ci.yml).
- The wrapper's image parser uses [stb](https://github.com/nothings/stb), pinned to
  `9600522de53973336fa033a289858a4354cbb04c` instead of CMake fetching its moving default branch.
  ZXing-C++ also includes libzueci in its source tree. License notices for these and the
  Emscripten runtime libraries are retained in `notices/third-party/zxing-cpp-qr/` and included in the web distribution.

The wallet requests only `QRCode` decoding. The original wrapper remains unmodified, including
its image/pixmap APIs. The build disables writers and emits an ES module with dynamic execution
disabled. `index.js` shares asynchronous initialization and always frees each RGBA frame buffer,
including when decoding throws. A failed asset load can be retried when the user reopens the scanner.

## Rebuild

Use the official [Emscripten SDK 4.0.23](https://github.com/emscripten-core/emsdk/tree/4.0.23)
(SDK repository revision `c0bb220cb6e6f4e0fabb6f6db9efd53390ef5e56`),
[Kitware CMake 4.1.2](https://github.com/Kitware/CMake/releases/tag/v4.1.2), and
[Ninja 1.13.1](https://github.com/ninja-build/ninja/releases/tag/v1.13.1).
Install/activate these in a disposable build environment or temporary directory, then run:

```sh
. /path/to/emsdk/emsdk_env.sh
PATH=/path/to/cmake/bin:/path/to/ninja:$PATH ./scripts/build-zxing-cpp.sh
```

The script checks tool versions, fetches exact source commits, builds the upstream wrapper with
its supported CMake entry point, and replaces only the two generated decoder files.
No tools are installed on the host by the script. Review updated binaries, checksums and tests
when changing any pinned input. The wallet maintains these generated artifacts and its small
integration; upstream ZXing-C++ maintains the underlying implementation/wrapper.

Current generated-file SHA-256 checksums:

```text
01d56a0f1df5416d1d6ccda7f0ee1e0c24525d4db149b72528898dfb0ba22dd6  zxing_reader.js
7200a0dfe8a5d0e03be55dfb5693903174f9613a21399552785e058ab9124dea  zxing_reader.wasm
```

## Verification

`QrDecoderTest` generates QR matrices with the wallet's existing QRose encoder and decodes real
browser canvases. It covers payment requests, text, rotation/scaling, blank/empty images, shared
async initialization, per-frame allocation cleanup, and stopping before initialization completes.
Run `./gradlew :composeApp:wasmJsBrowserTest` and `./gradlew :composeApp:wasmJsBrowserDistribution`.
