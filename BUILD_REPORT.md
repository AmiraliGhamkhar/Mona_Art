# Build report

Generated: 2026-11-04T23:18:03.520789+00:00

Commit: `138a8f5a3538d5f19ca792cca668583c82dbd3ea`

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

Generated: 2026-11-04T23:14:30.861626Z

| Check | Result | Detail |
|---|---|---|
| blend.normal_passthrough | PASS | kotlin.Unit (5ms) |
| blend.multiply_reference | PASS | kotlin.Unit (0ms) |
| blend.all_modes_defined | PASS | 17 modes (7ms) |
| blend.composite_alpha_math | PASS | kotlin.Unit (0ms) |
| brush.pressure_affects_geometry | PASS | radius 1.51 (30%%) vs 4.50 (100%%) (15ms) |
| brush.spacing_emits_dabs | PASS | 396 dabs over 200px (2ms) |
| brush.stroke_paints_pixels | PASS | alpha at centre 255 (17ms) |
| brush.eraser_removes_alpha | PASS | alpha after erase 0 (14ms) |
| brush.all_presets_render | PASS | 30 presets (319ms) |
| brush.sparse_samples_have_no_gaps | PASS | 907 dabs, max gap 0.50px, 4px tall stroke (18ms) |
| history.undo_redo_restores_pixels | PASS | undo/redo ok (2ms) |
| history.layer_ops_undoable | PASS | ok (8ms) |
| io.project_roundtrip | PASS | format v3, 36 KiB (79ms) |
| io.corruption_detected | PASS | detected (1ms) |
| io.png_roundtrip | PASS | png 287B (5ms) |
| io.all_export_formats_produce_bytes | PASS | MONA=2419 PNG=467 JPEG=2349 TIFF=120154 PSD=210164 PDF=926 (170ms) |
| io.psd_has_layers | PASS | 2 layers (2ms) |
| compositor.layer_order | PASS | order ok (1ms) |
| compositor.opacity_and_blend | PASS | ok (0ms) |
| compositor.dirty_region_only | PASS | 0.00 ms per 256px region on 2048² canvas (3ms) |
| filters.all_apply | PASS | 16 filters (36ms) |
| selection.rectangle_limits_fill | PASS | ok (0ms) |
| selection.magic_wand | PASS | bounds RectI(left=5, top=5, right=15, bottom=15) (4ms) |
| viewport.roundtrip_and_zoom | PASS | ok (0ms) |
| perf.stroke_throughput | PASS | 0.35 ms per stylus event (2892 events/s) (207ms) |
| perf.large_canvas_memory | PASS | stroke on 4096² in 239 ms, 2816 KiB of tiles used (239ms) |
| perf.save_large_project | PASS | 2048² save 51 ms (0.1 MiB), load 41 ms (96ms) |

**27/27 checks passed.**

## Artifacts


