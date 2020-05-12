package me.oriharel.machinery.utilities.extensions

import java.lang.reflect.Field

inline fun <reified T> T.getPrivateField(fieldName: String, setAsAccessible: Boolean = true): Field {
    var clazz: Class<*> = T::class.java

    while (clazz != Object::class.java) {
        try {
            val field: Field? = clazz.getDeclaredField(fieldName)
            if (field == null) {
                clazz = clazz.superclass
                continue
            }
            if (setAsAccessible) field.isAccessible = true
            return field
        } catch (e: NoSuchFieldException) {
            clazz = clazz.superclass
            continue
        }
    }

    throw RuntimeException("Field $fieldName not found!")
}

inline fun <reified T, reified V> T.getPrivateFieldValue(fieldName: String): V? {
    val field = this.getPrivateField(fieldName)
    val value = field.get(this)
    if (value !is V?) return null
    return value
}

inline fun <reified T, reified V> T.setPrivateFieldValue(fieldName: String, value: V) {
    val field = getPrivateField(fieldName)
    field.set(this, value)
}

