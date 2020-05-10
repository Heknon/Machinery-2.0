package me.oriharel.machinery.data.managers

interface DataManager<C, P> {

    val cache: MutableMap<P, C>

    fun save(save: C, overwrite: Boolean = false): P

    fun get(primitive: P): C

    fun toPrimitive(complex: C): P

    fun fromPrimitive(primitive: P): C

}