# Changelog

All notable changes to ToddlerLock will be documented in this file.

## v1.0.0 - 2026-06-02

### Added

- Initial public release of ToddlerLock.
- Transparent full-screen touch-blocking overlay using Android application overlay windows.
- Accessibility service shortcut for holding Volume Up + Volume Down for 3 seconds.
- Shortcut arming toggle in the app, so volume-button locking is ignored until explicitly enabled.
- Permission setup screen for Display over other apps and Accessibility Service.
- Android restricted settings guidance for sideloaded installs.
- Compact active-lock overlay banner positioned near the top of the screen.

### Changed

- Renamed package/namespace to `com.toddlerlock.app`.
- Updated app version to `1.0.0`.
- Simplified the main UI into a focused utility screen.
- Removed unused generated dependencies and AI Studio secret plumbing.

### Notes

- Release APK signing requires a private keystore outside the repository.
- Android system navigation cannot be fully blocked by a normal app overlay.
