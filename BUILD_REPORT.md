# Build report

Generated: 2026-11-05T13:02:30.884842+00:00

Commit: `80841d76e3125648d6bcfd74728371c42e6e6f20`

## Step status

| Step | Exit code | Result |
|---|---|---|
| engine_selfcheck | 0 | PASS |
| engine_unit_tests | 0 | PASS |
| app_unit_tests | 0 | PASS |
| assemble_debug | 0 | PASS |
| assemble_release | 0 | PASS |
| lint | 0 | PASS |

## Step outcomes

| Step | Outcome |
|---|---|
| engine_selfcheck | success |
| engine_unit_tests | success |
| app_unit_tests | success |
| assemble_debug | success |
| assemble_release | success |
| lint | success |

## APK release publishing

```
83c8a97f1e71eee7b380da6dab03f4d3acfd2c2462ef2849835dcdf1455b759f  mona-art-debug.apk
ae3ec6fe3c1923521dfd9cd6c415a746323039c7fa461b4fbdaae9bed0b52b5e  mona-art-release-unsigned.apk
https://github.com/AmiraliGhamkhar/Mona_Art/releases/tag/apk-latest
```

## Engine self-check

# Engine self-check report

Generated: 2026-11-05T12:55:08.985540980Z

| Check | Result | Detail |
|---|---|---|
| blend.normal_passthrough | PASS | kotlin.Unit (5ms) |
| blend.multiply_reference | PASS | kotlin.Unit (0ms) |
| blend.all_modes_defined | PASS | 17 modes (9ms) |
| blend.composite_alpha_math | PASS | kotlin.Unit (0ms) |
| brush.pressure_affects_geometry | PASS | radius 1.51 (30%%) vs 4.50 (100%%) (20ms) |
| brush.spacing_emits_dabs | PASS | 396 dabs over 200px (3ms) |
| brush.stroke_paints_pixels | PASS | alpha at centre 255 (15ms) |
| brush.eraser_removes_alpha | PASS | alpha after erase 0 (11ms) |
| brush.all_presets_render | PASS | 30 presets (365ms) |
| brush.sparse_samples_have_no_gaps | PASS | 907 dabs, max gap 0.50px, 4px tall stroke (12ms) |
| history.undo_redo_restores_pixels | PASS | undo/redo ok (0ms) |
| history.layer_ops_undoable | PASS | ok (11ms) |
| io.project_roundtrip | PASS | format v3, 36 KiB (85ms) |
| io.corruption_detected | PASS | detected (1ms) |
| io.png_roundtrip | PASS | png 287B (4ms) |
| io.all_export_formats_produce_bytes | PASS | MONA=2419 PNG=467 JPEG=1461 TIFF=120154 PSD=210164 PDF=926 (173ms) |
| io.psd_has_layers | PASS | 2 layers (1ms) |
| compositor.layer_order | PASS | order ok (1ms) |
| compositor.opacity_and_blend | PASS | ok (0ms) |
| compositor.dirty_region_only | PASS | 0.00 ms per 256px region on 2048² canvas (3ms) |
| filters.all_apply | PASS | 16 filters (31ms) |
| selection.rectangle_limits_fill | PASS | ok (0ms) |
| selection.magic_wand | PASS | bounds RectI(left=5, top=5, right=15, bottom=15) (5ms) |
| viewport.roundtrip_and_zoom | PASS | ok (0ms) |
| perf.stroke_throughput | PASS | 0.23 ms per stylus event (4297 events/s) (139ms) |
| perf.large_canvas_memory | PASS | stroke on 4096² in 130 ms, 2816 KiB of tiles used (130ms) |
| perf.save_large_project | PASS | 2048² save 74 ms (0.1 MiB), load 54 ms (145ms) |

**27/27 checks passed.**

<!-- 4 result files -->
## Engine unit tests

48 tests, 0 failures, 0 errors, 0 skipped

<!-- 2 result files -->
## App unit tests

13 tests, 0 failures, 0 errors, 0 skipped

## Artifacts

- `app/build/outputs/apk/debug/app-debug.apk` — 19.5 MiB
- `app/build/outputs/apk/release/app-release.apk` — 1.7 MiB
- `app/build/outputs/bundle/release/app-release.aab` — 4.3 MiB

## assembleDebug: log tail

```
> Task :app:processDebugMainManifest UP-TO-DATE
> Task :app:processDebugManifest UP-TO-DATE
> Task :app:processDebugManifestForPackage UP-TO-DATE
> Task :app:processDebugResources UP-TO-DATE
> Task :app:mergeDebugJniLibFolders
> Task :app:mergeLibDexDebug
> Task :app:validateSigningDebug
> Task :app:writeDebugAppMetadata
> Task :app:writeDebugSigningConfigVersions
> Task :app:mergeDebugNativeLibs
> Task :app:mergeExtDexDebug
> Task :app:dexBuilderDebug
> Task :app:stripDebugDebugSymbols
Unable to strip the following libraries, packaging them as they are: libandroidx.graphics.path.so, libdatastore_shared_counter.so. Run with --info option to learn more.
> Task :app:mergeProjectDexDebug
> Task :app:packageDebug
> Task :app:assembleDebug
> Task :app:createDebugApkListingFileRedirect
[Incubating] Problems report is available at: file:///home/runner/work/Mona_Art/Mona_Art/build/reports/problems/problems-report.html
Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.
You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.
For more on this, please refer to https://docs.gradle.org/9.8.2/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.
BUILD SUCCESSFUL in 1m 38s
41 actionable tasks: 18 executed, 23 up-to-date
Configuration cache entry stored.
```

## release: log tail

```
> Task :app:lintVitalAnalyzeRelease
> Task :app:minifyReleaseWithR8
> Task :app:lintVitalReportRelease
> Task :app:lintVitalRelease
> Task :app:convertShrunkResourcesToBinaryRelease
> Task :app:optimizeReleaseResources
> Task :app:l8DexDesugarLibRelease
> Task :app:buildReleasePreBundle
> Task :app:mergeReleaseComposeMapping
> Task :app:compileReleaseArtProfile
> Task :app:packageRelease
> Task :app:assembleRelease
> Task :app:createReleaseApkListingFileRedirect
> Task :app:packageReleaseBundle
> Task :app:signReleaseBundle
> Task :app:bundleRelease
> Task :app:produceReleaseBundleIdeListingFile
> Task :app:createReleaseBundleListingFileRedirect
[Incubating] Problems report is available at: file:///home/runner/work/Mona_Art/Mona_Art/build/reports/problems/problems-report.html
Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.
You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.
For more on this, please refer to https://docs.gradle.org/9.8.2/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.
BUILD SUCCESSFUL in 2m 22s
63 actionable tasks: 56 executed, 5 from cache, 2 up-to-date
Configuration cache entry stored.
```
