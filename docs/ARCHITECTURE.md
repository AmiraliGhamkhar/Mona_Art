# Mona Art — Architecture

This document describes the system as it is implemented. Every component named
here corresponds to a file in the repository; nothing is aspirational.

## 1. Modules

```
:engine   pure JVM, no Android dependency. Kotlin/JVM, toolchain 17.
:app      Android application. Compose UI, Material 3, minSdk 26, target/compileSdk 37.
```

`:app` depends on `:engine`; the reverse dependency does not exist and is
enforced by `:engine` being a plain `kotlin.jvm` module with no Android plugin.
This is the single most important structural rule in the project: it is what
lets the paint core be unit-tested and benchmarked on a JVM in seconds, with no
emulator.

Dependency direction inside `:app`:

```
presentation  ->  domain  <-  data  ->  engine
     |             ^
     +-------------+
core.input / core.design are leaf packages used by presentation.
app (MonaArtApp, MainActivity) wires everything.
```

`domain` holds plain models (`Project.kt`, `Settings.kt`, `Tool.kt`) and depends
on nothing but `:engine` types. `presentation` never touches `java.io` or
`ContentResolver` directly except through `data`.

## 2. The manual dependency container

`app/src/main/java/com/monaart/studio/app/MonaArtApp.kt`

`MonaArtApp : Application` owns four singletons:

| Singleton | Type | Responsibility |
|---|---|---|
| `index` | `ProjectIndex` | JSON project index + custom canvas presets, atomic writes |
| `settings` | `SettingsStore` | DataStore-backed user preferences |
| `projects` | `ProjectRepository` | Create/save/load/trash/import of `.mona` documents |
| `brushes` | `BrushLibraryRepository` | Built-in + user brush presets |

There is deliberately no DI framework. Eight singletons with obvious lifetimes
do not justify Hilt; the graph is readable in one 50-line file.

`MonaViewModel` receives the container through
`ViewModelProvider.Factory` (`monaViewModelFactory` in `MainActivity.kt`). The
`Application` comes from `AndroidViewModelFactory.APPLICATION_KEY` and is
narrowed with `as? MonaArtApp`. `MonaArtApp.instance` exists only as a fallback
for tests that construct the ViewModel without an Activity.

**Invariant:** nothing may construct a detached `Application()`. `contentResolver`
and `cacheDir` do not exist on a bare instance, and that was a real crash on the
incoming-file path.

## 3. Document lifecycle

```
create  NewCanvasScreen -> MonaViewModel.createDocument(spec) -> ProjectRepository.create
open    WorkspaceRoute  -> EditorViewModel.open(projectId)    -> ProjectRepository.load
edit    EditorViewModel mutates the ArtDocument in place
save    autosave loop / closeEditor / explicit save -> ProjectRepository.save
```

`ArtDocument` (`engine/.../document/ArtDocument.kt`) is mutable and owns a
`List<Layer>`. Each raster `Layer` owns a `TileSurface` — a sparse map of
`tileSize × tileSize` `IntArray` tiles keyed by a packed `Long`. Unpainted
regions allocate nothing, which is why a 4096² document with a single stroke
reports ~2.8 MiB of tiles rather than 64 MiB.

`ProjectCodec` serialises to `.mona`: a ZIP with a JSON manifest, DEFLATE-compressed
tile payloads and a SHA-256 over the archive. `ProjectRepository` writes
atomically and keeps a `.bak` plus a `recovery.mona` snapshot, so a crash mid-save
cannot lose the previous good file.

## 4. Input pipeline

Raw stylus data must survive, so the canvas does **not** use Compose's normalised
pointer stream.

```
MotionEvent
  -> MonaSurfaceView            (android.view.View inside the Compose tree)
  -> CanvasInputHandler         (interface)
  -> CanvasInput                (pointer classification, palm rejection, gestures,
                                 historical-sample flattening)
  -> EditorViewModel            (tool dispatch, document coordinates)
  -> BrushEngine                (dab geometry, spacing, symmetry)
  -> StrokeRasterizer           (dab rasterisation into tiles)
```

`MonaSurfaceView` calls `requestUnbufferedDispatch(SOURCE_CLASS_POINTER)` so
events are not batched. `CanvasInput` expands `getHistoricalX/Y`, because a fast
stroke delivers most of its pressure samples as historical events inside a
single dispatch; dropping them is what makes strokes look polygonal.

`StylusInputTest` drives synthetic `MotionEvent` streams through `CanvasInput`
and asserts pressure, tilt, tool type, palm window and gesture routing. It does
not prove anything about physical digitizers — see the hardware caveat in
`IMPLEMENTATION_STATUS.md`.

## 5. Rendering pipeline

```
ArtDocument -> Compositor.render(region) -> IntArray (straight/premultiplied ARGB)
            -> CanvasRenderer            -> android.graphics.Bitmap
            -> Compose Image / Canvas blit, one dirty region per frame
```

`Compositor` walks layers bottom-up applying visibility, opacity, alpha lock,
clipping masks, group isolation and blend mode. It renders only the requested
rect, so interactive tools (transform, liquify) repaint a bounded box.
`compositor.dirty_region_only` in the self-check measures this.

