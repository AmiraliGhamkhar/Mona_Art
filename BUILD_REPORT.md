# Build report

Generated: 2026-11-05T11:18:23.629055+00:00

Commit: `74d84108ab75b748e46eae29bbd8033cd1f64a4f`

## Step status

| Step | Exit code | Result |
|---|---|---|
| engine_selfcheck | 0 | PASS |
| engine_unit_tests | 0 | PASS |
| app_unit_tests | 1 | FAIL |
| assemble_debug | 1 | FAIL |
| assemble_release | 1 | FAIL |
| lint | 1 | FAIL |

## Step outcomes

| Step | Outcome |
|---|---|
| engine_selfcheck | success |
| engine_unit_tests | success |
| app_unit_tests | success |
| assemble_debug | success |
| assemble_release | success |
| lint | success |

## Build errors

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:8:33 Unresolved reference 'MutableStateFlow'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:25:31 Unresolved reference 'MutableStateFlow'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:65:18 Unresolved reference 'MutableStateFlow'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:69:18 Property delegate must have a 'getValue(Nothing?, KProperty0<??? (Unknown type for type parameter T)>)' method. None of the following functions is applicable:
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:69:21 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:69:33 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:72:43 Unresolved reference 'uri'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:72:53 Unresolved reference 'displayName'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:73:21 Unresolved reference 'value'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:8:33 Unresolved reference 'MutableStateFlow'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:25:31 Unresolved reference 'MutableStateFlow'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:65:18 Unresolved reference 'MutableStateFlow'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:69:18 Property delegate must have a 'getValue(Nothing?, KProperty0<??? (Unknown type for type parameter T)>)' method. None of the following functions is applicable:
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:69:21 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:69:33 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:72:43 Unresolved reference 'uri'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:72:53 Unresolved reference 'displayName'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:73:21 Unresolved reference 'value'.
```

## Engine self-check

# Engine self-check report

Generated: 2026-11-05T11:13:45.070069299Z

| Check | Result | Detail |
|---|---|---|
| blend.normal_passthrough | PASS | kotlin.Unit (6ms) |
| blend.multiply_reference | PASS | kotlin.Unit (0ms) |
| blend.all_modes_defined | PASS | 17 modes (10ms) |
| blend.composite_alpha_math | PASS | kotlin.Unit (0ms) |
| brush.pressure_affects_geometry | PASS | radius 1.51 (30%%) vs 4.50 (100%%) (22ms) |
| brush.spacing_emits_dabs | PASS | 396 dabs over 200px (4ms) |
| brush.stroke_paints_pixels | PASS | alpha at centre 255 (20ms) |
| brush.eraser_removes_alpha | PASS | alpha after erase 0 (16ms) |
| brush.all_presets_render | PASS | 30 presets (367ms) |
| brush.sparse_samples_have_no_gaps | PASS | 907 dabs, max gap 0.50px, 4px tall stroke (20ms) |
| history.undo_redo_restores_pixels | PASS | undo/redo ok (0ms) |
| history.layer_ops_undoable | PASS | ok (11ms) |
| io.project_roundtrip | PASS | format v3, 36 KiB (105ms) |
| io.corruption_detected | PASS | detected (2ms) |
| io.png_roundtrip | PASS | png 287B (5ms) |
| io.all_export_formats_produce_bytes | PASS | MONA=2419 PNG=467 JPEG=1461 TIFF=120154 PSD=210164 PDF=926 (201ms) |
| io.psd_has_layers | PASS | 2 layers (2ms) |
| compositor.layer_order | PASS | order ok (2ms) |
| compositor.opacity_and_blend | PASS | ok (0ms) |
| compositor.dirty_region_only | PASS | 0.00 ms per 256px region on 2048² canvas (3ms) |
| filters.all_apply | PASS | 16 filters (52ms) |
| selection.rectangle_limits_fill | PASS | ok (0ms) |
| selection.magic_wand | PASS | bounds RectI(left=5, top=5, right=15, bottom=15) (6ms) |
| viewport.roundtrip_and_zoom | PASS | ok (0ms) |
| perf.stroke_throughput | PASS | 0.20 ms per stylus event (5028 events/s) (119ms) |
| perf.large_canvas_memory | PASS | stroke on 4096² in 114 ms, 2816 KiB of tiles used (114ms) |
| perf.save_large_project | PASS | 2048² save 81 ms (0.1 MiB), load 67 ms (166ms) |

**27/27 checks passed.**

<!-- 4 result files -->
## Engine unit tests

48 tests, 0 failures, 0 errors, 0 skipped

## Artifacts


## assembleDebug: compiler errors (9)

| File | Errors |
|---|---|
| `MainActivity` | 9 |

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:25:31 Unresolved reference 'MutableStateFlow'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:65:18 Unresolved reference 'MutableStateFlow'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:69:18 Property delegate must have a 'getValue(Nothing?, KProperty0<??? (Unknown type for type parameter T)>)' method. None of the following functions is applicable:
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:69:21 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:69:33 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:72:43 Unresolved reference 'uri'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:72:53 Unresolved reference 'displayName'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:73:21 Unresolved reference 'value'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:8:33 Unresolved reference 'MutableStateFlow'.
```

