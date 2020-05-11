package me.oriharel.machinery.utilities.extensions

import java.lang.reflect.Field

inline fun <reified T> T.getPrivateField(fieldName: String, setAsAccessible: Boolean = true): Field {
    var clazz: Class<*> = T::class.java

    while (clazz != Object::class.java) {
        val field: Field? = clazz.getDeclaredField(fieldName)
        if (field == null) {
            clazz = clazz.superclass
            continue
        }

        if (setAsAccessible) field.isAccessible = true
        return field
    }

    throw RuntimeException("Field not found!")
}

inline fun <reified T, reified V> T.getPrivateFieldValue(fieldName: String): V? {
    val field = getPrivateField(fieldName)
    val value = field.get(fieldName)
    if (value !is V?) return null
    return value
}

inline fun <reified T, reified V> T.setPrivateFieldValue(fieldName: String, value: V) {
    val field = getPrivateField(fieldName)
    field.set(this, value)
}

