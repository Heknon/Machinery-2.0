package me.oriharel.machinery.utilities.extensions

inline fun <reified T: Enum<T>> T.next(jump: Int = 1): T {
    val values = enumValues<T>()
    val nextOrdinal = (ordinal + jump) % values.size
    return values[nextOrdinal]
}