## assembleDebug: errors

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/design/MonaTheme.kt:142:9 Argument type mismatch: actual type is 'Unit', but 'ProvidedValue<*>' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/design/MonaTheme.kt:142:20 No value passed for parameter 'content'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:141:42 Unresolved reference 'pressure' on receiver of type 'HistoricalChange'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:142:45 Unresolved reference 'pressure' on receiver of type 'HistoricalChange'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:142:57 Unresolved reference 'orientation' on receiver of type 'HistoricalChange'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:142:72 Unresolved reference 'type' on receiver of type 'HistoricalChange'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:143:45 Unresolved reference 'orientation' on receiver of type 'HistoricalChange'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:278:33 Unresolved reference 'orientation'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:279:23 Unresolved reference 'orientation'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:287:42 Unresolved reference 'attach' on receiver of type 'CanvasInputHandler'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/domain/tool/Tool.kt:10:49 Unresolved reference 'FormatPaintOff'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/domain/tool/Tool.kt:53:39 Unresolved reference 'FormatPaintOff'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:267:21 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:268:22 Cannot infer type for type parameter 'R'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:271:46 Unresolved reference 'asImageBitmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:276:26 Argument type mismatch: actual type is 'Any', but 'ImageBitmap' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/EditorViewModel.kt:40:39 Unresolved reference 'Tool'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/EditorViewModel.kt:345:9 'when' expression must be exhaustive. Add the 'MagicWand', 'ShowSelection', 'Liquify', 'Crop' branches or an 'else' branch.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:205:85 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:286:35 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:287:35 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:552:122 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:555:112 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:58:26 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:96:60 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:103:59 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:110:60 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:169:60 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:219:62 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/home/HomeScreen.kt:403:32 Unresolved reference 'collectAsStateWithLifecycle'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:153:86 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:166:90 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:226:114 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:229:74 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:271:26 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:301:43 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/settings/SettingsScreen.kt:309:32 Unresolved reference 'collectAsStateWithLifecycle'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:84:9 Functions which invoke @Composable functions must be marked with the @Composable annotation
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:85:9 @Composable invocations can only happen from the context of a @Composable function
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:488:22 Functions which invoke @Composable functions must be marked with the @Composable annotation
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:489:14 @Composable invocations can only happen from the context of a @Composable function
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:495:9 Argument type mismatch: actual type is 'Comparator<uninferred T (of fun <T> Comparator<T>.then)>', but 'Modifier' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:495:18 Unresolved reference 'pointerInput'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:496:13 Suspend function 'suspend fun PointerInputScope.detectTapGestures(onDoubleTap: ((Offset) -> Unit)? = ..., onLongPress: ((Offset) -> Unit)? = ..., onPress: suspend PressGestureScope.(Offset) -> Unit = ..., onTap: ((Offset) -> Unit)? = ...): Unit' can only be called from a coroutine or another suspend function.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:496:13 Candidate 'suspend fun PointerInputScope.detectTapGestures(onDoubleTap: ((Offset) -> Unit)? = ..., onLongPress: ((Offset) -> Unit)? = ..., onPress: suspend PressGestureScope.(Offset) -> Unit = ..., onTap: ((Offset) -> Unit)? = ...): Unit' is inapplicable because of a receiver type mismatch.
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
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:744:14 Suspend function 'suspend fun animateTo(targetValue: Float, animationSpec: AnimationSpec<Float>, initialVelocity: Float, block: (Animatable<Float, AnimationVector1D>.() -> Unit)?): AnimationResult<Float, AnimationVector1D>' can only be called from a coroutine or another suspend function.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:745:14 Suspend function 'suspend fun animateTo(targetValue: Float, animationSpec: AnimationSpec<Float>, initialVelocity: Float, block: (Animatable<Float, AnimationVector1D>.() -> Unit)?): AnimationResult<Float, AnimationVector1D>' can only be called from a coroutine or another suspend function.
FAILURE: Build failed with an exception.
```

## release: errors

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/design/MonaTheme.kt:142:9 Argument type mismatch: actual type is 'Unit', but 'ProvidedValue<*>' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/design/MonaTheme.kt:142:20 No value passed for parameter 'content'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:141:42 Unresolved reference 'pressure' on receiver of type 'HistoricalChange'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:142:45 Unresolved reference 'pressure' on receiver of type 'HistoricalChange'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:142:57 Unresolved reference 'orientation' on receiver of type 'HistoricalChange'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:142:72 Unresolved reference 'type' on receiver of type 'HistoricalChange'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:143:45 Unresolved reference 'orientation' on receiver of type 'HistoricalChange'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:278:33 Unresolved reference 'orientation'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:279:23 Unresolved reference 'orientation'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:287:42 Unresolved reference 'attach' on receiver of type 'CanvasInputHandler'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/domain/tool/Tool.kt:10:49 Unresolved reference 'FormatPaintOff'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/domain/tool/Tool.kt:53:39 Unresolved reference 'FormatPaintOff'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:267:21 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:268:22 Cannot infer type for type parameter 'R'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:271:46 Unresolved reference 'asImageBitmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:276:26 Argument type mismatch: actual type is 'Any', but 'ImageBitmap' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/EditorViewModel.kt:40:39 Unresolved reference 'Tool'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/EditorViewModel.kt:345:9 'when' expression must be exhaustive. Add the 'MagicWand', 'ShowSelection', 'Liquify', 'Crop' branches or an 'else' branch.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:205:85 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:286:35 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:287:35 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:552:122 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:555:112 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:58:26 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:96:60 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:103:59 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:110:60 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:169:60 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:219:62 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/home/HomeScreen.kt:403:32 Unresolved reference 'collectAsStateWithLifecycle'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:153:86 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:166:90 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:226:114 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:229:74 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:271:26 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:301:43 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/settings/SettingsScreen.kt:309:32 Unresolved reference 'collectAsStateWithLifecycle'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:84:9 Functions which invoke @Composable functions must be marked with the @Composable annotation
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:85:9 @Composable invocations can only happen from the context of a @Composable function
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:488:22 Functions which invoke @Composable functions must be marked with the @Composable annotation
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:489:14 @Composable invocations can only happen from the context of a @Composable function
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:495:9 Argument type mismatch: actual type is 'Comparator<uninferred T (of fun <T> Comparator<T>.then)>', but 'Modifier' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:495:18 Unresolved reference 'pointerInput'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:496:13 Suspend function 'suspend fun PointerInputScope.detectTapGestures(onDoubleTap: ((Offset) -> Unit)? = ..., onLongPress: ((Offset) -> Unit)? = ..., onPress: suspend PressGestureScope.(Offset) -> Unit = ..., onTap: ((Offset) -> Unit)? = ...): Unit' can only be called from a coroutine or another suspend function.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:496:13 Candidate 'suspend fun PointerInputScope.detectTapGestures(onDoubleTap: ((Offset) -> Unit)? = ..., onLongPress: ((Offset) -> Unit)? = ..., onPress: suspend PressGestureScope.(Offset) -> Unit = ..., onTap: ((Offset) -> Unit)? = ...): Unit' is inapplicable because of a receiver type mismatch.
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
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:744:14 Suspend function 'suspend fun animateTo(targetValue: Float, animationSpec: AnimationSpec<Float>, initialVelocity: Float, block: (Animatable<Float, AnimationVector1D>.() -> Unit)?): AnimationResult<Float, AnimationVector1D>' can only be called from a coroutine or another suspend function.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:745:14 Suspend function 'suspend fun animateTo(targetValue: Float, animationSpec: AnimationSpec<Float>, initialVelocity: Float, block: (Animatable<Float, AnimationVector1D>.() -> Unit)?): AnimationResult<Float, AnimationVector1D>' can only be called from a coroutine or another suspend function.
FAILURE: Build failed with an exception.
```

