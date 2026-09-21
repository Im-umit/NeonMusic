#!/bin/bash
mkdir -p .github/workflows
cat << 'WORKFLOW' > .github/workflows/build.yml
name: Build Android APK

on:
  push:
    branches: [ "main" ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'

      - name: Make gradlew executable
        run: chmod +x gradlew

      - name: Build Debug APK
        run: ./gradlew assembleDebug

      - name: Upload APK Artifact
        uses: actions/upload-artifact@v4
        with:
          name: neonmusic-debug-apk
          path: app/build/outputs/apk/debug/app-debug.apk
WORKFLOW

git add .github/workflows/build.yml
git commit -m "GitHub Actions workflow eklendi"
git push origin main
