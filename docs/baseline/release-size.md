# Release artifact sizes

From the last self-committed `BUILD_REPORT.md` (pre-Phase-0, blanket R8 keep):

- `app/build/outputs/apk/debug/app-debug.apk` — 19.5 MiB
- `app/build/outputs/apk/release/app-release.apk` — 1.7 MiB
- `app/build/outputs/bundle/release/app-release.aab` — 4.3 MiB

Phase 0 replaced the blanket `-keep class com.monaart.engine.** { *; }` with a
targeted enum rule. **Post-change release size was not re-measured** because
`:app:assembleRelease` / `:app:bundleRelease` require the Android SDK and a
working Gradle distribution, neither of which is available in this sandbox.

CI now runs both release-assembly commands and publishes their sizes in the
build report, so the next full-environment run will supply the missing number.