## app tests: errors

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/design/MonaTheme.kt:142:9 Argument type mismatch: actual type is 'Unit', but 'ProvidedValue<*>' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/design/MonaTheme.kt:142:20 No value passed for parameter 'content'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:141:42 Unresolved reference 'pressure' on receiver of type 'HistoricalChange'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:142:45 Unresolved reference 'pressure' on receiver of type 'HistoricalChange'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:142:57 Unresolved reference 'orientation' on receiver of type 'HistoricalChange'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:142:72 Unresolved reference 'type' on receiver of type 'HistoricalChange'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:143:45 Unresolved reference 'orientation' on receiver of type 'HistoricalChange'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:278:33 Unresolved reference 'orientation'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:279:23 Unresolved reference 'orientation'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:287:42 Unresolved reference 'attach' on receiver of type 'CanvasInputHandler'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/domain/tool/Tool.kt:10:49 Unresolved reference 'FormatPaintOff'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/domain/tool/Tool.kt:53:39 Unresolved reference 'FormatPaintOff'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:267:21 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:268:22 Cannot infer type for type parameter 'R'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:271:46 Unresolved reference 'asImageBitmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:276:26 Argument type mismatch: actual type is 'Any', but 'ImageBitmap' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/EditorViewModel.kt:40:39 Unresolved reference 'Tool'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/EditorViewModel.kt:345:9 'when' expression must be exhaustive. Add the 'MagicWand', 'ShowSelection', 'Liquify', 'Crop' branches or an 'else' branch.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:205:85 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:286:35 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:287:35 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:552:122 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:555:112 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:58:26 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:96:60 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:103:59 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:110:60 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:169:60 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:219:62 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/home/HomeScreen.kt:403:32 Unresolved reference 'collectAsStateWithLifecycle'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:153:86 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:166:90 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:226:114 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:229:74 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:271:26 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:301:43 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/settings/SettingsScreen.kt:309:32 Unresolved reference 'collectAsStateWithLifecycle'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:84:9 Functions which invoke @Composable functions must be marked with the @Composable annotation
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:85:9 @Composable invocations can only happen from the context of a @Composable function
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:488:22 Functions which invoke @Composable functions must be marked with the @Composable annotation
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:489:14 @Composable invocations can only happen from the context of a @Composable function
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:495:9 Argument type mismatch: actual type is 'Comparator<uninferred T (of fun <T> Comparator<T>.then)>', but 'Modifier' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:495:18 Unresolved reference 'pointerInput'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:496:13 Suspend function 'suspend fun PointerInputScope.detectTapGestures(onDoubleTap: ((Offset) -> Unit)? = ..., onLongPress: ((Offset) -> Unit)? = ..., onPress: suspend PressGestureScope.(Offset) -> Unit = ..., onTap: ((Offset) -> Unit)? = ...): Unit' can only be called from a coroutine or another suspend function.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:496:13 Candidate 'suspend fun PointerInputScope.detectTapGestures(onDoubleTap: ((Offset) -> Unit)? = ..., onLongPress: ((Offset) -> Unit)? = ..., onPress: suspend PressGestureScope.(Offset) -> Unit = ..., onTap: ((Offset) -> Unit)? = ...): Unit' is inapplicable because of a receiver type mismatch.
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
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:744:14 Suspend function 'suspend fun animateTo(targetValue: Float, animationSpec: AnimationSpec<Float>, initialVelocity: Float, block: (Animatable<Float, AnimationVector1D>.() -> Unit)?): AnimationResult<Float, AnimationVector1D>' can only be called from a coroutine or another suspend function.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:745:14 Suspend function 'suspend fun animateTo(targetValue: Float, animationSpec: AnimationSpec<Float>, initialVelocity: Float, block: (Animatable<Float, AnimationVector1D>.() -> Unit)?): AnimationResult<Float, AnimationVector1D>' can only be called from a coroutine or another suspend function.
FAILURE: Build failed with an exception.
```

## app tests: failure detail

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/design/MonaTheme.kt:142:9 Argument type mismatch: actual type is 'Unit', but 'ProvidedValue<*>' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/design/MonaTheme.kt:142:20 No value passed for parameter 'content'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:141:42 Unresolved reference 'pressure' on receiver of type 'HistoricalChange'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:142:45 Unresolved reference 'pressure' on receiver of type 'HistoricalChange'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:142:57 Unresolved reference 'orientation' on receiver of type 'HistoricalChange'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:142:72 Unresolved reference 'type' on receiver of type 'HistoricalChange'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:143:45 Unresolved reference 'orientation' on receiver of type 'HistoricalChange'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:278:33 Unresolved reference 'orientation'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:279:23 Unresolved reference 'orientation'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:287:42 Unresolved reference 'attach' on receiver of type 'CanvasInputHandler'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/domain/tool/Tool.kt:10:49 Unresolved reference 'FormatPaintOff'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/domain/tool/Tool.kt:53:39 Unresolved reference 'FormatPaintOff'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:267:21 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:268:22 Cannot infer type for type parameter 'R'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:271:46 Unresolved reference 'asImageBitmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:276:26 Argument type mismatch: actual type is 'Any', but 'ImageBitmap' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/EditorViewModel.kt:40:39 Unresolved reference 'Tool'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/EditorViewModel.kt:345:9 'when' expression must be exhaustive. Add the 'MagicWand', 'ShowSelection', 'Liquify', 'Crop' branches or an 'else' branch.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:205:85 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:286:35 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:287:35 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:552:122 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:555:112 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:58:26 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:96:60 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:103:59 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:110:60 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:169:60 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:219:62 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/home/HomeScreen.kt:403:32 Unresolved reference 'collectAsStateWithLifecycle'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:153:86 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:166:90 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:226:114 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:229:74 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:271:26 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:301:43 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/settings/SettingsScreen.kt:309:32 Unresolved reference 'collectAsStateWithLifecycle'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:84:9 Functions which invoke @Composable functions must be marked with the @Composable annotation
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:85:9 @Composable invocations can only happen from the context of a @Composable function
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:488:22 Functions which invoke @Composable functions must be marked with the @Composable annotation
```

