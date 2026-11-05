# Build report

**Commit under test:** `586e2b0a3d53f183a53a66a7fed80e085966338e`
**CI run:** <https://github.com/AmiraliGhamkhar/Mona_Art/actions/runs/42099924922>
**Branch:** `arena/43bc835d-mona-art`
**Runner:** GitHub-hosted `ubuntu-latest`, JDK 17 (temurin), Gradle 9.8.2, AGP 9.4.1
**Report generated (UTC):** 2026-11-05T20:09:55Z
**Job conclusion:** success — every step returned exit code 0

This file is a transcription of the report `scripts/make_report.py` generated
during that run. CI does not self-commit it (`android.yml` treats it as a job
summary plus a build artifact), so the checked-in copy is written by hand from
the run's own output, which is published to the branch diagnostic issue so the
numbers can be re-read without access to the runner logs.

## Step status

| Step | Command | Exit | Result |
|---|---|---|---|
| engine_selfcheck | `:engine:selfCheck` | 0 | PASS |
| engine_unit_tests | `:engine:test` | 0 | PASS |
| app_unit_tests | `:app:testDebugUnitTest` | 0 | PASS |
| assemble | `:app:assembleDebug` | 0 | PASS |
| release | `:app:assembleRelease :app:bundleRelease` | 0 | PASS |
| lint | `:app:lintDebug` | 0 | PASS |
| quality | `detekt ktlintCheck` | 0 | PASS |
| android_test_compile | `:app:assembleDebugAndroidTest` | 0 | PASS |
| kover | `:engine:koverXmlReport` | 0 | PASS |

## Tests executed

| Suite | Result |
|---|---|
| Engine self-check (`:engine:selfCheck`) | **27/27 checks passed** |
| Engine unit tests (`:engine:test`) | **69 tests, 0 failures, 0 errors, 0 skipped** |
| App unit tests (`:app:testDebugUnitTest`) | **13 tests, 0 failures, 0 errors, 0 skipped** |
| Android instrumentation (`:app:connectedDebugAndroidTest`) | **not executed** — no emulator or device in CI |

`android_test_compile` proves the instrumentation sources (`EditorSmokeTest`,
`StudioLaunchTest`) compile; it does not run them. Nothing in this report is
evidence that the Compose UI works on a device.

### Engine self-check detail (as reported)

| Check | Result | Detail |
|---|---|---|
| blend.normal_passthrough | PASS | (6 ms) |
| blend.multiply_reference | PASS | (0 ms) |
| blend.all_modes_defined | PASS | 17 modes |
| blend.composite_alpha_math | PASS | (0 ms) |
| brush.pressure_affects_geometry | PASS | radius 1.51 (30 %) vs 4.50 (100 %) |
| brush.spacing_emits_dabs | PASS | 396 dabs over 200 px |
| brush.stroke_paints_pixels | PASS | alpha at centre 255 |
| brush.eraser_removes_alpha | PASS | alpha after erase 0 |
| brush.all_presets_render | PASS | 30 presets |
| brush.sparse_samples_have_no_gaps | PASS | 907 dabs, max gap 0.50 px |
| history.undo_redo_restores_pixels | PASS | |
| history.layer_ops_undoable | PASS | |
| io.project_roundtrip | PASS | format v3, 36 KiB |
| io.corruption_detected | PASS | |
| io.png_roundtrip | PASS | 287 B |
| io.all_export_formats_produce_bytes | PASS | MONA 2419, PNG 467, JPEG 1461, TIFF 120154, PSD 210156, PDF 926 |
| io.psd_has_layers | PASS | 2 layers, composite at 33030 |
| compositor.layer_order | PASS | |
| compositor.opacity_and_blend | PASS | |
| compositor.dirty_region_only | PASS | 0.00 ms per 256 px region on 2048² |
| filters.all_apply | PASS | 16 filters |
| selection.rectangle_limits_fill | PASS | |
| selection.magic_wand | PASS | bounds RectI(5,5,15,15) |
| viewport.roundtrip_and_zoom | PASS | |
| perf.stroke_throughput | PASS | 0.18 ms per stylus event (5470 events/s) |
| perf.large_canvas_memory | PASS | 4096² stroke in 120 ms, 2816 KiB of tiles |
| perf.save_large_project | PASS | 2048² save 153 ms, load 57 ms |

These are **JVM** numbers measured on a CI runner. They are not device
measurements and must not be read as phone frame rates.

## Artifacts

| Artifact | Size | SHA-256 |
|---|---|---|
| `app/build/outputs/apk/debug/app-debug.apk` | 20,505,101 B (19.6 MiB) | `df3809da4257aee514df2467a8c3c298cb590e6bda7150ee59f790c7fa5f6c68` |
| `app/build/outputs/apk/release/app-release.apk` | 1,758,484 B (1.7 MiB) | `ba5234edc25d2546de6b69712ae9a4c4fc1168070c8396ae9c1040dc061e76fb` |
| `app/build/outputs/bundle/release/app-release.aab` | 4,453,864 B (4.2 MiB) | `b1dd2cc01761768b3a232eadc43bb540c210823e682e10134e41a32bb44cca0d` |

Uploaded as the `mona-art-build` artifact of the run above.

## Signing status

**The release APK and AAB are debug-signed and are not publishable.**
`app/build.gradle.kts` falls back to the debug signing config unless
`MONA_KEYSTORE_PATH`, `MONA_KEYSTORE_PASSWORD`, `MONA_KEY_ALIAS` and
`MONA_KEY_PASSWORD` are all set; none were provided to this run.
`.github/workflows/publish-apk.yml` publishes that debug APK and marks it as not
store-ready.

## Known gaps in this report

- **Kover coverage is meaningless as configured.** `:engine:koverXmlReport`
  produced `1/1 lines (100.0%)` — one line, not the engine. The task exits 0 and
  the number should be ignored until the Kover instrumentation is pointed at the
  engine source set.
- **Gradle deprecation warnings remain.** Every step still prints "Deprecated
  Gradle features were used in this build, which are incompatible with Gradle 10."
  They originate in the Gradle plugins (AGP 9.4.1, the ktlint and detekt Gradle
  plugins), not in this project's build scripts, so they cannot be cleared without
  upstream releases. `--warning-mode all` was not run to enumerate them.
- No device, emulator or physical stylus was available, so no UI, touch-latency
  or stylus behaviour is verified.
