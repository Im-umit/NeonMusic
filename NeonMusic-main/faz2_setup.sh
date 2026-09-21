#!/usr/bin/env bash
set -euo pipefail
# NeonMusic project setup helper.
# The Android project itself is already structured by Gradle; use Android Studio's
# Gradle sync/build commands rather than executing source snippets as shell code.
./gradlew :app:assembleDebug
