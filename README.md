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

Two workflows run on every push.

**`android`** — engine self-check, engine unit tests, app unit tests,
`assembleDebug`, `assembleRelease` + `bundleRelease`, Android Lint, and
`detekt` + `ktlintCheck`. Every step's exit code is recorded, so a step that is
skipped or silently green cannot be mistaken for a pass; the job fails if any
step is non-zero. Failures publish the step log to a per-ref diagnostic issue
(this repository's job-log API returns EOF, so the issue is the readable
channel), and Android Lint findings are dumped from the SARIF report with the
severity resolved from the rule's default level. `BUILD_REPORT.md` and the
APK/AAB are uploaded as the `mona-art-build` artifact and echoed to the job
summary — they are **not** committed, so the checked-in copy is a manual
transcription of one specific run and names the commit it came from.

**`format`** — a 4-pass `ktlintFormat` fixpoint with `--continue`, then
`detektBaseline`. The gate reports but does not discard the work: formatting is
committed under `if: always()` with a `[format]` marker so it cannot retrigger
itself. Trigger it by putting `[format]` in a commit message.

The report is the source of truth for "what actually passed" — it is produced by
the build, not written by hand.

## Documentation

| File | Contents |
|---|---|
| [`IMPLEMENTATION_STATUS.md`](IMPLEMENTATION_STATUS.md) | What is done, what is partial, known limitations |
| [`FEATURE_MATRIX.md`](FEATURE_MATRIX.md) | Every requested feature: implemented / partial / planned |
| [`TECHNICAL_DECISIONS.md`](TECHNICAL_DECISIONS.md) | Architecture and the reasoning behind each choice |
| [`BUILD_REPORT.md`](BUILD_REPORT.md) | Transcription of one CI run, naming the commit it built: step exit codes, test counts, artifact sizes, self-check table |
| [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | Module boundaries, data flow, the engine/app seam, threading and persistence model |
| [`docs/REFACTOR_LOG.md`](docs/REFACTOR_LOG.md) | What was changed and why, including decisions that were reversed |

## Design

Bento-based creative home, tablet-first with phone-adaptive layouts, one dark
palette plus light and AMOLED variants (lime/violet/cyan/pink accents), and a
shape/typography system defined once in `core/design`. Drawing quality is the
priority: the engine composites dirty regions only, the stroke rasteriser is
bounded per pointer sample, and every performance number in the docs is measured
by a test that fails if it regresses.
