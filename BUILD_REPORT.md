# Build report

Generated: 2026-11-04T23:44:04.903471+00:00

Commit: `9b862cf0dcf94aaabeb4bc449fc11f6ead56f6d3`

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

Generated: 2026-11-04T23:39:39.770829307Z

| Check | Result | Detail |
|---|---|---|
| blend.normal_passthrough | PASS | kotlin.Unit (6ms) |
| blend.multiply_reference | PASS | kotlin.Unit (0ms) |
| blend.all_modes_defined | PASS | 17 modes (12ms) |
| blend.composite_alpha_math | PASS | kotlin.Unit (0ms) |
| brush.pressure_affects_geometry | PASS | radius 1.51 (30%%) vs 4.50 (100%%) (26ms) |
| brush.spacing_emits_dabs | PASS | 396 dabs over 200px (5ms) |
| brush.stroke_paints_pixels | PASS | alpha at centre 255 (24ms) |
| brush.eraser_removes_alpha | PASS | alpha after erase 0 (15ms) |
| brush.all_presets_render | PASS | 30 presets (420ms) |
| brush.sparse_samples_have_no_gaps | PASS | 907 dabs, max gap 0.50px, 4px tall stroke (21ms) |
| history.undo_redo_restores_pixels | PASS | undo/redo ok (3ms) |
| history.layer_ops_undoable | PASS | ok (10ms) |
| io.project_roundtrip | PASS | format v3, 36 KiB (99ms) |
| io.corruption_detected | PASS | detected (1ms) |
| io.png_roundtrip | PASS | png 287B (6ms) |
| io.all_export_formats_produce_bytes | PASS | MONA=2419 PNG=467 JPEG=2349 TIFF=120154 PSD=210164 PDF=926 (213ms) |
| io.psd_has_layers | PASS | 2 layers (2ms) |
| compositor.layer_order | PASS | order ok (2ms) |
| compositor.opacity_and_blend | PASS | ok (0ms) |
| compositor.dirty_region_only | PASS | 0.01 ms per 256px region on 2048² canvas (4ms) |
| filters.all_apply | PASS | 16 filters (45ms) |
| selection.rectangle_limits_fill | PASS | ok (1ms) |
| selection.magic_wand | PASS | bounds RectI(left=5, top=5, right=15, bottom=15) (6ms) |
| viewport.roundtrip_and_zoom | PASS | ok (1ms) |
| perf.stroke_throughput | PASS | 0.48 ms per stylus event (2096 events/s) (286ms) |
| perf.large_canvas_memory | PASS | stroke on 4096² in 258 ms, 2816 KiB of tiles used (258ms) |
| perf.save_large_project | PASS | 2048² save 66 ms (0.1 MiB), load 41 ms (124ms) |

**27/27 checks passed.**

## Artifacts


## assembleDebug: compiler errors (26)

