# Build report

Generated: 2026-11-04T23:56:31.210710+00:00

Commit: `44d3f25f32a18929c02c096f0057b4bb57ab42b7`

## Step status

| Step | Exit code | Result |
|---|---|---|
| engine_selfcheck | 0 | PASS |
| engine_unit_tests | 0 | PASS |
| app_unit_tests | 1 | FAIL |
| assemble_debug | 1 | FAIL |
| assemble_release | 1 | FAIL |
| lint | 1 | FAIL |

## Engine self-check

# Engine self-check report

Generated: 2026-11-04T23:52:03.129217001Z

| Check | Result | Detail |
|---|---|---|
| blend.normal_passthrough | PASS | kotlin.Unit (7ms) |
| blend.multiply_reference | PASS | kotlin.Unit (0ms) |
| blend.all_modes_defined | PASS | 17 modes (11ms) |
| blend.composite_alpha_math | PASS | kotlin.Unit (0ms) |
| brush.pressure_affects_geometry | PASS | radius 1.51 (30%%) vs 4.50 (100%%) (25ms) |
| brush.spacing_emits_dabs | PASS | 396 dabs over 200px (3ms) |
| brush.stroke_paints_pixels | PASS | alpha at centre 255 (27ms) |
| brush.eraser_removes_alpha | PASS | alpha after erase 0 (15ms) |
| brush.all_presets_render | PASS | 30 presets (398ms) |
| brush.sparse_samples_have_no_gaps | PASS | 907 dabs, max gap 0.50px, 4px tall stroke (21ms) |
| history.undo_redo_restores_pixels | PASS | undo/redo ok (3ms) |
| history.layer_ops_undoable | PASS | ok (11ms) |
| io.project_roundtrip | PASS | format v3, 36 KiB (108ms) |
| io.corruption_detected | PASS | detected (2ms) |
| io.png_roundtrip | PASS | png 287B (6ms) |
| io.all_export_formats_produce_bytes | PASS | MONA=2419 PNG=467 JPEG=2349 TIFF=120154 PSD=210164 PDF=926 (213ms) |
| io.psd_has_layers | PASS | 2 layers (3ms) |
| compositor.layer_order | PASS | order ok (2ms) |
| compositor.opacity_and_blend | PASS | ok (0ms) |
| compositor.dirty_region_only | PASS | 0.01 ms per 256px region on 2048² canvas (4ms) |
| filters.all_apply | PASS | 16 filters (45ms) |
| selection.rectangle_limits_fill | PASS | ok (0ms) |
| selection.magic_wand | PASS | bounds RectI(left=5, top=5, right=15, bottom=15) (5ms) |
| viewport.roundtrip_and_zoom | PASS | ok (0ms) |
| perf.stroke_throughput | PASS | 0.49 ms per stylus event (2029 events/s) (296ms) |
| perf.large_canvas_memory | PASS | stroke on 4096² in 243 ms, 2816 KiB of tiles used (243ms) |
| perf.save_large_project | PASS | 2048² save 65 ms (0.1 MiB), load 39 ms (112ms) |

**27/27 checks passed.**

## Artifacts


## assembleDebug: compiler errors (14)

| File | Errors |
|---|---|
| `Panels` | 10 |
| `BrushStudioScreen` | 4 |

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:275:17 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:276:55 Cannot infer type for type parameter 'R'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:276:90 Unresolved reference 'asImageBitmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:287:26 Argument type mismatch: actual type is 'Any', but 'ImageBitmap' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:621:40 Unresolved reference 'edit'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:621:47 Unresolved reference 'it'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:621:50 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:621:50 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:621:50 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:40 Unresolved reference 'edit'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:47 Unresolved reference 'it'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:50 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:50 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:50 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
```

## release: compiler errors (14)

| File | Errors |
|---|---|
| `Panels` | 10 |
| `BrushStudioScreen` | 4 |

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:275:17 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:276:55 Cannot infer type for type parameter 'R'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:276:90 Unresolved reference 'asImageBitmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:287:26 Argument type mismatch: actual type is 'Any', but 'ImageBitmap' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:621:40 Unresolved reference 'edit'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:621:47 Unresolved reference 'it'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:621:50 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:621:50 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:621:50 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:40 Unresolved reference 'edit'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:47 Unresolved reference 'it'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:50 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:50 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:50 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
```

## app tests: compiler errors (14)

| File | Errors |
|---|---|
| `Panels` | 10 |
| `BrushStudioScreen` | 4 |

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:275:17 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:276:55 Cannot infer type for type parameter 'R'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:276:90 Unresolved reference 'asImageBitmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:287:26 Argument type mismatch: actual type is 'Any', but 'ImageBitmap' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:621:40 Unresolved reference 'edit'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:621:47 Unresolved reference 'it'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:621:50 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:621:50 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:621:50 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:40 Unresolved reference 'edit'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:47 Unresolved reference 'it'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:50 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:50 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:50 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
```

## app tests: failure detail

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:275:17 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:276:55 Cannot infer type for type parameter 'R'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:276:90 Unresolved reference 'asImageBitmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:287:26 Argument type mismatch: actual type is 'Any', but 'ImageBitmap' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:621:40 Unresolved reference 'edit'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:621:47 Unresolved reference 'it'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:621:50 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:621:50 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:621:50 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:40 Unresolved reference 'edit'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:47 Unresolved reference 'it'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:50 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:50 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:50 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
> Task :app:compileDebugKotlin FAILED
[Incubating] Problems report is available at: file:///home/runner/work/Mona_Art/Mona_Art/build/reports/problems/problems-report.html
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':app:compileDebugKotlin' (registered by plugin 'com.android.internal.application').
> A failure occurred while executing org.jetbrains.kotlin.compilerRunner.btapi.BuildToolsApiCompilationWork
   > Kotlin compiler: CANNOT_INFER_PARAMETER_TYPE
       Kotlin compiler error
         Cannot infer type for type parameter 'T'. Specify it explicitly.
         Location: /home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:275
* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights from a Build Scan (powered by Develocity).
> Get more help at https://help.gradle.org.
Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.
You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.
For more on this, please refer to https://docs.gradle.org/9.8.2/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.
```

