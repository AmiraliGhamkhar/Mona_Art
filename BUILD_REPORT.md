# Build report

Generated: 2026-11-04T23:36:23.966526+00:00

Commit: `ada073f4de5d1d22e9bd878fc279bcb1a8c4a5dd`

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

Generated: 2026-11-04T23:31:49.147678006Z

| Check | Result | Detail |
|---|---|---|
| blend.normal_passthrough | PASS | kotlin.Unit (5ms) |
| blend.multiply_reference | PASS | kotlin.Unit (0ms) |
| blend.all_modes_defined | PASS | 17 modes (9ms) |
| blend.composite_alpha_math | PASS | kotlin.Unit (0ms) |
| brush.pressure_affects_geometry | PASS | radius 1.51 (30%%) vs 4.50 (100%%) (21ms) |
| brush.spacing_emits_dabs | PASS | 396 dabs over 200px (3ms) |
| brush.stroke_paints_pixels | PASS | alpha at centre 255 (22ms) |
| brush.eraser_removes_alpha | PASS | alpha after erase 0 (16ms) |
| brush.all_presets_render | PASS | 30 presets (412ms) |
| brush.sparse_samples_have_no_gaps | PASS | 907 dabs, max gap 0.50px, 4px tall stroke (25ms) |
| history.undo_redo_restores_pixels | PASS | undo/redo ok (2ms) |
| history.layer_ops_undoable | PASS | ok (8ms) |
| io.project_roundtrip | PASS | format v3, 36 KiB (104ms) |
| io.corruption_detected | PASS | detected (1ms) |
| io.png_roundtrip | PASS | png 287B (5ms) |
| io.all_export_formats_produce_bytes | PASS | MONA=2419 PNG=467 JPEG=2349 TIFF=120154 PSD=210164 PDF=926 (190ms) |
| io.psd_has_layers | PASS | 2 layers (1ms) |
| compositor.layer_order | PASS | order ok (2ms) |
| compositor.opacity_and_blend | PASS | ok (0ms) |
| compositor.dirty_region_only | PASS | 0.00 ms per 256px region on 2048² canvas (4ms) |
| filters.all_apply | PASS | 16 filters (45ms) |
| selection.rectangle_limits_fill | PASS | ok (0ms) |
| selection.magic_wand | PASS | bounds RectI(left=5, top=5, right=15, bottom=15) (5ms) |
| viewport.roundtrip_and_zoom | PASS | ok (0ms) |
| perf.stroke_throughput | PASS | 0.47 ms per stylus event (2126 events/s) (282ms) |
| perf.large_canvas_memory | PASS | stroke on 4096² in 245 ms, 2816 KiB of tiles used (245ms) |
| perf.save_large_project | PASS | 2048² save 67 ms (0.1 MiB), load 33 ms (105ms) |

**27/27 checks passed.**

## Artifacts


## assembleDebug: compiler errors (77)

