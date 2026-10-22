package com.software.sello.data.backup

/**
 * Structural caps on a file, checked before it is parsed into objects. A field's
 * own rule (a 60-character note) still applies on top of [maxFieldCharacters].
 */
data class BackupBounds(
    val maxBytes: Int = 10 * 1024 * 1024,
    val maxRecords: Int = 100_000,
    val maxFieldCharacters: Int = 10_000,
    val maxDepth: Int = 8
)

/**
 * One pass over the text that counts what a parser would allocate: nesting depth,
 * array elements (every record is one) and string length after escapes. It also
 * refuses a key repeated in one object, which the JSON library would quietly let the
 * last one win. It does not otherwise judge the syntax; the parser after it does.
 */
internal fun scanStructure(text: String, bounds: BackupBounds): BackupRejection? {
    // One entry per open container: a key set for an object, null for an array.
    val open = ArrayList<HashSet<String>?>()
    var expectingKey = false
    var arrayValueExpected = false
    var elements = 0L
    var index = 0

    fun startValue(): BackupRejection? {
        if (open.lastOrNull() == null && open.isNotEmpty() && arrayValueExpected) {
            arrayValueExpected = false
            elements++
            if (elements >
                bounds.maxRecords
            ) {
                return BackupRejection.TooManyRecords(bounds.maxRecords)
            }
        }
        return null
    }

    while (index < text.length) {
        val char = text[index]
        when (char) {
            '"' -> {
                startValue()?.let { return it }
                val key = if (expectingKey && open.isNotEmpty() &&
                    open.last() != null
                ) {
                    StringBuilder()
                } else {
                    null
                }
                var length = 0
                index++
                while (true) {
                    if (index >= text.length) return BackupRejection.Truncated
                    val next = text[index]
                    if (next == '"') break
                    if (next == '\\') {
                        if (index + 1 >= text.length) return BackupRejection.Truncated
                        val escaped = text[index + 1]
                        if (escaped == 'u') {
                            if (index + 5 >= text.length) return BackupRejection.Truncated
                            val hex = text.substring(index + 2, index + 6)
                            val code = hex.toIntOrNull(16) ?: return BackupRejection.Malformed(null)
                            key?.append(code.toChar())
                            index += 6
                        } else {
                            key?.append(unescape(escaped) ?: return BackupRejection.Malformed(null))
                            index += 2
                        }
                    } else {
                        key?.append(next)
                        index++
                    }
                    length++
                    if (length > bounds.maxFieldCharacters) {
                        return BackupRejection.FieldTooLong(bounds.maxFieldCharacters)
                    }
                }
                if (key != null) {
                    expectingKey = false
                    if (!open.last()!!.add(key.toString())) {
                        return BackupRejection.DuplicateKey(key.toString())
                    }
                }
            }

            '{', '[' -> {
                startValue()?.let { return it }
                if (open.size >= bounds.maxDepth) return BackupRejection.TooDeep(bounds.maxDepth)
                if (char == '{') {
                    open += HashSet<String>()
                    expectingKey = true
                } else {
                    open += null
                    arrayValueExpected = true
                }
            }

            '}', ']' -> {
                if (open.isEmpty()) return BackupRejection.Malformed(null)
                open.removeAt(open.lastIndex)
                expectingKey = false
                arrayValueExpected = false
            }

            ',' -> {
                if (open.isNotEmpty()) {
                    if (open.last() == null) arrayValueExpected = true else expectingKey = true
                }
            }

            ' ', '\t', '\n', '\r', ':' -> Unit

            // The first character of a number or literal; the rest of it is skipped
            // below so `true` counts as one element, not four.
            else -> {
                startValue()?.let { return it }
                while (index + 1 < text.length && text[index + 1] !in literalEnd) index++
            }
        }
        index++
    }
    return if (open.isEmpty()) null else BackupRejection.Truncated
}

private val literalEnd = setOf(',', ']', '}', ' ', '\t', '\n', '\r', ':', '"', '{', '[')

private fun unescape(escaped: Char): Char? = when (escaped) {
    '"' -> '"'
    '\\' -> '\\'
    '/' -> '/'
    'b' -> '\b'
    'f' -> '\u000C'
    'n' -> '\n'
    'r' -> '\r'
    't' -> '\t'
    else -> null
}
