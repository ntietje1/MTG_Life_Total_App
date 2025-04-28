package domain.utils

fun Long.toBoolean(): Boolean {
    return if (this == 1L) {
        true
    } else if (this == 0L) {
        false
    } else {
        throw Exception("Invalid long value attempted to be converted to boolean: $this")
    }
}

fun Boolean.toLong(): Long {
    return if (this) 1 else 0
}

fun Int.toBoolean(): Boolean {
    return if (this == 1) {
        true
    } else if (this == 0) {
        false
    } else {
        throw Exception("Invalid long value attempted to be converted to boolean: $this")
    }
}

fun Boolean.toInt(): Int {
    return if (this) 1 else 0
}