| File | Errors |
|---|---|
| `NewCanvasScreen` | 25 |
| `Components` | 18 |
| `Panels` | 16 |
| `Sheets` | 6 |
| `CanvasInput` | 5 |
| `BrushStudioScreen` | 4 |
| `EditorViewModel` | 2 |
| `MonaSurfaceView` | 1 |

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:144:48 Unresolved reference 'FingerOrStylus'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:144:88 Unresolved reference 'TouchPaint'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:324:37 Unresolved reference 'hasAxis' on receiver of type 'InputDevice'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:326:45 Unresolved reference 'hasAxis' on receiver of type 'InputDevice'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:397:18 Unresolved reference 'hasAxis' on receiver of type 'InputDevice'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/MonaSurfaceView.kt:85:67 @Composable invocations can only happen from the context of a @Composable function
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:211:39 Unresolved reference 'brushStudioState' on receiver of type 'MonaViewModel'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:275:17 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:276:55 Unresolved reference 'asImageBitmap' on receiver of type 'Bitmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:287:26 Argument type mismatch: actual type is 'Any', but 'ImageBitmap' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/EditorViewModel.kt:485:55 No value passed for parameter 'focusX'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/EditorViewModel.kt:485:55 No value passed for parameter 'focusY'.
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
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:103:59 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:110:60 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:169:60 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:219:62 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:58:26 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:96:60 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:158:26 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:171:30 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:229:21 Unresolved reference 'MonaPill' on receiver of type '() -> Unit'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:229:43 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:229:45 Argument already passed for this parameter.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:229:74 Argument already passed for this parameter.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:232:134 Cannot infer type for value parameter 'c'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:232:21 Unresolved reference 'com' on receiver of type '() -> Unit'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:232:24 Syntax error: Expecting an element.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:232:32 Syntax error: Expecting an element.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:232:39 Syntax error: Expecting an element.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:232:44 Syntax error: Expecting an element.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:232:51 Syntax error: Expecting an element.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:232:64 Syntax error: Expecting an element.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:232:79 Syntax error: Expecting an element.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:232:86 Syntax error: Expecting an element.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:232:95 None of the following candidates is applicable:
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:233:38 Unresolved reference 'value'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:233:44 Cannot infer type for type parameter 'R'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:233:44 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:233:48 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:234:25 @Composable invocations can only happen from the context of a @Composable function
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:242:22 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:271:26 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:301:43 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:495:18 Unresolved reference 'pointerInput'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:495:9 Argument type mismatch: actual type is 'Comparator<uninferred T (of fun <T> Comparator<T>.then)>', but 'Modifier' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:496:13 Candidate 'suspend fun PointerInputScope.detectTapGestures(onDoubleTap: ((Offset) -> Unit)? = ..., onLongPress: ((Offset) -> Unit)? = ..., onPress: suspend PressGestureScope.(Offset) -> Unit = ..., onTap: ((Offset) -> Unit)? = ...): Unit' is inapplicable because of a receiver type mismatch.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:496:13 Suspend function 'suspend fun PointerInputScope.detectTapGestures(onDoubleTap: ((Offset) -> Unit)? = ..., onLongPress: ((Offset) -> Unit)? = ..., onPress: suspend PressGestureScope.(Offset) -> Unit = ..., onTap: ((Offset) -> Unit)? = ...): Unit' can only be called from a coroutine or another suspend function.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:497:40 Function invocation 'size(...)' expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:497:40 None of the following candidates is applicable:
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:497:45 Function invocation 'width(...)' expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:500:11 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:501:22 Unresolved reference 'pointerInput'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:502:54 Unresolved reference 'detectHorizontalDragGestures'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:502:85 Cannot infer type for value parameter 'change'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:502:93 Cannot infer type for implicit value parameter 'it'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:503:40 Unresolved reference 'position'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:503:53 Function invocation 'size(...)' expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:503:53 None of the following candidates is applicable:
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:503:58 Function invocation 'width(...)' expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:755:14 Suspend function 'suspend fun animateTo(targetValue: Float, animationSpec: AnimationSpec<Float>, initialVelocity: Float, block: (Animatable<Float, AnimationVector1D>.() -> Unit)?): AnimationResult<Float, AnimationVector1D>' can only be called from a coroutine or another suspend function.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:756:14 Suspend function 'suspend fun animateTo(targetValue: Float, animationSpec: AnimationSpec<Float>, initialVelocity: Float, block: (Animatable<Float, AnimationVector1D>.() -> Unit)?): AnimationResult<Float, AnimationVector1D>' can only be called from a coroutine or another suspend function.
```

## release: compiler errors (77)

| File | Errors |
|---|---|
| `NewCanvasScreen` | 25 |
| `Components` | 18 |
| `Panels` | 16 |
| `Sheets` | 6 |
| `CanvasInput` | 5 |
| `BrushStudioScreen` | 4 |
| `EditorViewModel` | 2 |
| `MonaSurfaceView` | 1 |

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:144:48 Unresolved reference 'FingerOrStylus'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:144:88 Unresolved reference 'TouchPaint'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:324:37 Unresolved reference 'hasAxis' on receiver of type 'InputDevice'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:326:45 Unresolved reference 'hasAxis' on receiver of type 'InputDevice'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:397:18 Unresolved reference 'hasAxis' on receiver of type 'InputDevice'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/MonaSurfaceView.kt:85:67 @Composable invocations can only happen from the context of a @Composable function
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:211:39 Unresolved reference 'brushStudioState' on receiver of type 'MonaViewModel'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:275:17 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:276:55 Unresolved reference 'asImageBitmap' on receiver of type 'Bitmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:287:26 Argument type mismatch: actual type is 'Any', but 'ImageBitmap' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/EditorViewModel.kt:485:55 No value passed for parameter 'focusX'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/EditorViewModel.kt:485:55 No value passed for parameter 'focusY'.
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
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:103:59 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:110:60 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:169:60 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:219:62 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:58:26 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:96:60 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:158:26 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:171:30 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:229:21 Unresolved reference 'MonaPill' on receiver of type '() -> Unit'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:229:43 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:229:45 Argument already passed for this parameter.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:229:74 Argument already passed for this parameter.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:232:134 Cannot infer type for value parameter 'c'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:232:21 Unresolved reference 'com' on receiver of type '() -> Unit'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:232:24 Syntax error: Expecting an element.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:232:32 Syntax error: Expecting an element.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:232:39 Syntax error: Expecting an element.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:232:44 Syntax error: Expecting an element.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:232:51 Syntax error: Expecting an element.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:232:64 Syntax error: Expecting an element.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:232:79 Syntax error: Expecting an element.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:232:86 Syntax error: Expecting an element.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:232:95 None of the following candidates is applicable:
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:233:38 Unresolved reference 'value'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:233:44 Cannot infer type for type parameter 'R'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:233:44 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:233:48 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:234:25 @Composable invocations can only happen from the context of a @Composable function
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:242:22 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:271:26 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:301:43 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:495:18 Unresolved reference 'pointerInput'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:495:9 Argument type mismatch: actual type is 'Comparator<uninferred T (of fun <T> Comparator<T>.then)>', but 'Modifier' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:496:13 Candidate 'suspend fun PointerInputScope.detectTapGestures(onDoubleTap: ((Offset) -> Unit)? = ..., onLongPress: ((Offset) -> Unit)? = ..., onPress: suspend PressGestureScope.(Offset) -> Unit = ..., onTap: ((Offset) -> Unit)? = ...): Unit' is inapplicable because of a receiver type mismatch.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:496:13 Suspend function 'suspend fun PointerInputScope.detectTapGestures(onDoubleTap: ((Offset) -> Unit)? = ..., onLongPress: ((Offset) -> Unit)? = ..., onPress: suspend PressGestureScope.(Offset) -> Unit = ..., onTap: ((Offset) -> Unit)? = ...): Unit' can only be called from a coroutine or another suspend function.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:497:40 Function invocation 'size(...)' expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:497:40 None of the following candidates is applicable:
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:497:45 Function invocation 'width(...)' expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:500:11 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:501:22 Unresolved reference 'pointerInput'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:502:54 Unresolved reference 'detectHorizontalDragGestures'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:502:85 Cannot infer type for value parameter 'change'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:502:93 Cannot infer type for implicit value parameter 'it'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:503:40 Unresolved reference 'position'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:503:53 Function invocation 'size(...)' expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:503:53 None of the following candidates is applicable:
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:503:58 Function invocation 'width(...)' expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:755:14 Suspend function 'suspend fun animateTo(targetValue: Float, animationSpec: AnimationSpec<Float>, initialVelocity: Float, block: (Animatable<Float, AnimationVector1D>.() -> Unit)?): AnimationResult<Float, AnimationVector1D>' can only be called from a coroutine or another suspend function.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:756:14 Suspend function 'suspend fun animateTo(targetValue: Float, animationSpec: AnimationSpec<Float>, initialVelocity: Float, block: (Animatable<Float, AnimationVector1D>.() -> Unit)?): AnimationResult<Float, AnimationVector1D>' can only be called from a coroutine or another suspend function.
```