## assembleDebug: failure detail

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/design/MonaTheme.kt:142:9 Argument type mismatch: actual type is 'Unit', but 'ProvidedValue<*>' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/design/MonaTheme.kt:142:20 No value passed for parameter 'content'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:141:42 Unresolved reference 'pressure' on receiver of type 'HistoricalChange'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:142:45 Unresolved reference 'pressure' on receiver of type 'HistoricalChange'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:142:57 Unresolved reference 'orientation' on receiver of type 'HistoricalChange'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:142:72 Unresolved reference 'type' on receiver of type 'HistoricalChange'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:143:45 Unresolved reference 'orientation' on receiver of type 'HistoricalChange'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:278:33 Unresolved reference 'orientation'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:279:23 Unresolved reference 'orientation'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:287:42 Unresolved reference 'attach' on receiver of type 'CanvasInputHandler'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/domain/tool/Tool.kt:10:49 Unresolved reference 'FormatPaintOff'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/domain/tool/Tool.kt:53:39 Unresolved reference 'FormatPaintOff'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:267:21 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:268:22 Cannot infer type for type parameter 'R'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:271:46 Unresolved reference 'asImageBitmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:276:26 Argument type mismatch: actual type is 'Any', but 'ImageBitmap' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/EditorViewModel.kt:40:39 Unresolved reference 'Tool'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/EditorViewModel.kt:345:9 'when' expression must be exhaustive. Add the 'MagicWand', 'ShowSelection', 'Liquify', 'Crop' branches or an 'else' branch.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:205:85 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:286:35 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:287:35 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:552:122 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:555:112 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:58:26 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:96:60 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:103:59 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:110:60 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:169:60 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:219:62 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/home/HomeScreen.kt:403:32 Unresolved reference 'collectAsStateWithLifecycle'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:153:86 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:166:90 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:226:114 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:229:74 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:271:26 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:301:43 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/settings/SettingsScreen.kt:309:32 Unresolved reference 'collectAsStateWithLifecycle'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:84:9 Functions which invoke @Composable functions must be marked with the @Composable annotation
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:85:9 @Composable invocations can only happen from the context of a @Composable function
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:488:22 Functions which invoke @Composable functions must be marked with the @Composable annotation
```

## release: failure detail

```
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/design/MonaTheme.kt:142:9 Argument type mismatch: actual type is 'Unit', but 'ProvidedValue<*>' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/design/MonaTheme.kt:142:20 No value passed for parameter 'content'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:141:42 Unresolved reference 'pressure' on receiver of type 'HistoricalChange'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:142:45 Unresolved reference 'pressure' on receiver of type 'HistoricalChange'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:142:57 Unresolved reference 'orientation' on receiver of type 'HistoricalChange'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:142:72 Unresolved reference 'type' on receiver of type 'HistoricalChange'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:143:45 Unresolved reference 'orientation' on receiver of type 'HistoricalChange'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:278:33 Unresolved reference 'orientation'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:279:23 Unresolved reference 'orientation'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/input/CanvasInput.kt:287:42 Unresolved reference 'attach' on receiver of type 'CanvasInputHandler'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/domain/tool/Tool.kt:10:49 Unresolved reference 'FormatPaintOff'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/domain/tool/Tool.kt:53:39 Unresolved reference 'FormatPaintOff'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:267:21 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:268:22 Cannot infer type for type parameter 'R'. Specify it explicitly.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:271:46 Unresolved reference 'asImageBitmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/brushstudio/BrushStudioScreen.kt:276:26 Argument type mismatch: actual type is 'Any', but 'ImageBitmap' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/EditorViewModel.kt:40:39 Unresolved reference 'Tool'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/EditorViewModel.kt:345:9 'when' expression must be exhaustive. Add the 'MagicWand', 'ShowSelection', 'Liquify', 'Crop' branches or an 'else' branch.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:205:85 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:286:35 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:287:35 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:552:122 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Panels.kt:555:112 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:58:26 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:96:60 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:103:59 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:110:60 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:169:60 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/editor/Sheets.kt:219:62 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/home/HomeScreen.kt:403:32 Unresolved reference 'collectAsStateWithLifecycle'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:153:86 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:166:90 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:226:114 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:229:74 Argument type mismatch: actual type is '() -> Unit', but 'Color?' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:271:26 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/newcanvas/NewCanvasScreen.kt:301:43 Unresolved reference 'Column'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/presentation/settings/SettingsScreen.kt:309:32 Unresolved reference 'collectAsStateWithLifecycle'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:84:9 Functions which invoke @Composable functions must be marked with the @Composable annotation
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:85:9 @Composable invocations can only happen from the context of a @Composable function
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:488:22 Functions which invoke @Composable functions must be marked with the @Composable annotation
```

## assembleDebug: log tail

```
> Task :app:compileDebugKotlin FAILED
> Task :app:mergeExtDexDebug
> Task :app:mergeLibDexDebug
> Task :app:stripDebugDebugSymbols
Unable to strip the following libraries, packaging them as they are: libandroidx.graphics.path.so, libdatastore_shared_counter.so. Run with --info option to learn more.
[Incubating] Problems report is available at: file:///home/runner/work/Mona_Art/Mona_Art/build/reports/problems/problems-report.html
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':app:compileDebugKotlin' (registered by plugin 'com.android.internal.application').
> A failure occurred while executing org.jetbrains.kotlin.compilerRunner.btapi.BuildToolsApiCompilationWork
   > Kotlin compiler: ARGUMENT_TYPE_MISMATCH
       Kotlin compiler error
         Argument type mismatch: actual type is 'Unit', but 'ProvidedValue<*>' was expected.
         Location: /home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/design/MonaTheme.kt:142
* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights from a Build Scan (powered by Develocity).
> Get more help at https://help.gradle.org.
Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.
You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.
For more on this, please refer to https://docs.gradle.org/9.8.2/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.
BUILD FAILED in 1m 36s
31 actionable tasks: 11 executed, 20 up-to-date
Configuration cache entry stored.
```

## release: log tail

```
  No value passed for parameter 'size'.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:503:58 Function invocation 'width(...)' expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:744:14 Suspend function 'suspend fun animateTo(targetValue: Float, animationSpec: AnimationSpec<Float>, initialVelocity: Float, block: (Animatable<Float, AnimationVector1D>.() -> Unit)?): AnimationResult<Float, AnimationVector1D>' can only be called from a coroutine or another suspend function.
e: file:///home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/ui/components/Components.kt:745:14 Suspend function 'suspend fun animateTo(targetValue: Float, animationSpec: AnimationSpec<Float>, initialVelocity: Float, block: (Animatable<Float, AnimationVector1D>.() -> Unit)?): AnimationResult<Float, AnimationVector1D>' can only be called from a coroutine or another suspend function.
> Task :app:compileReleaseKotlin FAILED
[Incubating] Problems report is available at: file:///home/runner/work/Mona_Art/Mona_Art/build/reports/problems/problems-report.html
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':app:compileReleaseKotlin' (registered by plugin 'com.android.internal.application').
> A failure occurred while executing org.jetbrains.kotlin.compilerRunner.btapi.BuildToolsApiCompilationWork
   > Kotlin compiler: ARGUMENT_TYPE_MISMATCH
       Kotlin compiler error
         Argument type mismatch: actual type is 'Unit', but 'ProvidedValue<*>' was expected.
         Location: /home/runner/work/Mona_Art/Mona_Art/app/src/main/java/com/monaart/studio/core/design/MonaTheme.kt:142
* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights from a Build Scan (powered by Develocity).
> Get more help at https://help.gradle.org.
Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.
You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.
For more on this, please refer to https://docs.gradle.org/9.8.2/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.
BUILD FAILED in 26s
39 actionable tasks: 32 executed, 5 from cache, 2 up-to-date
Configuration cache entry stored.
```