Clipping masks follow Photoshop semantics: the base layer is rendered in
isolation as the mask silhouette, its opacity applied once, then the clipped
layer's pixels are Normal-composited through it.

The bitmap upload still happens on the UI thread. That is a measured limitation
on 4K canvases, not an oversight — see `IMPLEMENTATION_STATUS.md` L1.

## 6. Undo / redo

`engine/.../history/History.kt` holds command entries; `PixelEdit` captures the
pre-edit tile contents of a layer so an undo restores bytes rather than
replaying intent. Entry count and byte budget are bounded.

Two rules matter:

- **One gesture = one entry.** A stroke creates one `PixelEdit` at `begin()` and
  commits at `end()`, no matter how many flushes happened in between. Symmetry
  strokes, transform drags and liquify pushes all follow this.
- **Previews never enter history.** `Filters.apply(..., recordHistory = false)`
  mutates pixels without recording. See next section.

## 7. Filter previews

`EditorViewModel` owns the preview state machine:

1. `beginFilterPreview` takes an exact tile-level backup of the layer.
2. `schedulePreview` debounces, cancels the previous job, restores the backup,
   then applies the filter with `recordHistory = false` on `Dispatchers.Default`.
3. A generation counter guards the repaint, so a superseded preview cannot draw
   over a newer result.
4. `cancelFilterPreview` restores the backup verbatim; the undo stack is untouched.
5. `applyFilterPreview` restores the backup and applies once with
   `recordHistory = true`, producing exactly one undoable step.

`forceCancelPreview` is the synchronous variant used when a stroke starts while
a preview is live, so painting never races an abandoned coroutine.

## 8. Import / export

Import: `ACTION_VIEW` -> `MainActivity.handleIntent` -> `MonaViewModel.handleIncomingFile`
-> bounded read (`IncomingFiles.MAX_FILE_BYTES`) -> `.mona` through
`ProjectRepository.importProject`, images through `IncomingFiles.decodeImage`
which inspects bounds first and downsamples to a pixel budget. The temp file is
created with `File.createTempFile` in `cacheDir` and deleted in `finally`.

Export: the Export sheet builds an `ExportRequest(format, scale, quality)` which
is passed unchanged to `EditorViewModel.exportToUri`. `DocumentExport` renders
through the same `Compositor` as the on-screen canvas, so exports match what the
artist sees. Formats: MONA, PNG, JPEG, TIFF, PSD, PDF.

`PsdWriter` produces planar per-channel layer data (R, G, B, alpha as separate
planes, in the order the layer record declares), a `luni` Unicode name block
plus a Pascal name, and a raw uncompressed composite. Its structure is pinned by
`PsdStructureTest`, which parses the bytes independently of the writer.

## 9. Testing strategy

| Layer | Where | What it proves |
|---|---|---|
| Engine unit tests | `engine/src/test` (JUnit) | Pixel-level behaviour of brush, compositor, crop, filters, persistence, PSD structure |
| Engine self-check | `:engine:selfCheck`, `diag/SelfCheck.kt` | 27 end-to-end engine checks that exit non-zero on any failure; also the perf regression guards |
| App unit tests | `app/src/test` (JUnit + Robolectric) | Stylus input routing, stroke performance budget |
| Instrumented | `app/src/androidTest` | `EditorSmokeTest` (paint -> save -> reload on a real process) and `StudioLaunchTest` (real Activity, real Compose tree) |
| Static | `detekt`, `ktlintCheck`, `:app:lintDebug` | Style, smells, Android API correctness |

The distinction between the two instrumented tests matters: `EditorSmokeTest`
drives the engine and repository directly while an Activity happens to be open.
That proves the paint core works on a device. It does not prove the UI renders —
`StudioLaunchTest` covers that, and touch-driven painting through the real UI
still needs an emulator.

## 10. CI

`.github/workflows/android.yml` runs nine steps and records each one's exit code
independently, so a single failure cannot be masked by a later success:

```
:engine:selfCheck  :engine:test  :app:testDebugUnitTest  :app:assembleDebug
:app:assembleRelease :app:bundleRelease  :app:lintDebug  detekt ktlintCheck
:app:assembleDebugAndroidTest  :engine:koverHtmlReport :engine:koverXmlReport
```

Runner logs are not readable from every environment that has to act on a
failure, so a failing run also posts the raw Gradle tail of every non-zero step
to one persistent issue per branch titled `CI diagnostics: <branch>`.

`.github/workflows/format.yml` is opt-in (a `[format]` marker in the head commit
message). It runs `ktlintFormat` and `detektBaseline` — neither can be produced
without a full JVM + Android SDK + Gradle toolchain — reports the diff, and
commits. Its own commit carries neither marker, so it cannot retrigger itself.

Detekt's baseline is **per module** (`app/config/detekt/baseline.xml`,
`engine/config/detekt/baseline.xml`). `build.maxIssues` stays at detekt's default
of 0, so anything introduced after the baseline was seeded still fails.
