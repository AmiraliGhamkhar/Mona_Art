# Technical decisions

Each entry records the decision, the alternatives considered, and why the chosen
one won. Decisions that were reversed during development are listed too, because
the reason they failed is the useful part.

## D1. Two modules: pure-JVM `:engine` and Android `:app`

**Decision.** All painting logic (pixels, tiles, brush rasterisation, compositing,
layers, history, filters, codecs, exports) lives in `:engine`, a plain
`kotlin("jvm")` module with **no Android dependencies**. `:app` holds Compose UI,
input plumbing and persistence wiring.

**Why.** The engine runs as a JVM `main` (`:engine:selfCheck`) and under plain
JUnit, so 27 behavioural self-checks and 33 unit tests execute in seconds without
an emulator. Rendering bugs are found by assertions on real pixels, not by
looking at a screen.

**Alternative rejected.** A single Android module with Robolectric for
everything: slower, and it hides accidental Android coupling inside the engine.

## D2. Own rendering engine instead of AndroidX Ink

**Decision.** Ink (`androidx.ink`) was evaluated as the stylus/stroke foundation
and **not adopted**. Ink is still alpha, its API surface is not stable across
versions, and it does not provide layers, compositing, undo or export — so it
would have replaced ~10 % of the work while adding a moving dependency to the
core. The brief explicitly allowed this: Ink is "evaluated", not mandated.

**What was kept.** The ideas that matter from Ink's design: pressure/tilt samples
are the input to geometry (not a post-process), strokes are committed through an
authoring layer, and input handling is separate from rendering.

**Rendering abstraction.** `:engine` never talks to Android. `PixelEdit`
captures undoable before/after tile snapshots, `Compositor` renders into
`Pixmap`, and `:app`'s `CanvasRenderer` converts one bitmap. That boundary is
where an alternative backend would plug in — see D3.

## D3. Custom CPU rasteriser now, GPU later (Rust+wgpu rejected)

**Decision.** Premultiplied-ARGB integer rasterisation on the CPU, sparse
256×256 tiles, incremental region rendering.

**Why.** It is deterministic (so pixels can be asserted), it runs in plain JUnit,
and a 4096² stroke costs ~0.2 s while a 1024² save costs ~0.16 s — measured, not
asserted. Premultiplied ARGB matches the W3C compositing model, so blend maths is
the published reference algorithm instead of an invented one.

**Why not Rust+wgpu.** ProCraft uses it, but that is not a reason. It would add
a second toolchain, cross-compilation, JNI marshalling per stroke, and a GPU
compatibility matrix — for a benefit that only appears on very large canvases.
The brief explicitly said not to add Rust without justification; there is no
measured bottleneck yet that justifies it.

**Where the GPU would go.** `CanvasRenderer` already uploads a single bitmap per
dirty region. Replacing that with a tiled `ImageBitmap` cache (or `RenderNode`)
is a local change, because the engine's output is just pixels + rectangles.

## D4. Sparse tiled pixel storage

**Decision.** `TileSurface`: 256×256 tiles allocated on first write, stored in a
hash map keyed by `(tileX shl 32) or tileY`.

**Why.** A 4096² canvas would be 64 MiB if dense; a typical drawing uses a fifth
of that. Tiles also give undo a natural granularity: undo snapshots only the
tiles a stroke touched, so history cost tracks painted area, not canvas size.

## D5. Command-based undo with tile snapshots

**Decision.** `History` holds `DocCommand`s. Pixel operations record
before/after `TileSnapshot`s; structural operations are `History.Lambda`. History
is bounded by both step count and byte budget (oldest released first).

**Why.** Snapshot-per-stroke is far simpler than inverse operations for brush
rendering (which is not invertible: alpha accumulates), and the byte budget keeps
memory predictable. Deep histories on big canvases degrade gracefully instead of
OOM-ing.

## D6. Native document format `.mona` v3

**Decision.** A ZIP containing: `manifest.json` (versioned, human-readable),
`layer-<id>.deflate` (raw premultiplied tiles), `thumb.png`, and a `SHA256`
entry.

