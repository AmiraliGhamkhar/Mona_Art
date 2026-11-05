# Build report

Generated: 2026-11-05T08:17:46.228974+00:00

Commit: `6dc5402bd881a985c2780aa438ca70729aea72aa`

## Step status

| Step | Exit code | Result |
|---|---|---|
| engine_selfcheck | 0 | PASS |
| engine_unit_tests | 1 | FAIL |
| app_unit_tests | 1 | FAIL |
| assemble_debug | 0 | PASS |
| assemble_release | 0 | PASS |
| lint | 0 | PASS |

## Engine self-check

# Engine self-check report

Generated: 2026-11-05T08:08:43.367601488Z

| Check | Result | Detail |
|---|---|---|
| blend.normal_passthrough | PASS | kotlin.Unit (6ms) |
| blend.multiply_reference | PASS | kotlin.Unit (0ms) |
| blend.all_modes_defined | PASS | 17 modes (11ms) |
| blend.composite_alpha_math | PASS | kotlin.Unit (0ms) |
| brush.pressure_affects_geometry | PASS | radius 1.51 (30%%) vs 4.50 (100%%) (25ms) |
| brush.spacing_emits_dabs | PASS | 396 dabs over 200px (4ms) |
| brush.stroke_paints_pixels | PASS | alpha at centre 255 (25ms) |
| brush.eraser_removes_alpha | PASS | alpha after erase 0 (18ms) |
| brush.all_presets_render | PASS | 30 presets (443ms) |
| brush.sparse_samples_have_no_gaps | PASS | 907 dabs, max gap 0.50px, 4px tall stroke (21ms) |
| history.undo_redo_restores_pixels | PASS | undo/redo ok (0ms) |
| history.layer_ops_undoable | PASS | ok (11ms) |
| io.project_roundtrip | PASS | format v3, 35 KiB (108ms) |
| io.corruption_detected | PASS | detected (1ms) |
| io.png_roundtrip | PASS | png 287B (6ms) |
| io.all_export_formats_produce_bytes | PASS | MONA=2419 PNG=467 JPEG=1461 TIFF=120154 PSD=210164 PDF=926 (204ms) |
| io.psd_has_layers | PASS | 2 layers (1ms) |
| compositor.layer_order | PASS | order ok (1ms) |
| compositor.opacity_and_blend | PASS | ok (0ms) |
| compositor.dirty_region_only | PASS | 0.01 ms per 256px region on 2048² canvas (4ms) |
| filters.all_apply | PASS | 16 filters (46ms) |
| selection.rectangle_limits_fill | PASS | ok (0ms) |
| selection.magic_wand | PASS | bounds RectI(left=5, top=5, right=15, bottom=15) (5ms) |
| viewport.roundtrip_and_zoom | PASS | ok (0ms) |
| perf.stroke_throughput | PASS | 0.85 ms per stylus event (1171 events/s) (512ms) |
| perf.large_canvas_memory | PASS | stroke on 4096² in 127 ms, 2816 KiB of tiles used (126ms) |
| perf.save_large_project | PASS | 2048² save 63 ms (0.1 MiB), load 32 ms (103ms) |

**27/27 checks passed.**

<!-- 3 result files -->
## Engine unit tests

0 tests, 0 failures, 0 errors, 0 skipped

Failing: com.monaart.engine.brush.BrushEngineTest.sparse samples are interpolated without gaps — java.lang.AssertionError: stroke must not contain gaps expected:<0> but was:<35>

<!-- 2 result files -->
## App unit tests

0 tests, 0 failures, 0 errors, 0 skipped

Failing: com.monaart.studio.presentation.editor.PerformanceTest.stroke throughput stays within budget — java.lang.AssertionError: a pointer sample must cost well under a 16 ms frame (measured 33.71 ms)

## Artifacts

- `app/build/outputs/apk/debug/app-debug.apk` — 19.5 MiB
- `app/build/outputs/apk/release/app-release.apk` — 1.7 MiB
- `app/build/outputs/bundle/release/app-release.aab` — 4.2 MiB

## engine tests: failure detail

```
* What went wrong:
Execution failed for task ':engine:test'.
> There were failing tests. See the report at: file:///home/runner/work/Mona_Art/Mona_Art/engine/build/reports/tests/test/index.html
* Try:
> Run with --scan to get full insights from a Build Scan (powered by Develocity).
Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.
You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.
For more on this, please refer to https://docs.gradle.org/9.8.2/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.
BUILD FAILED in 22s
4 actionable tasks: 3 executed, 1 up-to-date
Configuration cache entry stored.
```

## app tests: failure detail

```
* What went wrong:
Execution failed for task ':app:testDebugUnitTest'.
> There were failing tests. See the report at: file:///home/runner/work/Mona_Art/Mona_Art/app/build/reports/tests/testDebugUnitTest/index.html
* Try:
> Run with --scan to get full insights from a Build Scan (powered by Develocity).
Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.
You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.
For more on this, please refer to https://docs.gradle.org/9.8.2/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.
BUILD FAILED in 2m 40s
33 actionable tasks: 30 executed, 1 from cache, 2 up-to-date
Configuration cache entry stored.
```

## assembleDebug: log tail

```
> Task :app:processDebugMainManifest UP-TO-DATE
> Task :app:processDebugManifest UP-TO-DATE
> Task :app:processDebugManifestForPackage UP-TO-DATE
> Task :app:processDebugResources UP-TO-DATE
> Task :app:mergeDebugJniLibFolders
> Task :app:mergeDebugNativeLibs
> Task :app:mergeLibDexDebug
> Task :app:validateSigningDebug
> Task :app:writeDebugAppMetadata
> Task :app:writeDebugSigningConfigVersions
> Task :app:stripDebugDebugSymbols
Unable to strip the following libraries, packaging them as they are: libandroidx.graphics.path.so, libdatastore_shared_counter.so. Run with --info option to learn more.
> Task :app:dexBuilderDebug
> Task :app:mergeExtDexDebug
> Task :app:mergeProjectDexDebug
> Task :app:packageDebug
> Task :app:assembleDebug
> Task :app:createDebugApkListingFileRedirect
[Incubating] Problems report is available at: file:///home/runner/work/Mona_Art/Mona_Art/build/reports/problems/problems-report.html
Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.
You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.
For more on this, please refer to https://docs.gradle.org/9.8.2/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.
BUILD SUCCESSFUL in 1m 52s
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
BUILD SUCCESSFUL in 2m 43s
63 actionable tasks: 56 executed, 5 from cache, 2 up-to-date
Configuration cache entry stored.
```
