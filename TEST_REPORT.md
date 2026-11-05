# Test report

Commit: `6dc5402bd881a985c2780aa438ca70729aea72aa`

## Failures
```
BrushEngineTest > sparse samples are interpolated without gaps FAILED
    java.lang.AssertionError at BrushEngineTest.kt:81
PerformanceTest > stroke throughput stays within budget FAILED
    java.lang.AssertionError at PerformanceTest.kt:52
```

## Test summary
```
34 tests completed, 1 failed
13 tests completed, 1 failed
BUILD FAILED in 3m 38s
```

Exit codes: tests=1 selfcheck=0

# Engine self-check report

Generated: 2026-11-05T08:11:13.652273755Z

| Check | Result | Detail |
|---|---|---|
| blend.normal_passthrough | PASS | kotlin.Unit (11ms) |
| blend.multiply_reference | PASS | kotlin.Unit (0ms) |
| blend.all_modes_defined | PASS | 17 modes (12ms) |
| blend.composite_alpha_math | PASS | kotlin.Unit (0ms) |
| brush.pressure_affects_geometry | PASS | radius 1.51 (30%%) vs 4.50 (100%%) (30ms) |
| brush.spacing_emits_dabs | PASS | 396 dabs over 200px (3ms) |
| brush.stroke_paints_pixels | PASS | alpha at centre 255 (22ms) |
| brush.eraser_removes_alpha | PASS | alpha after erase 0 (17ms) |
| brush.all_presets_render | PASS | 30 presets (422ms) |
| brush.sparse_samples_have_no_gaps | PASS | 907 dabs, max gap 0.50px, 4px tall stroke (18ms) |
| history.undo_redo_restores_pixels | PASS | undo/redo ok (0ms) |
| history.layer_ops_undoable | PASS | ok (10ms) |
| io.project_roundtrip | PASS | format v3, 35 KiB (96ms) |
| io.corruption_detected | PASS | detected (1ms) |
| io.png_roundtrip | PASS | png 287B (5ms) |
| io.all_export_formats_produce_bytes | PASS | MONA=2419 PNG=467 JPEG=1461 TIFF=120154 PSD=210164 PDF=926 (203ms) |
| io.psd_has_layers | PASS | 2 layers (3ms) |
| compositor.layer_order | PASS | order ok (2ms) |
| compositor.opacity_and_blend | PASS | ok (0ms) |
| compositor.dirty_region_only | PASS | 0.00 ms per 256px region on 2048² canvas (4ms) |
| filters.all_apply | PASS | 16 filters (40ms) |
| selection.rectangle_limits_fill | PASS | ok (0ms) |
| selection.magic_wand | PASS | bounds RectI(left=5, top=5, right=15, bottom=15) (4ms) |
| viewport.roundtrip_and_zoom | PASS | ok (0ms) |
| perf.stroke_throughput | PASS | 0.94 ms per stylus event (1058 events/s) (567ms) |
| perf.large_canvas_memory | PASS | stroke on 4096² in 106 ms, 2816 KiB of tiles used (105ms) |
| perf.save_large_project | PASS | 2048² save 65 ms (0.1 MiB), load 37 ms (109ms) |

**27/27 checks passed.**