| File | Errors |
|---|---|
| `Panels` | 16 |
| `BrushStudioScreen` | 5 |
| `EditorViewModel` | 2 |
| `Components` | 2 |
| `NewCanvasScreen` | 1 |

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:211:39 Unresolved reference 'brushStudioState' on receiver of type 'MonaViewModel'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:275:17 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:276:55 Cannot infer type for type parameter 'R'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:276:90 Unresolved reference 'asImageBitmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:287:26 Argument type mismatch: actual type is 'Any', but 'ImageBitmap' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/EditorViewModel.kt:487:32 Unresolved reference 's'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/EditorViewModel.kt:487:63 Unresolved reference 's'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:220:14 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:306:35 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:307:35 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:575:112 Argument already passed for this parameter.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:575:25 Unresolved reference 'MonaPill' on receiver of type '() -> Unit'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:575:83 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:575:85 Argument already passed for this parameter.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:577:26 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:617:48 Unresolved reference 'it'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:617:51 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:617:51 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:617:51 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:625:48 Unresolved reference 'it'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:625:51 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:625:51 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:625:51 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:171:30 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:755:14 Suspend function 'suspend fun animateTo(targetValue: Float, animationSpec: AnimationSpec<Float>, initialVelocity: Float, block: (Animatable<Float, AnimationVector1D>.() -> Unit)?): AnimationResult<Float, AnimationVector1D>' can only be called from a coroutine or another suspend function.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:756:14 Suspend function 'suspend fun animateTo(targetValue: Float, animationSpec: AnimationSpec<Float>, initialVelocity: Float, block: (Animatable<Float, AnimationVector1D>.() -> Unit)?): AnimationResult<Float, AnimationVector1D>' can only be called from a coroutine or another suspend function.
```

## release: compiler errors (26)

| File | Errors |
|---|---|
| `Panels` | 16 |
| `BrushStudioScreen` | 5 |
| `EditorViewModel` | 2 |
| `Components` | 2 |
| `NewCanvasScreen` | 1 |

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:211:39 Unresolved reference 'brushStudioState' on receiver of type 'MonaViewModel'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:275:17 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:276:55 Cannot infer type for type parameter 'R'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:276:90 Unresolved reference 'asImageBitmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:287:26 Argument type mismatch: actual type is 'Any', but 'ImageBitmap' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/EditorViewModel.kt:487:32 Unresolved reference 's'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/EditorViewModel.kt:487:63 Unresolved reference 's'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:220:14 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:306:35 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:307:35 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:575:112 Argument already passed for this parameter.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:575:25 Unresolved reference 'MonaPill' on receiver of type '() -> Unit'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:575:83 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:575:85 Argument already passed for this parameter.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:577:26 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:617:48 Unresolved reference 'it'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:617:51 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:617:51 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:617:51 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:625:48 Unresolved reference 'it'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:625:51 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:625:51 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:625:51 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:171:30 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:755:14 Suspend function 'suspend fun animateTo(targetValue: Float, animationSpec: AnimationSpec<Float>, initialVelocity: Float, block: (Animatable<Float, AnimationVector1D>.() -> Unit)?): AnimationResult<Float, AnimationVector1D>' can only be called from a coroutine or another suspend function.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:756:14 Suspend function 'suspend fun animateTo(targetValue: Float, animationSpec: AnimationSpec<Float>, initialVelocity: Float, block: (Animatable<Float, AnimationVector1D>.() -> Unit)?): AnimationResult<Float, AnimationVector1D>' can only be called from a coroutine or another suspend function.
```

## app tests: compiler errors (26)

| File | Errors |
|---|---|
| `Panels` | 16 |
| `BrushStudioScreen` | 5 |
| `EditorViewModel` | 2 |
| `Components` | 2 |
| `NewCanvasScreen` | 1 |

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:211:39 Unresolved reference 'brushStudioState' on receiver of type 'MonaViewModel'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:275:17 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:276:55 Cannot infer type for type parameter 'R'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:276:90 Unresolved reference 'asImageBitmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:287:26 Argument type mismatch: actual type is 'Any', but 'ImageBitmap' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/EditorViewModel.kt:487:32 Unresolved reference 's'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/EditorViewModel.kt:487:63 Unresolved reference 's'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:220:14 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:306:35 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:307:35 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:575:112 Argument already passed for this parameter.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:575:25 Unresolved reference 'MonaPill' on receiver of type '() -> Unit'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:575:83 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:575:85 Argument already passed for this parameter.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:577:26 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:617:48 Unresolved reference 'it'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:617:51 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:617:51 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:617:51 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:625:48 Unresolved reference 'it'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:625:51 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:625:51 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:625:51 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:171:30 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:755:14 Suspend function 'suspend fun animateTo(targetValue: Float, animationSpec: AnimationSpec<Float>, initialVelocity: Float, block: (Animatable<Float, AnimationVector1D>.() -> Unit)?): AnimationResult<Float, AnimationVector1D>' can only be called from a coroutine or another suspend function.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:756:14 Suspend function 'suspend fun animateTo(targetValue: Float, animationSpec: AnimationSpec<Float>, initialVelocity: Float, block: (Animatable<Float, AnimationVector1D>.() -> Unit)?): AnimationResult<Float, AnimationVector1D>' can only be called from a coroutine or another suspend function.
```

## app tests: failure detail

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:211:39 Unresolved reference 'brushStudioState' on receiver of type 'MonaViewModel'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:275:17 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:276:55 Cannot infer type for type parameter 'R'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:276:90 Unresolved reference 'asImageBitmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:287:26 Argument type mismatch: actual type is 'Any', but 'ImageBitmap' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/EditorViewModel.kt:487:32 Unresolved reference 's'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/EditorViewModel.kt:487:63 Unresolved reference 's'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:220:14 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:306:35 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:307:35 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:575:25 Unresolved reference 'MonaPill' on receiver of type '() -> Unit'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:575:83 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:575:85 Argument already passed for this parameter.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:575:112 Argument already passed for this parameter.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:577:26 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:617:48 Unresolved reference 'it'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:617:51 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:617:51 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:617:51 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:625:48 Unresolved reference 'it'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:625:51 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:625:51 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:625:51 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:171:30 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:755:14 Suspend function 'suspend fun animateTo(targetValue: Float, animationSpec: AnimationSpec<Float>, initialVelocity: Float, block: (Animatable<Float, AnimationVector1D>.() -> Unit)?): AnimationResult<Float, AnimationVector1D>' can only be called from a coroutine or another suspend function.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:756:14 Suspend function 'suspend fun animateTo(targetValue: Float, animationSpec: AnimationSpec<Float>, initialVelocity: Float, block: (Animatable<Float, AnimationVector1D>.() -> Unit)?): AnimationResult<Float, AnimationVector1D>' can only be called from a coroutine or another suspend function.
> Task :app:compileDebugKotlin FAILED
[Incubating] Problems report is available at: file:///home/runner/work/Mona_Art/Mona_Art/build/reports/problems/problems-report.html
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':app:compileDebugKotlin' (registered by plugin 'com.android.internal.application').
> A failure occurred while executing org.jetbrains.kotlin.compilerRunner.btapi.BuildToolsApiCompilationWork
   > Kotlin compiler: UNRESOLVED_REFERENCE
       Kotlin compiler error
         Unresolved reference 'brushStudioState' on receiver of type 'MonaViewModel'.
         Location: /home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:211
```

