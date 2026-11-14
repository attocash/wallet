#!/usr/bin/env bash
set -euo pipefail

# Build the original project's Wasm wrapper, not a third-party npm distribution.
ZXING_REVISION=287c85df6f961c8efbfb5ffd736cd9457b8b890e
STB_REVISION=9600522de53973336fa033a289858a4354cbb04c
EMSCRIPTEN_VERSION=4.0.23
CMAKE_VERSION=4.1.2
NINJA_VERSION=1.13.1

repo_dir=$(cd "$(dirname "$0")/.." && pwd)
build_dir=$(mktemp -d)
trap 'rm -rf "$build_dir"' EXIT

emcc --version | head -n 1 | grep -F " $EMSCRIPTEN_VERSION " >/dev/null
cmake --version | head -n 1 | grep -Fx "cmake version $CMAKE_VERSION" >/dev/null
[ "$(ninja --version)" = "$NINJA_VERSION" ]

fetch_revision() {
  local repository=$1 revision=$2 destination=$3
  git init --quiet "$destination"
  git -C "$destination" remote add origin "$repository"
  git -C "$destination" fetch --quiet --depth 1 origin "$revision"
  git -C "$destination" checkout --quiet --detach FETCH_HEAD
  [ "$(git -C "$destination" rev-parse HEAD)" = "$revision" ]
}

fetch_revision https://github.com/zxing-cpp/zxing-cpp.git "$ZXING_REVISION" "$build_dir/zxing-cpp"
fetch_revision https://github.com/nothings/stb.git "$STB_REVISION" "$build_dir/stb"
emcmake cmake -S "$build_dir/zxing-cpp/wrappers/wasm" -B "$build_dir/output" -G Ninja \
  -DZXING_WRITERS=OFF -DZXING_READERS=ON -DZXING_EMSCRIPTEN_ENVIRONMENT=web -DZXING_DEPENDENCIES=GITHUB \
  -DFETCHCONTENT_SOURCE_DIR_STB="$build_dir/stb" -DCMAKE_BUILD_TYPE=Release \
  -DCMAKE_CXX_FLAGS='-O3 -flto' -DCMAKE_EXE_LINKER_FLAGS='-O3 -flto -sEXPORT_ES6=1'
cmake --build "$build_dir/output" --target zxing_reader --parallel 2
cp "$build_dir/output/zxing_reader.js" "$build_dir/output/zxing_reader.wasm" "$repo_dir/composeApp/zxing-cpp-qr/vendor/"
chmod 644 "$repo_dir/composeApp/zxing-cpp-qr/vendor/zxing_reader.js" "$repo_dir/composeApp/zxing-cpp-qr/vendor/zxing_reader.wasm"
(cd "$repo_dir/composeApp/zxing-cpp-qr/vendor" && sha256sum zxing_reader.js zxing_reader.wasm)