## release: compiler errors (9)

| File | Errors |
|---|---|
| `MainActivity` | 9 |

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:25:31 Unresolved reference 'MutableStateFlow'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:65:18 Unresolved reference 'MutableStateFlow'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:69:18 Property delegate must have a 'getValue(Nothing?, KProperty0<??? (Unknown type for type parameter T)>)' method. None of the following functions is applicable:
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:69:21 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:69:33 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:72:43 Unresolved reference 'uri'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:72:53 Unresolved reference 'displayName'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:73:21 Unresolved reference 'value'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:8:33 Unresolved reference 'MutableStateFlow'.
```

## app tests: compiler errors (9)

| File | Errors |
|---|---|
| `MainActivity` | 9 |

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:25:31 Unresolved reference 'MutableStateFlow'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:65:18 Unresolved reference 'MutableStateFlow'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:69:18 Property delegate must have a 'getValue(Nothing?, KProperty0<??? (Unknown type for type parameter T)>)' method. None of the following functions is applicable:
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:69:21 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:69:33 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:72:43 Unresolved reference 'uri'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:72:53 Unresolved reference 'displayName'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:73:21 Unresolved reference 'value'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:8:33 Unresolved reference 'MutableStateFlow'.
```

## app tests: failure detail

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:8:33 Unresolved reference 'MutableStateFlow'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:25:31 Unresolved reference 'MutableStateFlow'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:65:18 Unresolved reference 'MutableStateFlow'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:69:18 Property delegate must have a 'getValue(Nothing?, KProperty0<??? (Unknown type for type parameter T)>)' method. None of the following functions is applicable:
fun <T> State<T>.getValue(thisObj: Any?, property: KProperty<*>): T
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:69:21 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:69:33 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:72:43 Unresolved reference 'uri'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:72:53 Unresolved reference 'displayName'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:73:21 Unresolved reference 'value'.
> Task :app:compileDebugKotlin FAILED
[Incubating] Problems report is available at: file:///home/runner/work/Mona_Art/Mona_Art/build/reports/problems/problems-report.html
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':app:compileDebugKotlin' (registered by plugin 'com.android.internal.application').
> A failure occurred while executing org.jetbrains.kotlin.compilerRunner.btapi.BuildToolsApiCompilationWork
   > Kotlin compiler: UNRESOLVED_IMPORT
       Kotlin compiler error
         Unresolved reference 'MutableStateFlow'.
         Location: /home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:8
* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights from a Build Scan (powered by Develocity).
> Get more help at https://help.gradle.org.
Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.
You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.
For more on this, please refer to https://docs.gradle.org/9.8.2/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.
BUILD FAILED in 53s
26 actionable tasks: 23 executed, 1 from cache, 2 up-to-date
Configuration cache entry stored.
```

## assembleDebug: failure detail

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:8:33 Unresolved reference 'MutableStateFlow'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:25:31 Unresolved reference 'MutableStateFlow'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:65:18 Unresolved reference 'MutableStateFlow'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:69:18 Property delegate must have a 'getValue(Nothing?, KProperty0<??? (Unknown type for type parameter T)>)' method. None of the following functions is applicable:
fun <T> State<T>.getValue(thisObj: Any?, property: KProperty<*>): T
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:69:21 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:69:33 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:72:43 Unresolved reference 'uri'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:72:53 Unresolved reference 'displayName'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:73:21 Unresolved reference 'value'.
[Incubating] Problems report is available at: file:///home/runner/work/Mona_Art/Mona_Art/build/reports/problems/problems-report.html
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':app:compileDebugKotlin' (registered by plugin 'com.android.internal.application').
> A failure occurred while executing org.jetbrains.kotlin.compilerRunner.btapi.BuildToolsApiCompilationWork
   > Kotlin compiler: UNRESOLVED_IMPORT
       Kotlin compiler error
         Unresolved reference 'MutableStateFlow'.
         Location: /home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:8
* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights from a Build Scan (powered by Develocity).
> Get more help at https://help.gradle.org.
Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.
You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.
For more on this, please refer to https://docs.gradle.org/9.8.2/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.
BUILD FAILED in 2m 9s
29 actionable tasks: 9 executed, 20 up-to-date
Configuration cache entry stored.
```

