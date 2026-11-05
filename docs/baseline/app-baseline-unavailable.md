# App baseline unavailable in this sandbox

The requested Phase 0.1 command was attempted verbatim:

```bash
./gradlew --no-daemon --console=plain \
  :engine:test :engine:selfCheck :app:testDebugUnitTest \
  :app:assembleDebug :app:lintDebug
```

It failed before configuration:

```
Downloading https://services.gradle.org/distributions/gradle-9.8.2-bin.zip
Attempt 1/1 failed. Reason: Remote host terminated the handshake
javax.net.ssl.SSLHandshakeException: Remote host terminated the handshake
exit=1
```

Additionally, there is no Android SDK, no `ANDROID_HOME`/`ANDROID_SDK_ROOT`,
no emulator, and no downloaded AndroidX/Robolectric artifacts. Therefore:

- `:app:testDebugUnitTest` (13 existing Robolectric/JVM tests) — **not run**.
- `:app:assembleDebug` — **not run**.
- `:app:lintDebug` — **not run**.
- `detekt`, `ktlintCheck`, `kover*` — **not run**.
- `:app:assembleDebugAndroidTest` — **not run**.

No app-layer result is asserted here. The engine results in this directory are
from the direct-JVM path only.
