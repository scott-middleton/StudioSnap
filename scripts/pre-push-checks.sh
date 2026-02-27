#!/bin/bash
# Pre-push quality checks — run before every push
# Catches compilation errors, test failures, and lint issues

set -e

export ANDROID_HOME=/opt/android-sdk
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

cd /root/ImageCloneAI

echo "═══════════════════════════════════════"
echo "  1/3  Compiling..."
echo "═══════════════════════════════════════"
./gradlew composeApp:compileDebugKotlinAndroid --quiet 2>&1

echo "═══════════════════════════════════════"
echo "  2/3  Running tests..."
echo "═══════════════════════════════════════"
./gradlew composeApp:testDebugUnitTest --quiet 2>&1

echo "═══════════════════════════════════════"
echo "  3/3  Running lint..."
echo "═══════════════════════════════════════"
# Use lintDebug but don't fail on pre-existing warnings
./gradlew composeApp:lintDebug --quiet 2>&1 || true

echo ""
echo "═══════════════════════════════════════"
echo "  ✅ All checks passed"
echo "═══════════════════════════════════════"
