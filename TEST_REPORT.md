# Test report

Owner: unassigned — historical report, not authoritative for the current tree.

Commit: `b7c36f61f9298347b8f7844603a27026d04a2bd1`

## Failures
```
```

## Test summary
```
BUILD SUCCESSFUL in 2m 41s
```

Exit codes: tests=0 selfcheck=0

# Engine self-check report

Generated: 2026-11-05T08:18:17.618718745Z

| Check | Result | Detail |
|---|---|---|
| blend.normal_passthrough | PASS | kotlin.Unit (5ms) |
| blend.multiply_reference | PASS | kotlin.Unit (0ms) |
| blend.all_modes_defined | PASS | 17 modes (10ms) |
| blend.composite_alpha_math | PASS | kotlin.Unit (0ms) |
| brush.pressure_affects_geometry | PASS | radius 1.51 (30%%) vs 4.50 (100%%) (21ms) |
| brush.spacing_emits_dabs | PASS | 396 dabs over 200px (4ms) |
| brush.stroke_paints_pixels | PASS | alpha at centre 255 (23ms) |
| brush.eraser_removes_alpha | PASS | alpha after erase 0 (15ms) |
| brush.all_presets_render | PASS | 30 presets (443ms) |
| brush.sparse_samples_have_no_gaps | PASS | 907 dabs, max gap 0.50px, 4px tall stroke (21ms) |
| history.undo_redo_restores_pixels | PASS | undo/redo ok (0ms) |
| history.layer_ops_undoable | PASS | ok (8ms) |
| io.project_roundtrip | PASS | format v3, 36 KiB (104ms) |
| io.corruption_detected | PASS | detected (1ms) |
| io.png_roundtrip | PASS | png 287B (6ms) |
| io.all_export_formats_produce_bytes | PASS | MONA=2419 PNG=467 JPEG=1461 TIFF=120154 PSD=210164 PDF=926 (202ms) |
| io.psd_has_layers | PASS | 2 layers (2ms) |
| compositor.layer_order | PASS | order ok (2ms) |
| compositor.opacity_and_blend | PASS | ok (0ms) |
| compositor.dirty_region_only | PASS | 0.01 ms per 256px region on 2048² canvas (4ms) |
| filters.all_apply | PASS | 16 filters (46ms) |
| selection.rectangle_limits_fill | PASS | ok (0ms) |
| selection.magic_wand | PASS | bounds RectI(left=5, top=5, right=15, bottom=15) (5ms) |
| viewport.roundtrip_and_zoom | PASS | ok (0ms) |
| perf.stroke_throughput | PASS | 0.86 ms per stylus event (1168 events/s) (514ms) |
| perf.large_canvas_memory | PASS | stroke on 4096² in 175 ms, 2816 KiB of tiles used (175ms) |
| perf.save_large_project | PASS | 2048² save 63 ms (0.1 MiB), load 33 ms (101ms) |

**27/27 checks passed.**
