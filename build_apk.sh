#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
GRADLE_VERSION=8.9
DIST_DIR="$PWD/.gradle-dist"
GRADLE="$DIST_DIR/gradle-$GRADLE_VERSION/bin/gradle"

if [[ ! -x "$GRADLE" ]]; then
  mkdir -p "$DIST_DIR"
  curl -L "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -o "$DIST_DIR/gradle.zip"
  unzip -oq "$DIST_DIR/gradle.zip" -d "$DIST_DIR"
  rm -f "$DIST_DIR/gradle.zip"
fi

if [[ -z "${ANDROID_HOME:-}" && -n "${ANDROID_SDK_ROOT:-}" ]]; then
  export ANDROID_HOME="$ANDROID_SDK_ROOT"
fi
if [[ -z "${ANDROID_HOME:-}" ]]; then
  echo "Set ANDROID_HOME to your Android SDK folder first."
  exit 1
fi

"$GRADLE" assembleDebug
echo "APK: $PWD/app/build/outputs/apk/debug/app-debug.apk"
