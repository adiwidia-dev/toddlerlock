# Release Guide

This project publishes release APKs manually through the GitHub Releases web UI.

## Version

Current release target:

- Tag: `v1.0.0`
- App version name: `1.0.0`
- App version code: `1`

## Pre-release Checks

Run these before tagging:

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

Run a local secret scan:

```bash
rg -n --hidden --glob '!/.git/**' --glob '!**/build/**' --glob '!gradle/wrapper/gradle-wrapper.jar' "(?i)(api[_-]?key|secret|password|passwd|token|private[_-]?key|client[_-]?secret|BEGIN (RSA|OPENSSH|DSA|EC|PRIVATE) KEY|AIza|ghp_|github_pat_|sk-)" .
git grep -n -I -E "(api[_-]?key|secret|password|passwd|token|private[_-]?key|client[_-]?secret|AIza|ghp_|github_pat_|sk-|BEGIN (RSA|OPENSSH|DSA|EC|PRIVATE) KEY)" "$(git rev-list --all)"
```

Expected acceptable findings are placeholders only, not real secrets.

## Release Signing

Keep the release keystore outside the repository.

Example keystore creation:

```bash
keytool -genkeypair -v \
  -keystore ~/private/toddlerlock-upload.jks \
  -alias upload \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

Export signing values for the current shell:

```bash
export KEYSTORE_PATH=~/private/toddlerlock-upload.jks
export STORE_PASSWORD='your-store-password'
export KEY_PASSWORD='your-key-password'
```

Build the release APK:

```bash
./gradlew assembleRelease
```

Expected output:

```text
app/build/outputs/apk/release/app-release.apk
```

## Manual GitHub Release

1. Commit release-ready changes.
2. Create and push the tag:

```bash
git tag -a v1.0.0 -m "ToddlerLock v1.0.0"
git push origin main
git push origin v1.0.0
```

3. Open GitHub Releases in the browser.
4. Create release `v1.0.0`.
5. Upload `app/build/outputs/apk/release/app-release.apk`.
6. Use `CHANGELOG.md` as the source for release notes.
