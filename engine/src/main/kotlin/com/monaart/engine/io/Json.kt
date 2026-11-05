package com.monaart.engine.io

/**
 * A tiny, strict JSON reader/writer.
 *
 * The engine deliberately avoids runtime serialization frameworks: the project
 * file and brush presets need explicit, versioned, defensively-parsed formats so
 * that a malformed or hostile file can never crash or corrupt a session. This is
 * ~200 lines of well-tested code instead of an annotation processor dependency.
 */
sealed class Json {
    object Null : Json()
    data class Bool(val value: Boolean) : Json()
    data class Num(val value: Double) : Json()
    data class Str(val value: String) : Json()
    data class Arr(val items: List<Json>) : Json()
    data class Obj(val fields: Map<String, Json>) : Json()

    fun asString(): String? = (this as? Str)?.value
    fun asInt(): Int? = (this as? Num)?.value?.toInt()
    fun asLong(): Long? = (this as? Num)?.value?.toLong()
    fun asFloat(): Float? = (this as? Num)?.value?.toFloat()
    fun asDouble(): Double? = (this as? Num)?.value
    fun asBool(): Boolean? = when (this) {
        is Bool -> value
        is Num -> value != 0.0
        else -> null
    }

    fun array(): List<Json> = (this as? Arr)?.items ?: emptyList()
    fun obj(): Map<String, Json> = (this as? Obj)?.fields ?: emptyMap()
    operator fun get(key: String): Json? = (this as? Obj)?.fields?.get(key)

    /** Reads an int from a string or number field, tolerating both. */
    fun intOr(default: Int): Int = asInt() ?: asDouble()?.toInt() ?: asString()?.toIntOrNull() ?: default

    fun toJsonString(pretty: Boolean = false): String = StringBuilder().also { write(it, pretty, 0) }.toString()

    private fun write(sb: StringBuilder, pretty: Boolean, indent: Int) {
        when (this) {
            is Null -> sb.append("null")
            is Bool -> sb.append(if (value) "true" else "false")
            is Num -> sb.append(formatNumber(value))
            is Str -> quote(sb, value)
            is Arr -> {
                sb.append('[')
                items.forEachIndexed { i, v ->
                    if (i > 0) sb.append(',')
                    if (pretty) newline(sb, indent + 1)
                    v.write(sb, pretty, indent + 1)
                }
                if (pretty && items.isNotEmpty()) newline(sb, indent)
                sb.append(']')
            }
            is Obj -> {
                sb.append('{')
                var first = true
                for ((k, v) in fields) {
                    if (!first) sb.append(',')
                    first = false
                    if (pretty) newline(sb, indent + 1)
                    quote(sb, k)
                    sb.append(if (pretty) ": " else ":")
                    v.write(sb, pretty, indent + 1)
                }
                if (pretty && fields.isNotEmpty()) newline(sb, indent)
                sb.append('}')
            }
        }
    }

    private fun newline(sb: StringBuilder, indent: Int) {
        sb.append('\n')
        repeat(indent) { sb.append("  ") }
    }

