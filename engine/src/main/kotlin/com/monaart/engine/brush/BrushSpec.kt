package com.monaart.engine.brush

import com.monaart.engine.io.Json

/** Broad behavioural family; drives which rasterizer path the brush uses. */
enum class BrushKind {
    /** Opaque-to-transparent round/flat dab, "normal" painting. */
    PAINT,
    /** Hard-edged technical pen: no texture, full opacity per dab. */
    PEN,
    /** Pencil/graphite: grain texture, pressure-driven darkness. */
    PENCIL,
    /** Airbrush: soft falloff, continuous low flow. */
    AIRBRUSH,
    /** Dry media: heavy grain, scattered breakup. */
    CHARCOAL,
    /** Watercolor: soft edges, color bleed into existing paint. */
    WATERCOLOR,
    /** Oil/acrylic: color pickup from canvas, opaque body, stroke texture. */
    OIL,
    /** Marker: translucent, uniform, flat edges. */
    MARKER,
    /** Calligraphy: nib shape driven by angle; width from direction. */
    CALLIGRAPHY,
    /** Scatter/particle: many small dots per spacing step. */
    PARTICLE,
    /** Custom image stamp. */
    STAMP,
    /** Smudge tool: pulls existing color. */
    SMUDGE,
    /** Eraser: removes alpha. */
    ERASER,
}

enum class PressureTarget { NONE, SIZE, OPACITY, FLOW, SIZE_AND_OPACITY }

enum class TaperMode { NONE, START, END, BOTH }

enum class BlendBehavior { NORMAL, MULTIPLY, SCREEN, ADD }

/**
 * A complete brush definition. Everything here changes rendering output; there
 * are no decorative parameters. [texture] references a grain pattern in the
 * asset library by id (built-in patterns use ids like "grain.paper").
 */
data class BrushSpec(
    val id: String = "brush.custom",
    val name: String = "Custom brush",
    val category: String = "Sketching",
    val kind: BrushKind = BrushKind.PAINT,

    /** Base diameter in canvas pixels at 100% pressure. */
    val size: Float = 24f,
    /** Maximum stroke opacity (applied once per stroke, not per dab). */
    val opacity: Float = 1f,
    /** Per-dab alpha: how quickly dabs build up within one stroke. */
    val flow: Float = 1f,
    /** Distance between dabs as a fraction of dab diameter (0.01..2). */
    val spacing: Float = 0.08f,
    /** 0 = fully soft falloff, 1 = hard edge. */
    val hardness: Float = 0.85f,
    /** Nib rotation in degrees. */
    val angle: Float = 0f,
    /** 1 = circular, 0.05 = thin flat nib. */
    val roundness: Float = 1f,

    /** Positional jitter as a fraction of dab radius. */
    val scatter: Float = 0f,
    /** Random extra rotation in degrees. */
    val rotationJitter: Float = 0f,
    /** Random size multiplier range (0..1). */
    val sizeJitter: Float = 0f,
    /** Random hue shift in degrees. */
    val hueJitter: Float = 0f,
    val saturationJitter: Float = 0f,
    val brightnessJitter: Float = 0f,

    val pressureTarget: PressureTarget = PressureTarget.SIZE_AND_OPACITY,
    /** How strongly pressure scales size (weight applied to the pressure curve). */
    val pressureSize: Float = 1f,
    val pressureOpacity: Float = 0.35f,
    val pressureFlow: Float = 0.2f,
    /** How strongly tilt widens/narrows the dab (0..1). */
    val tiltSize: Float = 0f,
    /** How strongly stylus speed thins the stroke (0..1). */
    val velocitySize: Float = 0f,

    val curve: PressureCurve = PressureCurve(),
    /** Input smoothing: 0 = raw, 1 = very smooth (adds slight lag). */
    val stabilization: Float = 0.35f,
    val taperMode: TaperMode = TaperMode.NONE,
    /** Taper length as a fraction of the stroke length estimate. */
    val taperLength: Float = 0.25f,
    /** Minimum size factor at the tapered tip. */
    val taperTipSize: Float = 0.15f,

    /** Grain pattern id (null = no grain). */
    val texture: String? = null,
    /** Grain scale: 1 = one tile every dab diameter. */
    val grainScale: Float = 1f,
    /** 0 = grain ignored, 1 = grain in full effect. */
    val grainStrength: Float = 0.6f,
    /** How much the grain pattern follows stroke direction. */
    val grainFollowStroke: Boolean = false,

    /** Watercolor/oil: how much existing color mixes into the dab (0..1). */
    val wetness: Float = 0f,
    /** Smudge: pickup rate of the smudge tool (0..1). */
    val smudgeRate: Float = 0.6f,
    /** Particle brushes: dots spawned per spacing step. */
    val particleCount: Int = 6,
    /** Custom stamp image id (asset library) for [BrushKind.STAMP]. */
    val stampImage: String? = null,
    /** Per-dab blend behavior (soft-light-ish brushes use MULTIPLY etc). */
    val blend: BlendBehavior = BlendBehavior.NORMAL,
    /** Icon hint for the preset picker. */
    val shape: String = "round",
) {
    init {
        require(size > 0f) { "Brush size must be > 0" }
    }

    val dabRadius: Float get() = size * 0.5f
}

