# Engine self-check report

Generated: 2026-11-05T15:02:41.699863529Z

| Check | Result | Detail |
|---|---|---|
| blend.normal_passthrough | PASS | kotlin.Unit (4ms) |
| blend.multiply_reference | PASS | kotlin.Unit (0ms) |
| blend.all_modes_defined | PASS | 17 modes (6ms) |
| blend.composite_alpha_math | PASS | kotlin.Unit (0ms) |
| brush.pressure_affects_geometry | PASS | radius 1.51 (30%%) vs 4.50 (100%%) (22ms) |
| brush.spacing_emits_dabs | PASS | 396 dabs over 200px (3ms) |
| brush.stroke_paints_pixels | PASS | alpha at centre 255 (17ms) |
| brush.eraser_removes_alpha | PASS | alpha after erase 0 (10ms) |
| brush.all_presets_render | PASS | 30 presets (264ms) |
| brush.sparse_samples_have_no_gaps | PASS | 907 dabs, max gap 0.50px, 4px tall stroke (22ms) |
| history.undo_redo_restores_pixels | PASS | undo/redo ok (1ms) |
| history.layer_ops_undoable | PASS | ok (17ms) |
| io.project_roundtrip | PASS | format v3, 36 KiB (80ms) |
| io.corruption_detected | PASS | detected (0ms) |
| io.png_roundtrip | PASS | png 287B (3ms) |
| io.all_export_formats_produce_bytes | PASS | MONA=2419 PNG=467 JPEG=1461 TIFF=120154 PSD=210164 PDF=926 (172ms) |
| io.psd_has_layers | PASS | 2 layers (1ms) |
| compositor.layer_order | PASS | order ok (3ms) |
| compositor.opacity_and_blend | PASS | ok (0ms) |
| compositor.dirty_region_only | PASS | 0.00 ms per 256px region on 2048² canvas (2ms) |
| filters.all_apply | PASS | 16 filters (43ms) |
| selection.rectangle_limits_fill | PASS | ok (1ms) |
| selection.magic_wand | PASS | bounds RectI(left=5, top=5, right=15, bottom=15) (6ms) |
| viewport.roundtrip_and_zoom | PASS | ok (0ms) |
| perf.stroke_throughput | PASS | 0.18 ms per stylus event (5485 events/s) (109ms) |
| perf.large_canvas_memory | PASS | stroke on 4096² in 222 ms, 2816 KiB of tiles used (222ms) |
| perf.save_large_project | PASS | 2048² save 90 ms (0.1 MiB), load 40 ms (225ms) |

**27/27 checks passed.**
