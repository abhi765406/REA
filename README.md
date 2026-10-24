# REA Android Companion

Android companion inspired by [morluto/rea](https://github.com/morluto/rea), a Node.js CLI/MCP server for agent-assisted reverse engineering.

## Included
- Native Android app, minimum Android 8.1 (API 27)
- Local file picker
- On-device SHA-256 calculation and file size
- Printable ASCII string preview for selected files
- Overview and quick-start reference for upstream REA
- GitHub Actions workflow to build a debug APK

## Important scope note
This is **not a full port of the REA CLI/MCP server**. The upstream package depends on Node.js and host-side tools/integrations such as Ghidra, Hopper, IDA, JADX and OS-specific bridges. This companion does not bundle or emulate those desktop dependencies. File inspection is local; it does not upload selected files.

## Build
Open in Android Studio, or push to GitHub and run **Actions → Build REA Android APK**. The workflow uploads `app-debug.apk` as an artifact.

Toolchain: Java 17, Android Gradle Plugin 8.5.0, Gradle 8.7. `compileSdk` 35 warning is suppressed in `gradle.properties`.
