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
JUnit, so 27 behavioural self-checks and 69 unit tests execute in seconds without
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

## D3b. Stroke flushing: cumulative buffer, pending-region recomposite

**Decision.** The stroke rasteriser accumulates the stroke's coverage in one
scratch buffer, and `BrushEngine.flush()` recomposites only the pixels painted
since the previous flush against the *pre-stroke* pixels.

**Why.** Two wrong designs were tried and rejected with measurements:

1. *Compositing the buffer on top of the live layer.* A 50 % opacity brush reached
   100 % after a couple of flushes, because each flush applied stroke opacity
   again. Re-compositing against the pristine pixels applies it exactly once.
2. *Flushing only the newest strip of the stroke.* Fast strokes broke into dashes,
   because a dab drawn after a flush overlaps pixels already on the layer.

The cost trap is elsewhere: the compositors must scan only the intersection of
the scratch buffer with the tile being written (and `TileSurface.copyRect` /
`writeRect` resolve one tile per row segment instead of one hash lookup per
pixel), and the scratch buffer must grow geometrically. With those three fixes a
per-sample flush on a 2048² canvas costs **0.33 ms**, down from **10.5 ms** —
the difference between "fine on a 60 Hz phone" and "unusable". `StrokeQualityTest`
fails the build if it regresses past 4 ms, and separately asserts that the painted
result is identical whether the engine flushes once or on every sample.

## D3c. Interactive tools rewrite a bounded box, not the canvas

**Decision.** The Transform and Liquify tools capture the layer once, then on every
pointer sample rewrite only the rectangle they can affect, and repaint only that
rectangle (`PixelEditSession.limitTo` / `lastDirty`).

**Why.** Both tools originally called `renderAll()` per sample — a full-canvas
composite and a full bitmap upload for every pointer move, which is exactly what the
"no full-canvas redraw per pointer sample" requirement forbids. The bounded version
is provably equivalent: `a limited interactive render matches a full one and stays
bounded` renders the same six-step drag both ways and compares every pixel.

**The subtle part.** A transform *moves* artwork, so the box it vacates also has to be
mapped (to transparent), not merely left alone: limiting the render to the
destination box leaves a ghost of the previous geometry. The limit is therefore
`destination box ∪ box the artwork occupied when the gesture began`, with the
destination box computed by mapping the artwork's four corners through the same
affine map the renderer applies. Estimating it from the artwork's radius is wrong —
a rotation about a pivot far from the artwork moves it much further than its size.

## D3d. A stroke buffer that recycles instead of growing

**Decision.** The stroke scratch buffer grows by a capped, fixed slack, and once it
passes ~1.6 MB the coverage that has already been flushed is dropped: the buffer is
rebuilt around the live tail of the stroke.

**Why.** Growing by a multiple of the current *bounding box* is fine for a
horizontal stroke and pathological for a diagonal one: a 2000 px diagonal that is
60 px wide grew to a 5878×5986 buffer (134 MB) and copied it on every extension,
producing 85 ms stalls. It is also how a long stroke ends up with a buffer
proportional to its bounding box rather than to its paint.

**Why it is safe.** Dabs are drawn in order, so after a flush only the tail near the
current position can still change; the window kept covers the next dabs' reach and
the region the flush still had to write. Pixels outside it are finalized, and later
dabs are not allowed to re-flush them (a dab that lands on a finalized tile keeps
that tile out of the pending set), which would otherwise write transparent pixels
over finished artwork. Two tests pin this down: bit-identical output versus a
single-flush stroke, and a p99 frame-budget assertion over 1500 samples.

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


## D14. Detekt baselines per module, seeded from real findings

**Decision.** `app/config/detekt/baseline.xml` and `engine/config/detekt/baseline.xml`,
each generated once from the findings that actually existed, with
`build.maxIssues` left at Detekt's default of `0`.

**Why not** a single root baseline: Detekt resolves the baseline relative to the
project it runs in, so a root file is silently ignored by both modules. **Why
not** an empty baseline and "we will fix them later": that turns the gate red for
hundreds of pre-existing style findings and hides new ones in the noise. The
baseline is frozen at the audit commit; anything written after it must be clean.

## D15. `ktlint_official` kept, cosmetic wrapping rules disabled

**Decision.** `.editorconfig` keeps `ktlint_code_style = ktlint_official` and
`max_line_length = 120`, and disables 13 rules that only dictate *where* a line
breaks (`argument-list-wrapping`, `function-signature`, `class-signature`,
`property-wrapping`, `multiline-expression-wrapping`, `expression-wrapping`,
`chain-method-continuation`, `function-expression-body`, `if-else-wrapping`,
`try-catch-finally-spacing`, `string-template-indent`,
`blank-line-before-declaration`, `no-consecutive-comments`), each with an inline
rationale.

**Reversed from** an earlier note that the code style must not be touched. Once
`--continue` was added, ktlint reported ~5,000 violations, of which 4,300 were
these formatting opinions on code that was already readable. `ktlintFormat` would
have rewritten most of the codebase and, in doing so, itself emitted a 143-character
line in `CropTest.kt`. The rules that find defects stay on; `max-line-length` is
not auto-correctable, so the 163 long lines were wrapped by a script with a
whitespace-only proof.

## D16. `requestUnbufferedDispatch` behind `SDK_INT >= R`

**Decision.** `MonaSurfaceView.configureForStylus()` calls
`View.requestUnbufferedDispatch` only when `Build.VERSION.SDK_INT >= R`, and sets
`SOURCE_CLASS_POINTER` on the input device unconditionally.

**Reversed from** a commit in this same pass that removed the guard on the
assumption the method had existed since API 21. It has existed since API 30; the
unconditional call would have thrown `NoSuchMethodError` on every Android 8-10
device with a stylus, i.e. exactly the users this path serves. Android Lint's
`NewApi` check is what settled it, and it is why that check stays fatal.