## assembleDebug: failure detail

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:211:39 Unresolved reference 'brushStudioState' on receiver of type 'MonaViewModel'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:275:17 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:276:55 Cannot infer type for type parameter 'R'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:276:90 Unresolved reference 'asImageBitmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:287:26 Argument type mismatch: actual type is 'Any', but 'ImageBitmap' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/EditorViewModel.kt:487:32 Unresolved reference 's'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/EditorViewModel.kt:487:63 Unresolved reference 's'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:220:14 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:306:35 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:307:35 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:575:25 Unresolved reference 'MonaPill' on receiver of type '() -> Unit'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:575:83 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:575:85 Argument already passed for this parameter.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:575:112 Argument already passed for this parameter.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:577:26 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:617:48 Unresolved reference 'it'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:617:51 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:617:51 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:617:51 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:625:48 Unresolved reference 'it'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:625:51 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:625:51 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:625:51 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:171:30 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:755:14 Suspend function 'suspend fun animateTo(targetValue: Float, animationSpec: AnimationSpec<Float>, initialVelocity: Float, block: (Animatable<Float, AnimationVector1D>.() -> Unit)?): AnimationResult<Float, AnimationVector1D>' can only be called from a coroutine or another suspend function.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:756:14 Suspend function 'suspend fun animateTo(targetValue: Float, animationSpec: AnimationSpec<Float>, initialVelocity: Float, block: (Animatable<Float, AnimationVector1D>.() -> Unit)?): AnimationResult<Float, AnimationVector1D>' can only be called from a coroutine or another suspend function.
> Task :app:mergeLibDexDebug
> Task :app:stripDebugDebugSymbols
Unable to strip the following libraries, packaging them as they are: libandroidx.graphics.path.so, libdatastore_shared_counter.so. Run with --info option to learn more.
[Incubating] Problems report is available at: file:///home/runner/work/Mona_Art/Mona_Art/build/reports/problems/problems-report.html
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':app:compileDebugKotlin' (registered by plugin 'com.android.internal.application').
> A failure occurred while executing org.jetbrains.kotlin.compilerRunner.btapi.BuildToolsApiCompilationWork
   > Kotlin compiler: UNRESOLVED_REFERENCE
       Kotlin compiler error