## app tests: compiler errors (77)

| File | Errors |
|---|---|
| `NewCanvasScreen` | 25 |
| `Components` | 18 |
| `Panels` | 16 |
| `Sheets` | 6 |
| `CanvasInput` | 5 |
| `BrushStudioScreen` | 4 |
| `EditorViewModel` | 2 |
| `MonaSurfaceView` | 1 |

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:144:48 Unresolved reference 'FingerOrStylus'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:144:88 Unresolved reference 'TouchPaint'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:324:37 Unresolved reference 'hasAxis' on receiver of type 'InputDevice'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:326:45 Unresolved reference 'hasAxis' on receiver of type 'InputDevice'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:397:18 Unresolved reference 'hasAxis' on receiver of type 'InputDevice'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/MonaSurfaceView.kt:85:67 @Composable invocations can only happen from the context of a @Composable function
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:211:39 Unresolved reference 'brushStudioState' on receiver of type 'MonaViewModel'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:275:17 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:276:55 Unresolved reference 'asImageBitmap' on receiver of type 'Bitmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:287:26 Argument type mismatch: actual type is 'Any', but 'ImageBitmap' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/EditorViewModel.kt:485:55 No value passed for parameter 'focusX'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/EditorViewModel.kt:485:55 No value passed for parameter 'focusY'.
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
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:103:59 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:110:60 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:169:60 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:219:62 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:58:26 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:96:60 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:158:26 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:171:30 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:229:21 Unresolved reference 'MonaPill' on receiver of type '() -> Unit'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:229:43 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:229:45 Argument already passed for this parameter.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:229:74 Argument already passed for this parameter.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:232:134 Cannot infer type for value parameter 'c'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:232:21 Unresolved reference 'com' on receiver of type '() -> Unit'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:232:24 Syntax error: Expecting an element.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:232:32 Syntax error: Expecting an element.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:232:39 Syntax error: Expecting an element.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:232:44 Syntax error: Expecting an element.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:232:51 Syntax error: Expecting an element.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:232:64 Syntax error: Expecting an element.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:232:79 Syntax error: Expecting an element.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:232:86 Syntax error: Expecting an element.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:232:95 None of the following candidates is applicable:
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:233:38 Unresolved reference 'value'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:233:44 Cannot infer type for type parameter 'R'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:233:44 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:233:48 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:234:25 @Composable invocations can only happen from the context of a @Composable function
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:242:22 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:271:26 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:301:43 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:495:18 Unresolved reference 'pointerInput'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:495:9 Argument type mismatch: actual type is 'Comparator<uninferred T (of fun <T> Comparator<T>.then)>', but 'Modifier' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:496:13 Candidate 'suspend fun PointerInputScope.detectTapGestures(onDoubleTap: ((Offset) -> Unit)? = ..., onLongPress: ((Offset) -> Unit)? = ..., onPress: suspend PressGestureScope.(Offset) -> Unit = ..., onTap: ((Offset) -> Unit)? = ...): Unit' is inapplicable because of a receiver type mismatch.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:496:13 Suspend function 'suspend fun PointerInputScope.detectTapGestures(onDoubleTap: ((Offset) -> Unit)? = ..., onLongPress: ((Offset) -> Unit)? = ..., onPress: suspend PressGestureScope.(Offset) -> Unit = ..., onTap: ((Offset) -> Unit)? = ...): Unit' can only be called from a coroutine or another suspend function.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:497:40 Function invocation 'size(...)' expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:497:40 None of the following candidates is applicable:
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:497:45 Function invocation 'width(...)' expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:500:11 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:501:22 Unresolved reference 'pointerInput'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:502:54 Unresolved reference 'detectHorizontalDragGestures'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:502:85 Cannot infer type for value parameter 'change'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:502:93 Cannot infer type for implicit value parameter 'it'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:503:40 Unresolved reference 'position'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:503:53 Function invocation 'size(...)' expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:503:53 None of the following candidates is applicable:
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:503:58 Function invocation 'width(...)' expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:755:14 Suspend function 'suspend fun animateTo(targetValue: Float, animationSpec: AnimationSpec<Float>, initialVelocity: Float, block: (Animatable<Float, AnimationVector1D>.() -> Unit)?): AnimationResult<Float, AnimationVector1D>' can only be called from a coroutine or another suspend function.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:756:14 Suspend function 'suspend fun animateTo(targetValue: Float, animationSpec: AnimationSpec<Float>, initialVelocity: Float, block: (Animatable<Float, AnimationVector1D>.() -> Unit)?): AnimationResult<Float, AnimationVector1D>' can only be called from a coroutine or another suspend function.
```

## app tests: failure detail

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:144:48 Unresolved reference 'FingerOrStylus'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:144:88 Unresolved reference 'TouchPaint'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:324:37 Unresolved reference 'hasAxis' on receiver of type 'InputDevice'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:326:45 Unresolved reference 'hasAxis' on receiver of type 'InputDevice'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:397:18 Unresolved reference 'hasAxis' on receiver of type 'InputDevice'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/MonaSurfaceView.kt:85:67 @Composable invocations can only happen from the context of a @Composable function
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:211:39 Unresolved reference 'brushStudioState' on receiver of type 'MonaViewModel'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:275:17 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:276:55 Unresolved reference 'asImageBitmap' on receiver of type 'Bitmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:287:26 Argument type mismatch: actual type is 'Any', but 'ImageBitmap' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/EditorViewModel.kt:485:55 No value passed for parameter 'focusX'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/EditorViewModel.kt:485:55 No value passed for parameter 'focusY'.
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
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:58:26 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:96:60 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:103:59 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:110:60 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:169:60 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:219:62 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:158:26 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:171:30 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:229:21 Unresolved reference 'MonaPill' on receiver of type '() -> Unit'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:229:43 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:229:45 Argument already passed for this parameter.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:229:74 Argument already passed for this parameter.
```