## assembleDebug: failure detail

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:275:17 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:276:55 Cannot infer type for type parameter 'R'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:276:90 Unresolved reference 'asImageBitmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:287:26 Argument type mismatch: actual type is 'Any', but 'ImageBitmap' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:621:40 Unresolved reference 'edit'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:621:47 Unresolved reference 'it'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:621:50 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:621:50 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:621:50 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:40 Unresolved reference 'edit'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:47 Unresolved reference 'it'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:50 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:50 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:50 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
[Incubating] Problems report is available at: file:///home/runner/work/Mona_Art/Mona_Art/build/reports/problems/problems-report.html
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':app:compileDebugKotlin' (registered by plugin 'com.android.internal.application').
> A failure occurred while executing org.jetbrains.kotlin.compilerRunner.btapi.BuildToolsApiCompilationWork
   > Kotlin compiler: CANNOT_INFER_PARAMETER_TYPE
       Kotlin compiler error
         Cannot infer type for type parameter 'T'. Specify it explicitly.
         Location: /home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:275
* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights from a Build Scan (powered by Develocity).
> Get more help at https://help.gradle.org.
Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.
You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.
For more on this, please refer to https://docs.gradle.org/9.8.2/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.
BUILD FAILED in 2m 6s
```

## release: failure detail

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:275:17 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:276:55 Cannot infer type for type parameter 'R'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:276:90 Unresolved reference 'asImageBitmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:287:26 Argument type mismatch: actual type is 'Any', but 'ImageBitmap' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:621:40 Unresolved reference 'edit'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:621:47 Unresolved reference 'it'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:621:50 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:621:50 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:621:50 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:40 Unresolved reference 'edit'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:47 Unresolved reference 'it'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:50 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:50 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:50 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
> Task :app:compileReleaseKotlin FAILED
[Incubating] Problems report is available at: file:///home/runner/work/Mona_Art/Mona_Art/build/reports/problems/problems-report.html
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':app:compileReleaseKotlin' (registered by plugin 'com.android.internal.application').
> A failure occurred while executing org.jetbrains.kotlin.compilerRunner.btapi.BuildToolsApiCompilationWork
   > Kotlin compiler: CANNOT_INFER_PARAMETER_TYPE
       Kotlin compiler error
         Cannot infer type for type parameter 'T'. Specify it explicitly.
         Location: /home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:275
* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights from a Build Scan (powered by Develocity).
> Get more help at https://help.gradle.org.
Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.
You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.
For more on this, please refer to https://docs.gradle.org/9.8.2/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.
```

## assembleDebug: log tail

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:40 Unresolved reference 'edit'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:47 Unresolved reference 'it'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:50 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:50 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:50 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
[Incubating] Problems report is available at: file:///home/runner/work/Mona_Art/Mona_Art/build/reports/problems/problems-report.html
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':app:compileDebugKotlin' (registered by plugin 'com.android.internal.application').
> A failure occurred while executing org.jetbrains.kotlin.compilerRunner.btapi.BuildToolsApiCompilationWork
   > Kotlin compiler: CANNOT_INFER_PARAMETER_TYPE
       Kotlin compiler error
         Cannot infer type for type parameter 'T'. Specify it explicitly.
         Location: /home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:275
* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights from a Build Scan (powered by Develocity).
> Get more help at https://help.gradle.org.
Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.
You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.
For more on this, please refer to https://docs.gradle.org/9.8.2/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.
BUILD FAILED in 2m 6s
30 actionable tasks: 10 executed, 20 up-to-date
Configuration cache entry stored.
```

## release: log tail

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:47 Unresolved reference 'it'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:50 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:50 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:629:50 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
> Task :app:compileReleaseKotlin FAILED
[Incubating] Problems report is available at: file:///home/runner/work/Mona_Art/Mona_Art/build/reports/problems/problems-report.html
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':app:compileReleaseKotlin' (registered by plugin 'com.android.internal.application').
> A failure occurred while executing org.jetbrains.kotlin.compilerRunner.btapi.BuildToolsApiCompilationWork
   > Kotlin compiler: CANNOT_INFER_PARAMETER_TYPE
       Kotlin compiler error
         Cannot infer type for type parameter 'T'. Specify it explicitly.
         Location: /home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:275
* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights from a Build Scan (powered by Develocity).
> Get more help at https://help.gradle.org.
Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.
You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.
For more on this, please refer to https://docs.gradle.org/9.8.2/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.
BUILD FAILED in 36s
39 actionable tasks: 32 executed, 5 from cache, 2 up-to-date
Configuration cache entry stored.
```