## release: failure detail

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:8:33 Unresolved reference 'MutableStateFlow'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:25:31 Unresolved reference 'MutableStateFlow'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:65:18 Unresolved reference 'MutableStateFlow'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:69:18 Property delegate must have a 'getValue(Nothing?, KProperty0<??? (Unknown type for type parameter T)>)' method. None of the following functions is applicable:
fun <T> State<T>.getValue(thisObj: Any?, property: KProperty<*>): T
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:69:21 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:69:33 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:72:43 Unresolved reference 'uri'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:72:53 Unresolved reference 'displayName'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:73:21 Unresolved reference 'value'.
[Incubating] Problems report is available at: file:///home/runner/work/Mona_Art/Mona_Art/build/reports/problems/problems-report.html
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':app:compileReleaseKotlin' (registered by plugin 'com.android.internal.application').
> A failure occurred while executing org.jetbrains.kotlin.compilerRunner.btapi.BuildToolsApiCompilationWork
   > Kotlin compiler: UNRESOLVED_IMPORT
       Kotlin compiler error
         Unresolved reference 'MutableStateFlow'.
         Location: /home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:8
* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights from a Build Scan (powered by Develocity).
> Get more help at https://help.gradle.org.
Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.
You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.
For more on this, please refer to https://docs.gradle.org/9.8.2/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.
BUILD FAILED in 37s
39 actionable tasks: 32 executed, 5 from cache, 2 up-to-date
Configuration cache entry stored.
```

## assembleDebug: log tail

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:69:21 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:69:33 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:72:43 Unresolved reference 'uri'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:72:53 Unresolved reference 'displayName'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:73:21 Unresolved reference 'value'.
[Incubating] Problems report is available at: file:///home/runner/work/Mona_Art/Mona_Art/build/reports/problems/problems-report.html
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':app:compileDebugKotlin' (registered by plugin 'com.android.internal.application').
> A failure occurred while executing org.jetbrains.kotlin.compilerRunner.btapi.BuildToolsApiCompilationWork
   > Kotlin compiler: UNRESOLVED_IMPORT
       Kotlin compiler error
         Unresolved reference 'MutableStateFlow'.
         Location: /home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:8
* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights from a Build Scan (powered by Develocity).
> Get more help at https://help.gradle.org.
Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.
You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.
For more on this, please refer to https://docs.gradle.org/9.8.2/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.
BUILD FAILED in 2m 9s
29 actionable tasks: 9 executed, 20 up-to-date
Configuration cache entry stored.
```

## release: log tail

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:69:21 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:69:33 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:72:43 Unresolved reference 'uri'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:72:53 Unresolved reference 'displayName'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:73:21 Unresolved reference 'value'.
[Incubating] Problems report is available at: file:///home/runner/work/Mona_Art/Mona_Art/build/reports/problems/problems-report.html
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':app:compileReleaseKotlin' (registered by plugin 'com.android.internal.application').
> A failure occurred while executing org.jetbrains.kotlin.compilerRunner.btapi.BuildToolsApiCompilationWork
   > Kotlin compiler: UNRESOLVED_IMPORT
       Kotlin compiler error
         Unresolved reference 'MutableStateFlow'.
         Location: /home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/app/MainActivity.kt:8
* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights from a Build Scan (powered by Develocity).
> Get more help at https://help.gradle.org.
Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.
You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.
For more on this, please refer to https://docs.gradle.org/9.8.2/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.
BUILD FAILED in 37s
39 actionable tasks: 32 executed, 5 from cache, 2 up-to-date
Configuration cache entry stored.
```
