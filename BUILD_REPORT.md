# Build report

Generated: 2026-11-05T07:09:00.861760+00:00

Commit: `cb46eb71daa1585838689552e8f8e2a21179433d`

## Step status

| Step | Exit code | Result |
|---|---|---|
| engine_selfcheck | 0 | PASS |
| engine_unit_tests | 1 | FAIL |
| app_unit_tests | 0 | PASS |
| assemble_debug | 0 | PASS |
| assemble_release | 0 | PASS |
| lint | 0 | PASS |

## Engine self-check

# Engine self-check report

Generated: 2026-11-05T07:04:00.547486080Z

| Check | Result | Detail |
|---|---|---|
| blend.normal_passthrough | PASS | kotlin.Unit (3ms) |
| blend.multiply_reference | PASS | kotlin.Unit (0ms) |
| blend.all_modes_defined | PASS | 17 modes (7ms) |
| blend.composite_alpha_math | PASS | kotlin.Unit (0ms) |
| brush.pressure_affects_geometry | PASS | radius 1.51 (30%%) vs 4.50 (100%%) (16ms) |
| brush.spacing_emits_dabs | PASS | 396 dabs over 200px (2ms) |
| brush.stroke_paints_pixels | PASS | alpha at centre 255 (14ms) |
| brush.eraser_removes_alpha | PASS | alpha after erase 0 (10ms) |
| brush.all_presets_render | PASS | 30 presets (290ms) |
| brush.sparse_samples_have_no_gaps | PASS | 907 dabs, max gap 0.50px, 4px tall stroke (15ms) |
| history.undo_redo_restores_pixels | PASS | undo/redo ok (2ms) |
| history.layer_ops_undoable | PASS | ok (5ms) |
| io.project_roundtrip | PASS | format v3, 36 KiB (64ms) |
| io.corruption_detected | PASS | detected (1ms) |
| io.png_roundtrip | PASS | png 287B (5ms) |
| io.all_export_formats_produce_bytes | PASS | MONA=2419 PNG=467 JPEG=2349 TIFF=120154 PSD=210164 PDF=926 (125ms) |
| io.psd_has_layers | PASS | 2 layers (1ms) |
| compositor.layer_order | PASS | order ok (1ms) |
| compositor.opacity_and_blend | PASS | ok (0ms) |
| compositor.dirty_region_only | PASS | 0.00 ms per 256px region on 2048² canvas (3ms) |
| filters.all_apply | PASS | 16 filters (29ms) |
| selection.rectangle_limits_fill | PASS | ok (0ms) |
| selection.magic_wand | PASS | bounds RectI(left=5, top=5, right=15, bottom=15) (3ms) |
| viewport.roundtrip_and_zoom | PASS | ok (0ms) |
| perf.stroke_throughput | PASS | 0.29 ms per stylus event (3483 events/s) (172ms) |
| perf.large_canvas_memory | PASS | stroke on 4096² in 199 ms, 2816 KiB of tiles used (199ms) |
| perf.save_large_project | PASS | 2048² save 52 ms (0.1 MiB), load 46 ms (111ms) |

**27/27 checks passed.**

## Artifacts

- `app/build/outputs/apk/debug/app-debug.apk` — 19.5 MiB
- `app/build/outputs/apk/release/app-release.apk` — 1.7 MiB
- `app/build/outputs/bundle/release/app-release.aab` — 4.2 MiB

## engine tests: compiler errors (19)

| File | Errors |
|---|---|
| `DocumentTest` | 16 |
| `PersistenceTest` | 3 |