```

## release: failure detail

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:211:39 Unresolved reference 'brushStudioState' on receiver of type 'MonaViewModel'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:275:17 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:276:55 Cannot infer type for type parameter 'R'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:276:90 Unresolved reference 'asImageBitmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:287:26 Argument type mismatch: actual type is 'Any', but 'ImageBitmap' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/EditorViewModel.kt:487:32 Unresolved reference 's'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/EditorViewModel.kt:487:63 Unresolved reference 's'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:220:14 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:306:35 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:307:35 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:575:25 Unresolved reference 'MonaPill' on receiver of type '() -> Unit'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:575:83 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:575:85 Argument already passed for this parameter.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:575:112 Argument already passed for this parameter.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:577:26 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:617:48 Unresolved reference 'it'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:617:51 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:617:51 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:617:51 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:625:48 Unresolved reference 'it'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:625:51 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:625:51 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:625:51 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:171:30 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:755:14 Suspend function 'suspend fun animateTo(targetValue: Float, animationSpec: AnimationSpec<Float>, initialVelocity: Float, block: (Animatable<Float, AnimationVector1D>.() -> Unit)?): AnimationResult<Float, AnimationVector1D>' can only be called from a coroutine or another suspend function.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:756:14 Suspend function 'suspend fun animateTo(targetValue: Float, animationSpec: AnimationSpec<Float>, initialVelocity: Float, block: (Animatable<Float, AnimationVector1D>.() -> Unit)?): AnimationResult<Float, AnimationVector1D>' can only be called from a coroutine or another suspend function.
> Task :app:compileReleaseKotlin FAILED
[Incubating] Problems report is available at: file:///home/runner/work/Mona_Art/Mona_Art/build/reports/problems/problems-report.html
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':app:compileReleaseKotlin' (registered by plugin 'com.android.internal.application').
> A failure occurred while executing org.jetbrains.kotlin.compilerRunner.btapi.BuildToolsApiCompilationWork
   > Kotlin compiler: UNRESOLVED_REFERENCE
       Kotlin compiler error
         Unresolved reference 'brushStudioState' on receiver of type 'MonaViewModel'.
         Location: /home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:211
```

## assembleDebug: log tail

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:755:14 Suspend function 'suspend fun animateTo(targetValue: Float, animationSpec: AnimationSpec<Float>, initialVelocity: Float, block: (Animatable<Float, AnimationVector1D>.() -> Unit)?): AnimationResult<Float, AnimationVector1D>' can only be called from a coroutine or another suspend function.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:756:14 Suspend function 'suspend fun animateTo(targetValue: Float, animationSpec: AnimationSpec<Float>, initialVelocity: Float, block: (Animatable<Float, AnimationVector1D>.() -> Unit)?): AnimationResult<Float, AnimationVector1D>' can only be called from a coroutine or another suspend function.
> Task :app:mergeLibDexDebug
> Task :app:stripDebugDebugSymbols
Unable to strip the following libraries, packaging them as they are: libandroidx.graphics.path.so, libdatastore_shared_counter.so. Run with --info option to learn more.
[Incubating] Problems report is available at: file:///home/runner/work/Mona_Art/Mona_Art/build/reports/problems/problems-report.html
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':app:compileDebugKotlin' (registered by plugin 'com.android.internal.application').
> A failure occurred while executing org.jetbrains.kotlin.compilerRunner.btapi.BuildToolsApiCompilationWork
   > Kotlin compiler: UNRESOLVED_REFERENCE
       Kotlin compiler error
         Unresolved reference 'brushStudioState' on receiver of type 'MonaViewModel'.
         Location: /home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:211
* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights from a Build Scan (powered by Develocity).
> Get more help at https://help.gradle.org.
Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.
You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.
For more on this, please refer to https://docs.gradle.org/9.8.2/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.
BUILD FAILED in 2m 5s
31 actionable tasks: 11 executed, 20 up-to-date
Configuration cache entry stored.
```

## release: log tail

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:625:51 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:171:30 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:755:14 Suspend function 'suspend fun animateTo(targetValue: Float, animationSpec: AnimationSpec<Float>, initialVelocity: Float, block: (Animatable<Float, AnimationVector1D>.() -> Unit)?): AnimationResult<Float, AnimationVector1D>' can only be called from a coroutine or another suspend function.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:756:14 Suspend function 'suspend fun animateTo(targetValue: Float, animationSpec: AnimationSpec<Float>, initialVelocity: Float, block: (Animatable<Float, AnimationVector1D>.() -> Unit)?): AnimationResult<Float, AnimationVector1D>' can only be called from a coroutine or another suspend function.
> Task :app:compileReleaseKotlin FAILED
[Incubating] Problems report is available at: file:///home/runner/work/Mona_Art/Mona_Art/build/reports/problems/problems-report.html
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':app:compileReleaseKotlin' (registered by plugin 'com.android.internal.application').
> A failure occurred while executing org.jetbrains.kotlin.compilerRunner.btapi.BuildToolsApiCompilationWork
   > Kotlin compiler: UNRESOLVED_REFERENCE
       Kotlin compiler error
         Unresolved reference 'brushStudioState' on receiver of type 'MonaViewModel'.
         Location: /home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:211
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
