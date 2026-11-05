# Feature matrix

Legend: **Implemented** — shipped and exercised by tests; **Partial** — the core
exists but a documented part is missing; **Planned** — not built, listed so the
gap is explicit.

## Canvas & document

| Feature | State | Notes |
|---|---|---|
| Create canvas from presets (print, screen, social, comic) | Implemented | 12 presets + custom size, DPI, orientation, background |
| Custom presets saved by the user | Implemented | Stored in the project index |
| Transparent or coloured background | Implemented | |
| Canvas rotation, pan, pinch zoom (12.5 %–6400 %) | Implemented | `Viewport`, rotation-aware hit testing |
| Fit / 100 % / reset rotation | Implemented | |
| Symmetry (vertical + horizontal guides) | Implemented | Mirrored strokes as second engine pass |
| Crop to selection | Implemented | Engine `Transforms.crop` |
| Flip canvas horizontally / vertically | Implemented | |
| Transform tool (drag-move, live scale/rotation sliders, Apply/Reset) | Implemented | One undo step per gesture (`Transforms.PixelEditSession`) |
| Free transform corner handles | Partial | Tool options drive scale/rotation; on-canvas handles are not built (L2) |
| Liquify (push brush, adjustable radius/strength) | Implemented | Cumulative push, undoable per gesture |
| Liquify twirl/pinch/bloat modes | Planned | Only the push mode exists |
| Resize / resample document after creation | Planned | |

## Brush engine

| Feature | State | Notes |
|---|---|---|
| Size, opacity, flow, spacing, hardness, roundness, angle | Implemented | |
| Pressure → size / opacity / flow (with curve + gamma) | Implemented | |
| Tilt → dab shape | Implemented (engine); input verified synthetically only (L1) | |
| Velocity → size | Implemented | |
| Scatter, size jitter, rotation jitter, hue/sat/brightness jitter | Implemented | Seeded RNG, reproducible |
| Grain textures (procedural) + grain scale/strength/follow-stroke | Implemented | 8 patterns |
| Taper (both / in / out) | Implemented | |
| Stabilisation (input smoothing) | Implemented | |
| Wetness (colour pickup), smudge rate, particle count | Implemented | |
| 30 built-in presets across 6 categories | Implemented | |
| Save / duplicate / favourite custom presets | Implemented | JSON in `files/brushes/` |
| Custom stamp images | Partial | `BrushKind.STAMP` exists, no import UI (L5) |
| Brush preview | Implemented | Rendered by the real engine, not a mock |

## Layers

| Feature | State |
|---|---|
| Add / duplicate / delete / reorder / move into group | Implemented |
| Groups with recursive compositing | Implemented |
| Visibility, lock, alpha lock, clipping mask | Implemented |
| Opacity (0–100 %) | Implemented |
| Rename | Implemented |
| Merge down, flatten image | Implemented |
| Layer thumbnails | Implemented (dedicated 26 dp inspector view) |
| 16 blend modes in 6 groups | Implemented |
| Layer search / filter | Planned |
| Layer effects (stroke, shadow, glow) | Planned |

## Selection

| Feature | State |
|---|---|
| Rectangle / ellipse / lasso | Implemented |
| Magic wand (contiguous, tolerance) | Implemented |
| Select all, invert, feather, grow, shrink, clear | Implemented |
| Marching-ants style overlay | Implemented (dashed outline) |
| Quick mask editing | Planned |

## Colour

| Feature | State |
|---|---|
| HSB picker, hue strip, hex field | Implemented |
| Primary/secondary colour + swap | Implemented |
| Eyedropper (continuous while dragging) | Implemented |
| 6 artist palettes | Implemented |
| Colour history | Planned |
| Colour harmony / wheel | Planned |

## History

| Feature | State |
|---|---|
| Command-based undo/redo with labels | Implemented |
| Tile-snapshot pixel diffs (bounded by byte budget) | Implemented |
| Depth configurable (10–400 steps) | Implemented |
| History panel with scrubber | Planned |

## Files

| Feature | State |
|---|---|
| Native `.mona` v3 (manifest + DEFLATE tiles + SHA-256) | Implemented |
| Atomic save + `.bak` previous version | Implemented |
| Autosave (configurable) + `recovery.mona` | Implemented |
| Corruption detection with clear failure | Implemented |
| Format migration from v1/v2 manifests | Implemented |
| Export PNG | Implemented |
| Export JPEG (baseline, quality) | Implemented |
| Export TIFF (uncompressed RGBA) | Implemented |
| Export PSD (composite + layer records) | Implemented |
| Export PDF | Implemented |
| Export at 0.5× / 1× / 2× / 4× | Implemented |
| Export WebP | Planned (L7 — removed rather than faked) |
| Export SVG | Planned (L6) |
| Import PNG/JPEG as a layer | Partial | Picker wired, decode via `BitmapFactory` |
| Import PSD | Planned (L4) |
| Open `.mona` / images shared from another app (ACTION_VIEW) | Implemented | `MainActivity` intake → `importProject`/`importImage`, opens in the editor, reports failures |
| Receive a second share while already running | Implemented | `onNewIntent` |

## Input

| Feature | State |
|---|---|
| Stylus pressure (with batched historical samples) | Implemented |
| Stylus tilt + orientation | Implemented on device code path; synthetic verification only (L1) |
| Eraser tip (tool type + inverted axis) | Implemented |
| Palm rejection (time window + hover distance + contact size) | Implemented |
| Side-button shortcut (configurable) | Implemented |
| Two-finger pan / pinch / rotate | Implemented |
| Two- and three-finger taps → undo / redo / panels | Implemented |
| Double-tap to toggle zoom | Implemented |
| Stylus hover cursor | Partial | Hover position tracked, ring overlay is drawn only during contact |
| Input modes (stylus only / stylus + finger / any input) | Implemented |

## App shell & design

| Feature | State |
|---|---|
| Nine screens | Implemented |
| Bento dashboard | Implemented |
| Palette tokens (exact brief values) | Implemented |
| Shape system 8–36 dp | Implemented |
| Typography (bundled Inter, OFL) | Implemented |
| Dark / Light / AMOLED | Implemented |
| Accent colour choice | Implemented |
| Font scale, reduce motion, haptics | Implemented |
| Tablet-first + phone-adaptive | Implemented |
| TalkBack labels, 48 dp targets, no colour-only state | Implemented (audited per screen) |
| Onboarding tour | Partial (L8) |
| Localisation | Planned (L12) |