```
e: file:///home/runner/work/Mona_Art/Mona_Art/engine/src/test/kotlin/com/monaart/engine/document/DocumentTest.kt:105:28 Unresolved reference 'getPixel' on receiver of type 'Pixmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/engine/src/test/kotlin/com/monaart/engine/document/DocumentTest.kt:105:53 None of the following candidates is applicable:
e: file:///home/runner/work/Mona_Art/Mona_Art/engine/src/test/kotlin/com/monaart/engine/document/DocumentTest.kt:106:81 'operator' modifier is required on 'fun <T> Comparable<T>.compareTo(other: T): Int'.
e: file:///home/runner/work/Mona_Art/Mona_Art/engine/src/test/kotlin/com/monaart/engine/document/DocumentTest.kt:107:94 Unresolved reference 'getPixel' on receiver of type 'Pixmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/engine/src/test/kotlin/com/monaart/engine/document/DocumentTest.kt:166:84 Unresolved reference 'getPixel' on receiver of type 'Pixmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/engine/src/test/kotlin/com/monaart/engine/document/DocumentTest.kt:167:83 Unresolved reference 'getPixel' on receiver of type 'Pixmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/engine/src/test/kotlin/com/monaart/engine/document/DocumentTest.kt:180:46 Unresolved reference 'getPixel' on receiver of type 'Pixmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/engine/src/test/kotlin/com/monaart/engine/document/DocumentTest.kt:214:50 Unresolved reference 'getPixel' on receiver of type 'Pixmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/engine/src/test/kotlin/com/monaart/engine/document/DocumentTest.kt:219:92 Unresolved reference 'getPixel' on receiver of type 'Pixmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/engine/src/test/kotlin/com/monaart/engine/document/DocumentTest.kt:49:87 Unresolved reference 'getPixel' on receiver of type 'Pixmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/engine/src/test/kotlin/com/monaart/engine/document/DocumentTest.kt:62:46 Unresolved reference 'getPixel' on receiver of type 'Pixmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/engine/src/test/kotlin/com/monaart/engine/document/DocumentTest.kt:75:24 Unresolved reference 'getPixel' on receiver of type 'Pixmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/engine/src/test/kotlin/com/monaart/engine/document/DocumentTest.kt:75:49 None of the following candidates is applicable:
e: file:///home/runner/work/Mona_Art/Mona_Art/engine/src/test/kotlin/com/monaart/engine/document/DocumentTest.kt:90:24 Unresolved reference 'getPixel' on receiver of type 'Pixmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/engine/src/test/kotlin/com/monaart/engine/document/DocumentTest.kt:90:49 None of the following candidates is applicable:
e: file:///home/runner/work/Mona_Art/Mona_Art/engine/src/test/kotlin/com/monaart/engine/document/DocumentTest.kt:91:62 'operator' modifier is required on 'fun <T> Comparable<T>.compareTo(other: T): Int'.
e: file:///home/runner/work/Mona_Art/Mona_Art/engine/src/test/kotlin/com/monaart/engine/io/PersistenceTest.kt:119:60 None of the following candidates is applicable:
e: file:///home/runner/work/Mona_Art/Mona_Art/engine/src/test/kotlin/com/monaart/engine/io/PersistenceTest.kt:119:75 Unresolved reference 'listFiles'.
e: file:///home/runner/work/Mona_Art/Mona_Art/engine/src/test/kotlin/com/monaart/engine/io/PersistenceTest.kt:119:96 Unresolved reference 'it'.
```

## engine tests: failure detail