/** Pressure/length response curve: adjustable gamma plus a 5-point shaping curve. */
data class PressureCurve(
    val gamma: Float = 1f,
    val minimum: Float = 0f,
    val maximum: Float = 1f,
    /** Values at t = 0, .25, .5, .75, 1 (piecewise-linear). */
    val points: List<Float> = listOf(0f, 0.25f, 0.5f, 0.75f, 1f),
) {
    /** Maps raw pressure [0,1] through the curve; always returns [minimum, maximum]. */
    fun apply(pressure: Float): Float {
        val p = pressure.coerceIn(0f, 1f)
        val shaped = if (points.size >= 2) {
            val scaled = p * (points.size - 1)
            val i = kotlin.math.floor(scaled).toInt().coerceIn(0, points.size - 2)
            val t = scaled - i
            points[i] + (points[i + 1] - points[i]) * t
        } else p
        val g = if (gamma <= 0.01f) shaped else Math.pow(shaped.toDouble(), gamma.toDouble()).toFloat()
        return (minimum + (maximum - minimum) * g).coerceIn(0f, 1f)
    }
}

/**
 * Brush preset persistence (versioned JSON). Used by brush import/export and by
 * the preset library on disk; unknown fields are ignored so older app versions
 * can still read newer presets.
 */
object BrushCodec {
    const val VERSION = 2

    fun encode(spec: BrushSpec): String = toJson(spec).toJsonString(pretty = true)

    fun toJson(b: BrushSpec): Json = Json.obj(
        "version" to Json.of(VERSION),
        "id" to Json.of(b.id),
        "name" to Json.of(b.name),
        "category" to Json.of(b.category),
        "kind" to Json.of(b.kind.name),
        "size" to Json.of(b.size),
        "opacity" to Json.of(b.opacity),
        "flow" to Json.of(b.flow),
        "spacing" to Json.of(b.spacing),
        "hardness" to Json.of(b.hardness),
        "angle" to Json.of(b.angle),
        "roundness" to Json.of(b.roundness),
        "scatter" to Json.of(b.scatter),
        "rotationJitter" to Json.of(b.rotationJitter),
        "sizeJitter" to Json.of(b.sizeJitter),
        "hueJitter" to Json.of(b.hueJitter),
        "saturationJitter" to Json.of(b.saturationJitter),
        "brightnessJitter" to Json.of(b.brightnessJitter),
        "pressureTarget" to Json.of(b.pressureTarget.name),
        "pressureSize" to Json.of(b.pressureSize),
        "pressureOpacity" to Json.of(b.pressureOpacity),
        "pressureFlow" to Json.of(b.pressureFlow),
        "tiltSize" to Json.of(b.tiltSize),
        "velocitySize" to Json.of(b.velocitySize),
        "curve" to Json.obj(
            "gamma" to Json.of(b.curve.gamma),
            "minimum" to Json.of(b.curve.minimum),
            "maximum" to Json.of(b.curve.maximum),
            "points" to Json.arr(b.curve.points.map { Json.of(it) }),
        ),
        "stabilization" to Json.of(b.stabilization),
        "taperMode" to Json.of(b.taperMode.name),
        "taperLength" to Json.of(b.taperLength),
        "taperTipSize" to Json.of(b.taperTipSize),
        "texture" to Json.of(b.texture),
        "grainScale" to Json.of(b.grainScale),
        "grainStrength" to Json.of(b.grainStrength),
        "grainFollowStroke" to Json.of(b.grainFollowStroke),
        "wetness" to Json.of(b.wetness),
        "smudgeRate" to Json.of(b.smudgeRate),
        "particleCount" to Json.of(b.particleCount),
        "stampImage" to Json.of(b.stampImage),
        "blend" to Json.of(b.blend.name),
        "shape" to Json.of(b.shape),
    )