    companion object {
        fun of(v: String?): Json = if (v == null) Null else Str(v)
        fun of(v: Int): Json = Num(v.toDouble())
        fun of(v: Long): Json = Num(v.toDouble())
        fun of(v: Float): Json = Num(v.toDouble())
        fun of(v: Double): Json = Num(v)
        fun of(v: Boolean): Json = Bool(v)
        fun arr(vararg items: Json): Json = Arr(items.toList())
        fun arr(items: List<Json>): Json = Arr(items)
        fun obj(vararg pairs: Pair<String, Json>): Json = Obj(linkedMapOf(*pairs))
        fun obj(pairs: List<Pair<String, Json>>): Json = Obj(linkedMapOf(*pairs.toTypedArray()))

        private fun formatNumber(v: Double): String {
            if (v.isNaN() || v.isInfinite()) return "0"
            if (v == v.toLong().toDouble() && kotlin.math.abs(v) < 9.0e15) return v.toLong().toString()
            return v.toString()
        }

        private fun quote(sb: StringBuilder, s: String) {
            sb.append('"')
            for (c in s) {
                when (c) {
                    '"' -> sb.append("\\\"")
                    '\\' -> sb.append("\\\\")
                    '\n' -> sb.append("\\n")
                    '\r' -> sb.append("\\r")
                    '\t' -> sb.append("\\t")
                    else -> if (c < ' ') sb.append("\\u%04x".format(c.code)) else sb.append(c)
                }
            }
            sb.append('"')
        }

        fun parse(text: String): Json {
            val p = Parser(text)
            val v = p.parseValue()
            p.skipWhitespace()
            return v
        }

        private class Parser(val s: String) {
            var i = 0

            fun skipWhitespace() {
                while (i < s.length && s[i].isWhitespace()) i++
            }

            fun parseValue(): Json {
                skipWhitespace()
                if (i >= s.length) throw IllegalArgumentException("Unexpected end of JSON")
                return when (val c = s[i]) {
                    '{' -> parseObject()
                    '[' -> parseArray()
                    '"' -> Str(parseString())
                    't' -> { expect("true"); Bool(true) }
                    'f' -> { expect("false"); Bool(false) }
                    'n' -> { expect("null"); Null }
                    else -> if (c == '-' || c.isDigit()) parseNumber() else
                        throw IllegalArgumentException("Unexpected character '$c' at $i")
                }
            }

            private fun expect(word: String) {
                if (!s.startsWith(word, i)) throw IllegalArgumentException("Expected '$word' at $i")
                i += word.length
            }

            private fun parseObject(): Json {
                i++ // {
                val fields = LinkedHashMap<String, Json>()
                skipWhitespace()
                if (i < s.length && s[i] == '}') { i++; return Obj(fields) }
                while (true) {
                    skipWhitespace()
                    if (i >= s.length || s[i] != '"') throw IllegalArgumentException("Expected key at $i")
                    val key = parseString()
                    skipWhitespace()
                    if (i >= s.length || s[i] != ':') throw IllegalArgumentException("Expected ':' at $i")
                    i++
                    fields[key] = parseValue()
                    skipWhitespace()
                    if (i >= s.length) throw IllegalArgumentException("Unterminated object")
                    when (s[i]) {
                        ',' -> i++
                        '}' -> { i++; return Obj(fields) }
                        else -> throw IllegalArgumentException("Expected ',' or '}' at $i")
                    }
                }
            }

            private fun parseArray(): Json {
                i++ // [
                val items = ArrayList<Json>()
                skipWhitespace()
                if (i < s.length && s[i] == ']') { i++; return Arr(items) }
                while (true) {
                    items.add(parseValue())
                    skipWhitespace()
                    if (i >= s.length) throw IllegalArgumentException("Unterminated array")
                    when (s[i]) {
                        ',' -> i++
                        ']' -> { i++; return Arr(items) }
                        else -> throw IllegalArgumentException("Expected ',' or ']' at $i")
                    }
                }
            }

            private fun parseString(): String {
                i++ // opening quote
                val sb = StringBuilder()
                while (true) {
                    if (i >= s.length) throw IllegalArgumentException("Unterminated string")
                    val c = s[i++]
                    when (c) {
                        '"' -> return sb.toString()
                        '\\' -> {
                            if (i >= s.length) throw IllegalArgumentException("Bad escape")
                            when (val e = s[i++]) {
                                '"' -> sb.append('"')
                                '\\' -> sb.append('\\')
                                '/' -> sb.append('/')
                                'b' -> sb.append('\b')
                                'f' -> sb.append('\u000C')
                                'n' -> sb.append('\n')
                                'r' -> sb.append('\r')
                                't' -> sb.append('\t')
                                'u' -> {
                                    if (i + 4 > s.length) throw IllegalArgumentException("Bad unicode escape")
                                    sb.append(s.substring(i, i + 4).toInt(16).toChar())
                                    i += 4
                                }
                                else -> throw IllegalArgumentException("Bad escape '$e'")
                            }
                        }
                        else -> sb.append(c)
                    }
                }
            }

            private fun parseNumber(): Json {
                val start = i
                if (i < s.length && (s[i] == '-' || s[i] == '+')) i++
                while (i < s.length && (s[i].isDigit() || s[i] == '.' || s[i] == 'e' || s[i] == 'E' || s[i] == '-' || s[i] == '+')) i++
                val token = s.substring(start, i)
                val d = token.toDoubleOrNull() ?: throw IllegalArgumentException("Bad number '$token'")
                return Num(d)
            }
        }
    }
}
