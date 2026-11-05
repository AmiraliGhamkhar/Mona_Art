# Mona Art

A native Android painting and drawing studio: its own brush engine, own
compositor, own document format and own exporters. No third-party paint engine,
no network access, no telemetry.

Kotlin · Jetpack Compose · Material 3 Expressive · minSdk 26 · target/compileSdk 37

| | |
|---|---|
| Painting core | `:engine` — pure JVM, no Android dependencies, unit-tested on the JVM |
| App | `:app` — Compose UI, ViewModel + StateFlow, DataStore, JSON project index |
| Document format | `.mona` v3 — ZIP with a versioned JSON manifest, DEFLATE tiles, SHA-256, atomic save + `.bak` + recovery snapshot |
| Export | PNG, JPEG (own baseline encoder), TIFF, PSD (layered), PDF, MONA |

## Build

Requirements: JDK 17+, Android SDK with platform 37 (`compileSdk = 37`).

```bash
git clone <this repo> && cd Mona_Art
./gradlew :app:assembleDebug        # app/build/outputs/apk/debug/app-debug.apk
./gradlew :engine:selfCheck         # 27 engine self-checks on the JVM
./gradlew :engine:test              # brush / document / persistence suites
./gradlew :app:testDebugUnitTest    # stylus routing + performance guards
```

`gradle/libs.versions.toml` pins every version (AGP 9.4.1, Kotlin 2.4.21,
Compose BOM 2026.09.00, Material 3 1.4.0, Gradle 9.8.2). No dependency is
experimental.

### Install

Download the latest debug build:
<https://github.com/AmiraliGhamkhar/Mona_Art/releases/tag/apk-latest>
(`mona-art-debug.apk` + `SHA256SUMS.txt`, refreshed by CI on every green push), or
install the local build:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The app works entirely offline (no `INTERNET` permission), but the debug APK does declare `VIBRATE` and media-read permissions; see `app/src/main/AndroidManifest.xml`.

### Release

```bash
./gradlew :app:assembleRelease :app:bundleRelease
```

The release build is minified but only debug-signed unless you provide
`MONA_KEYSTORE_PATH`, `MONA_KEYSTORE_PASSWORD`, `MONA_KEY_ALIAS` and
`MONA_KEY_PASSWORD` (see `app/build.gradle.kts`).

## Continuous integration

`.github/workflows/android.yml` runs on every push: engine self-check, engine
unit tests, app unit tests, `assembleDebug`, `assembleRelease` + `bundleRelease`,
and lint. It commits `BUILD_REPORT.md` with the measured results and uploads the
APK/AAB as the `mona-art-build` artifact. The report is the source of truth for
"what actually passed" — it is produced by the build, not written by hand.

## Documentation

| File | Contents |
|---|---|
| [`IMPLEMENTATION_STATUS.md`](IMPLEMENTATION_STATUS.md) | What is done, what is partial, known limitations |
| [`FEATURE_MATRIX.md`](FEATURE_MATRIX.md) | Every requested feature: implemented / partial / planned |
| [`TECHNICAL_DECISIONS.md`](TECHNICAL_DECISIONS.md) | Architecture and the reasoning behind each choice |
| [`BUILD_REPORT.md`](BUILD_REPORT.md) | Latest CI step results, test counts, artifact sizes, self-check table |

## Design

Bento-based creative home, tablet-first with phone-adaptive layouts, one dark
palette plus light and AMOLED variants (lime/violet/cyan/pink accents), and a
shape/typography system defined once in `core/design`. Drawing quality is the
priority: the engine composites dirty regions only, the stroke rasteriser is
bounded per pointer sample, and every performance number in the docs is measured
by a test that fails if it regresses.