    fun decode(text: String): BrushSpec = fromJson(Json.parse(text))

    fun fromJson(j: Json): BrushSpec {
        val d = BrushSpec(id = "brush.custom", name = "Custom")
        fun f(key: String, def: Float): Float = j[key]?.asFloat() ?: def
        fun s(key: String, def: String): String = j[key]?.asString() ?: def
        fun b(key: String, def: Boolean): Boolean = j[key]?.asBool() ?: def
        val curveJson = j["curve"]
        val points = curveJson?.get("points")?.array()?.mapNotNull { it.asFloat() }?.takeIf { it.size >= 2 }
            ?: d.curve.points
        val curve = PressureCurve(
            gamma = curveJson?.get("gamma")?.asFloat() ?: d.curve.gamma,
            minimum = curveJson?.get("minimum")?.asFloat() ?: d.curve.minimum,
            maximum = curveJson?.get("maximum")?.asFloat() ?: d.curve.maximum,
            points = points,
        )
        return BrushSpec(
            id = s("id", d.id),
            name = s("name", d.name),
            category = s("category", d.category),
            kind = runCatching { BrushKind.valueOf(s("kind", d.kind.name)) }.getOrDefault(d.kind),
            size = f("size", d.size).coerceIn(1f, 2000f),
            opacity = f("opacity", d.opacity).coerceIn(0f, 1f),
            flow = f("flow", d.flow).coerceIn(0f, 1f),
            spacing = f("spacing", d.spacing).coerceIn(0.01f, 2f),
            hardness = f("hardness", d.hardness).coerceIn(0f, 1f),
            angle = f("angle", d.angle),
            roundness = f("roundness", d.roundness).coerceIn(0.03f, 1f),
            scatter = f("scatter", d.scatter).coerceIn(0f, 3f),
            rotationJitter = f("rotationJitter", d.rotationJitter),
            sizeJitter = f("sizeJitter", d.sizeJitter).coerceIn(0f, 1f),
            hueJitter = f("hueJitter", d.hueJitter).coerceIn(0f, 180f),
            saturationJitter = f("saturationJitter", d.saturationJitter).coerceIn(0f, 1f),
            brightnessJitter = f("brightnessJitter", d.brightnessJitter).coerceIn(0f, 1f),
            pressureTarget = runCatching { PressureTarget.valueOf(s("pressureTarget", d.pressureTarget.name)) }
                .getOrDefault(d.pressureTarget),
            pressureSize = f("pressureSize", d.pressureSize).coerceIn(0f, 1f),
            pressureOpacity = f("pressureOpacity", d.pressureOpacity).coerceIn(0f, 1f),
            pressureFlow = f("pressureFlow", d.pressureFlow).coerceIn(0f, 1f),
            tiltSize = f("tiltSize", d.tiltSize).coerceIn(0f, 1f),
            velocitySize = f("velocitySize", d.velocitySize).coerceIn(0f, 1f),
            curve = curve,
            stabilization = f("stabilization", d.stabilization).coerceIn(0f, 0.95f),
            taperMode = runCatching { TaperMode.valueOf(s("taperMode", d.taperMode.name)) }.getOrDefault(d.taperMode),
            taperLength = f("taperLength", d.taperLength).coerceIn(0f, 1f),
            taperTipSize = f("taperTipSize", d.taperTipSize).coerceIn(0f, 1f),
            texture = j["texture"]?.asString(),
            grainScale = f("grainScale", d.grainScale).coerceIn(0.02f, 16f),
            grainStrength = f("grainStrength", d.grainStrength).coerceIn(0f, 1f),
            grainFollowStroke = b("grainFollowStroke", d.grainFollowStroke),
            wetness = f("wetness", d.wetness).coerceIn(0f, 1f),
            smudgeRate = f("smudgeRate", d.smudgeRate).coerceIn(0f, 1f),
            particleCount = j["particleCount"]?.asInt() ?: d.particleCount,
            stampImage = j["stampImage"]?.asString(),
            blend = runCatching { BlendBehavior.valueOf(s("blend", d.blend.name)) }.getOrDefault(d.blend),
            shape = s("shape", d.shape),
        )
    }
}