```
e: file:///home/runner/work/Mona_Art/Mona_Art/engine/src/test/kotlin/com/monaart/engine/document/DocumentTest.kt:49:87 Unresolved reference 'getPixel' on receiver of type 'Pixmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/engine/src/test/kotlin/com/monaart/engine/document/DocumentTest.kt:62:46 Unresolved reference 'getPixel' on receiver of type 'Pixmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/engine/src/test/kotlin/com/monaart/engine/document/DocumentTest.kt:75:24 Unresolved reference 'getPixel' on receiver of type 'Pixmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/engine/src/test/kotlin/com/monaart/engine/document/DocumentTest.kt:75:49 None of the following candidates is applicable:
fun and(p0: BigInteger!): BigInteger!:
  Argument type mismatch: actual type is 'Int', but 'BigInteger!' was expected.
  'infix' modifier is required on 'fun and(p0: BigInteger!): BigInteger!'.
fun BigInteger.and(other: BigInteger): BigInteger:
  Argument type mismatch: actual type is 'Int', but 'BigInteger' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/engine/src/test/kotlin/com/monaart/engine/document/DocumentTest.kt:90:24 Unresolved reference 'getPixel' on receiver of type 'Pixmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/engine/src/test/kotlin/com/monaart/engine/document/DocumentTest.kt:90:49 None of the following candidates is applicable:
fun and(p0: BigInteger!): BigInteger!:
  Argument type mismatch: actual type is 'Int', but 'BigInteger!' was expected.
  'infix' modifier is required on 'fun and(p0: BigInteger!): BigInteger!'.
fun BigInteger.and(other: BigInteger): BigInteger:
  Argument type mismatch: actual type is 'Int', but 'BigInteger' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/engine/src/test/kotlin/com/monaart/engine/document/DocumentTest.kt:91:62 'operator' modifier is required on 'fun <T> Comparable<T>.compareTo(other: T): Int'.
e: file:///home/runner/work/Mona_Art/Mona_Art/engine/src/test/kotlin/com/monaart/engine/document/DocumentTest.kt:105:28 Unresolved reference 'getPixel' on receiver of type 'Pixmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/engine/src/test/kotlin/com/monaart/engine/document/DocumentTest.kt:105:53 None of the following candidates is applicable:
fun and(p0: BigInteger!): BigInteger!:
  Argument type mismatch: actual type is 'Int', but 'BigInteger!' was expected.
  'infix' modifier is required on 'fun and(p0: BigInteger!): BigInteger!'.
fun BigInteger.and(other: BigInteger): BigInteger:
  Argument type mismatch: actual type is 'Int', but 'BigInteger' was expected.
e: file:///home/runner/work/Mona_Art/Mona_Art/engine/src/test/kotlin/com/monaart/engine/document/DocumentTest.kt:106:81 'operator' modifier is required on 'fun <T> Comparable<T>.compareTo(other: T): Int'.
e: file:///home/runner/work/Mona_Art/Mona_Art/engine/src/test/kotlin/com/monaart/engine/document/DocumentTest.kt:107:94 Unresolved reference 'getPixel' on receiver of type 'Pixmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/engine/src/test/kotlin/com/monaart/engine/document/DocumentTest.kt:166:84 Unresolved reference 'getPixel' on receiver of type 'Pixmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/engine/src/test/kotlin/com/monaart/engine/document/DocumentTest.kt:167:83 Unresolved reference 'getPixel' on receiver of type 'Pixmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/engine/src/test/kotlin/com/monaart/engine/document/DocumentTest.kt:180:46 Unresolved reference 'getPixel' on receiver of type 'Pixmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/engine/src/test/kotlin/com/monaart/engine/document/DocumentTest.kt:214:50 Unresolved reference 'getPixel' on receiver of type 'Pixmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/engine/src/test/kotlin/com/monaart/engine/document/DocumentTest.kt:219:92 Unresolved reference 'getPixel' on receiver of type 'Pixmap'.
e: file:///home/runner/work/Mona_Art/Mona_Art/engine/src/test/kotlin/com/monaart/engine/io/PersistenceTest.kt:119:60 None of the following candidates is applicable:
constructor(p0: String!): File:
```

## assembleDebug: log tail

```
> Task :app:processDebugManifest UP-TO-DATE
> Task :app:processDebugManifestForPackage UP-TO-DATE
> Task :app:processDebugResources UP-TO-DATE
> Task :app:mergeDebugJniLibFolders
> Task :app:mergeDebugNativeLibs
> Task :app:dexBuilderDebug
> Task :app:mergeLibDexDebug
> Task :app:validateSigningDebug
> Task :app:writeDebugAppMetadata
> Task :app:writeDebugSigningConfigVersions
> Task :app:mergeExtDexDebug
> Task :app:stripDebugDebugSymbols
> Task :app:mergeProjectDexDebug
> Task :app:stripDebugDebugSymbols
Unable to strip the following libraries, packaging them as they are: libandroidx.graphics.path.so, libdatastore_shared_counter.so. Run with --info option to learn more.
> Task :app:packageDebug
> Task :app:assembleDebug
> Task :app:createDebugApkListingFileRedirect
[Incubating] Problems report is available at: file:///home/runner/work/Mona_Art/Mona_Art/build/reports/problems/problems-report.html
Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.
You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.
For more on this, please refer to https://docs.gradle.org/9.8.2/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.
BUILD SUCCESSFUL in 1m 14s
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
BUILD SUCCESSFUL in 1m 46s
63 actionable tasks: 56 executed, 5 from cache, 2 up-to-date
Configuration cache entry stored.
```