## assembleDebug: failure detail

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:144:48 Unresolved reference 'FingerOrStylus'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:144:88 Unresolved reference 'TouchPaint'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:324:37 Unresolved reference 'hasAxis' on receiver of type 'InputDevice'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:326:45 Unresolved reference 'hasAxis' on receiver of type 'InputDevice'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:397:18 Unresolved reference 'hasAxis' on receiver of type 'InputDevice'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/MonaSurfaceView.kt:85:67 @Composable invocations can only happen from the context of a @Composable function
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:211:39 Unresolved reference 'brushStudioState' on receiver of type 'MonaViewModel'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:275:17 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:276:55 Unresolved reference 'asImageBitmap' on receiver of type 'Bitmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:287:26 Argument type mismatch: actual type is 'Any', but 'ImageBitmap' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/EditorViewModel.kt:485:55 No value passed for parameter 'focusX'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/EditorViewModel.kt:485:55 No value passed for parameter 'focusY'.
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
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:58:26 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:96:60 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:103:59 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:110:60 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:169:60 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:219:62 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:158:26 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:171:30 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:229:21 Unresolved reference 'MonaPill' on receiver of type '() -> Unit'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:229:43 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:229:45 Argument already passed for this parameter.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:229:74 Argument already passed for this parameter.
```

## release: failure detail

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:144:48 Unresolved reference 'FingerOrStylus'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:144:88 Unresolved reference 'TouchPaint'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:324:37 Unresolved reference 'hasAxis' on receiver of type 'InputDevice'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:326:45 Unresolved reference 'hasAxis' on receiver of type 'InputDevice'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:397:18 Unresolved reference 'hasAxis' on receiver of type 'InputDevice'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/MonaSurfaceView.kt:85:67 @Composable invocations can only happen from the context of a @Composable function
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:211:39 Unresolved reference 'brushStudioState' on receiver of type 'MonaViewModel'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:275:17 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:276:55 Unresolved reference 'asImageBitmap' on receiver of type 'Bitmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:287:26 Argument type mismatch: actual type is 'Any', but 'ImageBitmap' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/EditorViewModel.kt:485:55 No value passed for parameter 'focusX'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/EditorViewModel.kt:485:55 No value passed for parameter 'focusY'.
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
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:58:26 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:96:60 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:103:59 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:110:60 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:169:60 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:219:62 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:158:26 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:171:30 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:229:21 Unresolved reference 'MonaPill' on receiver of type '() -> Unit'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:229:43 Syntax error: Expecting ')'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:229:45 Argument already passed for this parameter.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:229:74 Argument already passed for this parameter.
```

