# Test report

Commit: `81b6b091c6341ccc299975fc255dea009aa099ef`

## Failures
```
BrushEngineTest > sparse samples are interpolated without gaps FAILED
    java.lang.AssertionError at BrushEngineTest.kt:81
DocumentTest > clipping mask limits a layer to the base alpha FAILED
    java.lang.AssertionError at DocumentTest.kt:107
DocumentTest > group visibility hides its children FAILED
    java.lang.IllegalStateException at DocumentTest.kt:29
PersistenceTest > every export format produces a non-empty payload FAILED
    java.lang.ArrayIndexOutOfBoundsException at PersistenceTest.kt:184
```

## Test summary
```
31 tests completed, 4 failed
BUILD FAILED in 2m 36s
```

Exit codes: tests=1 selfcheck=0

# Engine self-check report

Generated: 2026-11-05T07:45:11.885551726Z

| Check | Result | Detail |
|---|---|---|
| blend.normal_passthrough | PASS | kotlin.Unit (6ms) |
| blend.multiply_reference | PASS | kotlin.Unit (0ms) |
| blend.all_modes_defined | PASS | 17 modes (12ms) |
| blend.composite_alpha_math | PASS | kotlin.Unit (0ms) |
| brush.pressure_affects_geometry | PASS | radius 1.51 (30%%) vs 4.50 (100%%) (27ms) |
| brush.spacing_emits_dabs | PASS | 396 dabs over 200px (4ms) |
| brush.stroke_paints_pixels | PASS | alpha at centre 255 (25ms) |
| brush.eraser_removes_alpha | PASS | alpha after erase 0 (16ms) |
| brush.all_presets_render | PASS | 30 presets (442ms) |
| brush.sparse_samples_have_no_gaps | PASS | 907 dabs, max gap 0.50px, 4px tall stroke (22ms) |
| history.undo_redo_restores_pixels | PASS | undo/redo ok (2ms) |
| history.layer_ops_undoable | PASS | ok (10ms) |
| io.project_roundtrip | PASS | format v3, 36 KiB (106ms) |
| io.corruption_detected | PASS | detected (1ms) |
| io.png_roundtrip | PASS | png 287B (6ms) |
| io.all_export_formats_produce_bytes | PASS | MONA=2419 PNG=467 JPEG=2349 TIFF=120154 PSD=210164 PDF=926 (210ms) |
| io.psd_has_layers | PASS | 2 layers (4ms) |
| compositor.layer_order | PASS | order ok (2ms) |
| compositor.opacity_and_blend | PASS | ok (0ms) |
| compositor.dirty_region_only | PASS | 0.00 ms per 256px region on 2048² canvas (4ms) |
| filters.all_apply | PASS | 16 filters (45ms) |
| selection.rectangle_limits_fill | PASS | ok (0ms) |
| selection.magic_wand | PASS | bounds RectI(left=5, top=5, right=15, bottom=15) (5ms) |
| viewport.roundtrip_and_zoom | PASS | ok (0ms) |
| perf.stroke_throughput | PASS | 0.50 ms per stylus event (2013 events/s) (298ms) |
| perf.large_canvas_memory | PASS | stroke on 4096² in 192 ms, 2816 KiB of tiles used (192ms) |
| perf.save_large_project | PASS | 2048² save 63 ms (0.1 MiB), load 37 ms (104ms) |

**27/27 checks passed.**
