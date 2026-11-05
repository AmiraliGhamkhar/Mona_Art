package com.monaart.engine.brush

/**
 * Built-in brush library. Every preset differs in *rendering behaviour*
 * (kind, falloff, grain, wetness, taper, pressure response), not just in name.
 */
object BrushPresets {
    val categories = listOf(
        "Sketching", "Inking", "Painting", "Dry media", "Airbrush",
        "Marker", "Calligraphy", "Texture", "Eraser", "Smudge",
    )

    val all: List<BrushSpec> = listOf(
        // ---------------- Sketching ----------------
        BrushSpec(
            id = "sketch.pencil-hb", name = "Pencil HB", category = "Sketching", kind = BrushKind.PENCIL,
            size = 6f, flow = 0.55f, spacing = 0.05f, hardness = 0.55f,
            pressureTarget = PressureTarget.SIZE_AND_OPACITY, pressureSize = 0.55f, pressureOpacity = 0.6f,
            pressureFlow = 0.5f, stabilization = 0.25f,
            texture = "grain.pencil", grainStrength = 0.55f, grainScale = 1.4f,
            shape = "pencil",
        ),
        BrushSpec(
            id = "sketch.pencil-soft", name = "Soft Graphite", category = "Sketching", kind = BrushKind.PENCIL,
            size = 18f, flow = 0.3f, spacing = 0.08f, hardness = 0.25f,
            pressureTarget = PressureTarget.SIZE_AND_OPACITY, pressureSize = 0.7f, pressureOpacity = 0.7f,
            pressureFlow = 0.6f, stabilization = 0.3f,
            texture = "grain.paper", grainStrength = 0.5f, grainScale = 0.9f,
            shape = "pencil",
        ),
        BrushSpec(
            id = "sketch.charcoal-stick", name = "Charcoal Stick", category = "Dry media", kind = BrushKind.CHARCOAL,
            size = 34f, flow = 0.42f, spacing = 0.11f, hardness = 0.35f, roundness = 0.82f,
            scatter = 0.12f, sizeJitter = 0.25f, rotationJitter = 25f,
            pressureTarget = PressureTarget.SIZE_AND_OPACITY, pressureSize = 0.6f, pressureOpacity = 0.55f,
            texture = "grain.charcoal", grainStrength = 0.85f, grainScale = 0.7f,
            stabilization = 0.4f, taperMode = TaperMode.BOTH, taperLength = 0.08f,
            shape = "charcoal",
        ),
        BrushSpec(
            id = "sketch.chalk", name = "Chalk", category = "Dry media", kind = BrushKind.CHARCOAL,
            size = 28f, flow = 0.34f, spacing = 0.13f, hardness = 0.5f, roundness = 0.95f,
            scatter = 0.2f, sizeJitter = 0.3f, rotationJitter = 40f,
            pressureTarget = PressureTarget.OPACITY, pressureSize = 0.3f, pressureOpacity = 0.8f,
            texture = "grain.chalk", grainStrength = 0.9f, grainScale = 1.1f,
            shape = "chalk",
        ),
        // ---------------- Inking ----------------
        BrushSpec(
            id = "ink.technical-pen", name = "Technical Pen", category = "Inking", kind = BrushKind.PEN,
            size = 4f, flow = 1f, spacing = 0.035f, hardness = 1f,
            pressureTarget = PressureTarget.SIZE, pressureSize = 0.35f, pressureOpacity = 0f, pressureFlow = 0f,
            stabilization = 0.55f, shape = "pen",
        ),
        BrushSpec(
            id = "ink.g-pen", name = "G-Pen", category = "Inking", kind = BrushKind.CALLIGRAPHY,
            size = 9f, flow = 1f, spacing = 0.03f, hardness = 0.95f, roundness = 0.55f, angle = 35f,
            pressureTarget = PressureTarget.SIZE, pressureSize = 0.95f, pressureOpacity = 0f, pressureFlow = 0f,
            stabilization = 0.5f, taperMode = TaperMode.BOTH, taperLength = 0.22f, taperTipSize = 0.08f,
            velocitySize = 0.15f, shape = "pen",
        ),
        BrushSpec(
            id = "ink.marker-liner", name = "Ink Liner", category = "Inking", kind = BrushKind.PEN,
            size = 2f, flow = 1f, spacing = 0.04f, hardness = 1f, opacity = 1f,
            pressureTarget = PressureTarget.NONE, stabilization = 0.45f, shape = "pen",
        ),
        BrushSpec(
            id = "ink.fineliner-smooth", name = "Smooth Liner", category = "Inking", kind = BrushKind.PEN,
            size = 6f, flow = 0.95f, spacing = 0.03f, hardness = 0.9f,
            pressureTarget = PressureTarget.SIZE, pressureSize = 0.8f,
            stabilization = 0.85f, taperMode = TaperMode.BOTH, taperLength = 0.15f, shape = "pen",
        ),
        // ---------------- Calligraphy ----------------
        BrushSpec(
            id = "calligraphy.flat-nib", name = "Flat Nib", category = "Calligraphy", kind = BrushKind.CALLIGRAPHY,
            size = 22f, flow = 1f, spacing = 0.02f, hardness = 0.95f, roundness = 0.16f, angle = 45f,
            pressureTarget = PressureTarget.SIZE_AND_OPACITY, pressureSize = 0.5f, pressureOpacity = 0.4f,
            stabilization = 0.35f, shape = "calligraphy",
        ),
        BrushSpec(
            id = "calligraphy.brush-pen", name = "Brush Pen", category = "Calligraphy", kind = BrushKind.CALLIGRAPHY,
            size = 16f, flow = 1f, spacing = 0.025f, hardness = 0.85f, roundness = 0.85f,
            pressureTarget = PressureTarget.SIZE, pressureSize = 1f, pressureOpacity = 0.1f,
            stabilization = 0.45f, taperMode = TaperMode.BOTH, taperLength = 0.3f, taperTipSize = 0.05f,
            shape = "calligraphy",
        ),
        // ---------------- Painting ----------------
        BrushSpec(
            id = "paint.flat-brush", name = "Flat Brush", category = "Painting", kind = BrushKind.OIL,
            size = 42f, flow = 0.75f, spacing = 0.045f, hardness = 0.7f, roundness = 0.42f, angle = 0f,
            pressureTarget = PressureTarget.SIZE_AND_OPACITY, pressureSize = 0.8f, pressureOpacity = 0.4f,
            texture = "grain.canvas", grainStrength = 0.35f, grainScale = 1.6f, grainFollowStroke = true,
            wetness = 0.3f, stabilization = 0.3f, shape = "brush",
        ),
        BrushSpec(
            id = "paint.round-oil", name = "Round Oil", category = "Painting", kind = BrushKind.OIL,
            size = 28f, flow = 0.6f, spacing = 0.06f, hardness = 0.55f,
            pressureTarget = PressureTarget.SIZE_AND_OPACITY, pressureSize = 0.9f, pressureOpacity = 0.45f,
            pressureFlow = 0.3f, texture = "grain.canvas", grainStrength = 0.25f, grainScale = 1.2f,
            wetness = 0.45f, stabilization = 0.35f, taperMode = TaperMode.START, shape = "brush",
        ),
        BrushSpec(
            id = "paint.watercolor-soft", name = "Watercolor Soft", category = "Painting", kind = BrushKind.WATERCOLOR,
            size = 60f, flow = 0.18f, spacing = 0.06f, hardness = 0.08f,
            pressureTarget = PressureTarget.SIZE_AND_OPACITY, pressureSize = 0.7f, pressureOpacity = 0.6f,
            pressureFlow = 0.5f, texture = "grain.paper", grainStrength = 0.45f, grainScale = 1.8f,
            wetness = 0.55f, stabilization = 0.5f, shape = "watercolor",
        ),
        BrushSpec(
            id = "paint.watercolor-wash", name = "Wash", category = "Painting", kind = BrushKind.WATERCOLOR,
            size = 140f, flow = 0.08f, spacing = 0.05f, hardness = 0.03f, opacity = 1f,
            pressureTarget = PressureTarget.FLOW, pressureFlow = 0.7f, wetness = 0.4f,
            texture = "grain.paper", grainStrength = 0.35f, grainScale = 2.4f, stabilization = 0.6f,
            shape = "watercolor",
        ),
        BrushSpec(
            id = "paint.acrylic-opaque", name = "Acrylic Opaque", category = "Painting", kind = BrushKind.OIL,
            size = 46f, flow = 0.98f, spacing = 0.03f, hardness = 0.9f, roundness = 0.7f,
            pressureTarget = PressureTarget.SIZE, pressureSize = 0.5f,
            texture = "grain.canvas", grainStrength = 0.2f, grainScale = 1.3f, wetness = 0.15f,
            stabilization = 0.2f, shape = "brush",
        ),
        BrushSpec(
            id = "paint.gouache", name = "Gouache", category = "Painting", kind = BrushKind.OIL,
            size = 36f, flow = 0.85f, spacing = 0.04f, hardness = 0.75f, roundness = 0.6f,
            pressureTarget = PressureTarget.SIZE_AND_OPACITY, pressureSize = 0.6f, pressureOpacity = 0.35f,
            wetness = 0.25f, texture = "grain.canvas", grainStrength = 0.3f, grainScale = 1.5f,
            shape = "brush",
        ),
        // ---------------- Airbrush ----------------
        BrushSpec(
            id = "airbrush.soft", name = "Soft Airbrush", category = "Airbrush", kind = BrushKind.AIRBRUSH,
            size = 120f, flow = 0.1f, spacing = 0.04f, hardness = 0f,
            pressureTarget = PressureTarget.FLOW, pressureFlow = 0.85f, stabilization = 0.4f,
            shape = "airbrush",
        ),
        BrushSpec(
            id = "airbrush.hard", name = "Hard Airbrush", category = "Airbrush", kind = BrushKind.AIRBRUSH,
            size = 48f, flow = 0.22f, spacing = 0.035f, hardness = 0.5f,
            pressureTarget = PressureTarget.SIZE_AND_OPACITY, pressureSize = 0.4f, pressureOpacity = 0.5f,
            pressureFlow = 0.6f, shape = "airbrush",
        ),
        BrushSpec(
            id = "airbrush.spray-particles", name = "Spray Particles", category = "Airbrush", kind = BrushKind.PARTICLE,
            size = 70f, flow = 0.35f, spacing = 0.22f, hardness = 0.9f,
            particleCount = 14, scatter = 0.25f,
            pressureTarget = PressureTarget.SIZE_AND_OPACITY, pressureSize = 0.5f, pressureOpacity = 0.5f,
            shape = "spray",
        ),
        // ---------------- Markers ----------------
        BrushSpec(
            id = "marker.copic-chisel", name = "Chisel Marker", category = "Marker", kind = BrushKind.MARKER,
            size = 30f, flow = 0.45f, spacing = 0.03f, hardness = 0.92f, roundness = 0.3f, angle = -35f,
            opacity = 0.75f, pressureTarget = PressureTarget.NONE,
            blend = BlendBehavior.MULTIPLY, stabilization = 0.3f, shape = "marker",
        ),
        BrushSpec(
            id = "marker.alcohol-soft", name = "Alcohol Marker", category = "Marker", kind = BrushKind.MARKER,
            size = 44f, flow = 0.3f, spacing = 0.04f, hardness = 0.6f,
            opacity = 0.55f, blend = BlendBehavior.MULTIPLY, shape = "marker",
        ),
        BrushSpec(
            id = "marker.highlighter", name = "Highlighter", category = "Marker", kind = BrushKind.MARKER,
            size = 52f, flow = 0.28f, spacing = 0.03f, hardness = 0.95f, roundness = 0.35f, angle = -45f,
            opacity = 0.4f, blend = BlendBehavior.MULTIPLY, shape = "marker",
        ),
        // ---------------- Texture ----------------
        BrushSpec(
            id = "texture.sponge", name = "Sponge", category = "Texture", kind = BrushKind.CHARCOAL,
            size = 90f, flow = 0.35f, spacing = 0.18f, hardness = 0.4f,
            scatter = 0.3f, sizeJitter = 0.35f, rotationJitter = 90f, particleCount = 4,
            texture = "grain.sponge", grainStrength = 0.95f, grainScale = 0.6f,
            shape = "texture",
        ),
        BrushSpec(
            id = "texture.canvas-grain", name = "Canvas Grain", category = "Texture", kind = BrushKind.PAINT,
            size = 56f, flow = 0.4f, spacing = 0.1f, hardness = 0.6f,
            texture = "grain.canvas", grainStrength = 0.8f, grainScale = 1f, grainFollowStroke = true,
            shape = "texture",
        ),
        BrushSpec(
            id = "texture.stamp-leaf", name = "Stamp: Leaf", category = "Texture", kind = BrushKind.STAMP,
            size = 40f, flow = 1f, spacing = 0.9f, hardness = 1f, stampImage = "stamp.leaf",
            pressureTarget = PressureTarget.SIZE, pressureSize = 0.5f, shape = "stamp",
        ),
        BrushSpec(
            id = "texture.particle-dust", name = "Particle Dust", category = "Texture", kind = BrushKind.PARTICLE,
            size = 60f, flow = 0.5f, spacing = 0.3f, hardness = 0.8f, particleCount = 24,
            scatter = 0.5f, sizeJitter = 0.6f,
            pressureTarget = PressureTarget.OPACITY, pressureOpacity = 0.7f,
            shape = "particles",
        ),
        // ---------------- Eraser / Smudge ----------------
        BrushSpec(
            id = "eraser.hard", name = "Hard Eraser", category = "Eraser", kind = BrushKind.ERASER,
            size = 40f, flow = 1f, spacing = 0.03f, hardness = 0.95f, opacity = 1f,
            pressureTarget = PressureTarget.SIZE, pressureSize = 0.6f, stabilization = 0.25f,
            shape = "eraser",
        ),
        BrushSpec(
            id = "eraser.soft", name = "Soft Eraser", category = "Eraser", kind = BrushKind.ERASER,
            size = 80f, flow = 0.25f, spacing = 0.04f, hardness = 0.05f,
            pressureTarget = PressureTarget.FLOW, pressureFlow = 0.7f, shape = "eraser",
        ),
        BrushSpec(
            id = "eraser.pencil-eraser", name = "Pencil Eraser", category = "Eraser", kind = BrushKind.ERASER,
            size = 22f, flow = 0.7f, spacing = 0.06f, hardness = 0.7f,
            texture = "grain.rough", grainStrength = 0.5f, grainScale = 1.2f,
            pressureTarget = PressureTarget.OPACITY, pressureOpacity = 0.8f, shape = "eraser",
        ),
        BrushSpec(
            id = "smudge.soft", name = "Soft Smudge", category = "Smudge", kind = BrushKind.SMUDGE,
            size = 44f, flow = 0.9f, spacing = 0.02f, hardness = 0.15f,
            smudgeRate = 0.7f, pressureTarget = PressureTarget.FLOW, pressureFlow = 0.6f,
            shape = "smudge",
        ),
    )

    fun byId(id: String): BrushSpec? = all.firstOrNull { it.id == id }
    fun byCategory(category: String): List<BrushSpec> = all.filter { it.category == category }
    val default: BrushSpec get() = byId("ink.g-pen")!!

    /** Presets grouped for the picker UI. */
    fun grouped(): List<Pair<String, List<BrushSpec>>> =
        categories.map { c -> c to all.filter { it.category == c } }.filter { it.second.isNotEmpty() }
}