**Why.** The manifest can be read without decoding pixels (gallery metadata,
recovery scan). Per-layer streams let a future version load lazily. SHA-256
catches the failure mode that actually happens — a truncated write — instead of
loading a corrupt document and silently losing layers.

**Atomicity.** Write to `.tmp` → `fsync` → rename previous to `.bak` → rename temp
into place. With `NO_COMPRESSION` for the outer ZIP, because the default DEFLATE
inside DEFLATE hid corruption from the checksum during development.

## D7. JSON metadata index instead of Room

**Decision.** The gallery index is `files/index/projects.json`, written
atomically and rebuilt by scanning project folders when missing. Room and KSP were
removed entirely.

**Why (reversal).** Room was implemented first, then removed for two reasons.
(1) AGP 9.4 ships built-in Kotlin support, which makes the KSP/Room annotation
processor toolchain an extra failure surface — it did in fact break the build.
(2) The query surface is "one list, sorted" — a database buys nothing. The index
is *derived* data: losing it cannot lose artwork, because the projects on disk
are the source of truth.

**Also removed for the same reason:** `kotlinx-serialization` (needed a compiler
plugin; replaced by a small `Json` reader/writer in the engine) and type-safe
Navigation routes (need the same plugin; replaced by a centralised string route
table in `MonaRoot.Routes`).

## D8. MotionEvent input instead of Compose pointer input

**Decision.** A `MonaSurfaceView` (`AndroidView`) receives raw `MotionEvent`s and
feeds `CanvasInputHandler`.

**Why (reversal).** The first implementation used Compose's `pointerInput`. It
turned out to be the wrong layer: Compose's `PointerInputChange` exposes neither
`AXIS_TILT` nor `AXIS_DISTANCE`, and its historical samples lose the per-sample
type. Tilt is a first-class requirement, and hover distance is what makes palm
rejection reliable, so the input layer moved down to `MotionEvent`.

**Honest consequence.** Synthetic tests can verify routing (who paints, what gets
cancelled, which gestures fire) but cannot emulate a real digitizer's tilt. See
limitation L1.

## D9. Manual DI, no Hilt

**Decision.** `MonaArtApp` is the container (settings store, project repository,
brush repository). ViewModels use an explicit factory.

**Why.** Three singletons and one factory — Hilt would add KAPT/KSP, generated
code and a second build failure surface for no testability gain (everything is
constructor-injected and JVM-testable already).

## D10. State: immutable `EditorUiState` + pixels outside it

**Decision.** `StateFlow<EditorUiState>` holds only scalars, layer *snapshots* and
flags. Pixels stay in `ArtDocument`; the renderer owns the single `ImageBitmap`.

**Why.** Putting a bitmap in state means recomposition storms and copies on every
pointer sample. Keeping it out keeps `copy()` cheap and makes the "no full-canvas
redraw per sample" requirement structural rather than a convention.

## D11. Design system as tokens, not Material defaults

**Decision.** `MonaTokens` defines the palette, radii, spacing and type ramp
literally from the brief; `MonaTheme` maps them onto a Material 3 `ColorScheme`
so stock components inherit the identity.

**Why.** The brief fixes exact hex values and a shape scale. Mapping into
Material 3 (rather than fighting it) means `Switch`, `Slider`, dialogs and ripple
behave natively while looking like Mona.

## D12. Testing strategy

| Layer | Tool | Why |
|---|---|---|
| Engine behaviour | JUnit, real pixels | Fast, deterministic, no device |
| Self-check suite | `:engine:selfCheck` JVM main | Runs the whole pipeline end-to-end and prints a markdown report |
| Input routing | Robolectric + synthetic `MotionEvent` | The only way to test stylus routing without hardware |
| Performance | JUnit with generous thresholds | Regression guard against 10× slowdowns, not a benchmark |
| Build honesty | CI writes `BUILD_REPORT.md` with real exit codes | Prevented an earlier false "all green" |

## D13. Documentation of gaps

Every feature that is not complete is listed in `IMPLEMENTATION_STATUS.md` with a
limitation ID (L1…L12) and mapping to `FEATURE_MATRIX.md`, so nothing is claimed
as finished that a user could not actually do.
