---
name: Android import environment
description: Environment constraints observed when setting up imported native Android projects.
---

Imported Android projects may have a Java runtime available while the Android SDK is absent, so Gradle can resolve plugins and dependencies but still stop before Android compilation.

**Why:** The Replit container used for this project exposed Java after module setup but did not expose `ANDROID_HOME`, `ANDROID_SDK_ROOT`, or an Android SDK directory.

**How to apply:** Check for an Android SDK before claiming a native build is verified. If none exists, report the exact SDK-location blocker rather than adding a guessed `local.properties` path.