## assembleDebug: log tail

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:503:58 Function invocation 'width(...)' expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:755:14 Suspend function 'suspend fun animateTo(targetValue: Float, animationSpec: AnimationSpec<Float>, initialVelocity: Float, block: (Animatable<Float, AnimationVector1D>.() -> Unit)?): AnimationResult<Float, AnimationVector1D>' can only be called from a coroutine or another suspend function.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:756:14 Suspend function 'suspend fun animateTo(targetValue: Float, animationSpec: AnimationSpec<Float>, initialVelocity: Float, block: (Animatable<Float, AnimationVector1D>.() -> Unit)?): AnimationResult<Float, AnimationVector1D>' can only be called from a coroutine or another suspend function.
> Task :app:stripDebugDebugSymbols
Unable to strip the following libraries, packaging them as they are: libandroidx.graphics.path.so, libdatastore_shared_counter.so. Run with --info option to learn more.
[Incubating] Problems report is available at: file:///home/runner/work/Mona_Art/Mona_Art/build/reports/problems/problems-report.html
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':app:compileDebugKotlin' (registered by plugin 'com.android.internal.application').
> A failure occurred while executing org.jetbrains.kotlin.compilerRunner.btapi.BuildToolsApiCompilationWork
   > Kotlin compiler: UNRESOLVED_REFERENCE
       Kotlin compiler error
         Unresolved reference 'FingerOrStylus'.
         Location: /home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:144
* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights from a Build Scan (powered by Develocity).
> Get more help at https://help.gradle.org.
Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.
You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.
For more on this, please refer to https://docs.gradle.org/9.8.2/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.
BUILD FAILED in 2m 3s
31 actionable tasks: 11 executed, 20 up-to-date
Configuration cache entry stored.
```

## release: log tail

```
  No value passed for parameter 'size'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:503:58 Function invocation 'width(...)' expected.
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
         Unresolved reference 'FingerOrStylus'.
         Location: /home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:144
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
