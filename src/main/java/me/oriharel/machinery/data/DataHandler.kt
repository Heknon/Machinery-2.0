package me.oriharel.machinery.data

import me.oriharel.machinery.data.managers.DataManager

/**
 * Wraps up the DataManagers into one DataHandler that sends data update, remove, and get "requests"
 */
class DataHandler {

    val managers: MutableMap<Class<*>, DataManager<Any, Any>> = mutableMapOf()

    inline fun <reified C : Any, P : Any> registerManager(manager: DataManager<Any, Any>) {
        managers[C::class.java] = manager
    }

    inline fun <reified C : Any> set(complex: C, overwrite: Boolean = false) {
        managers[C::class.java]?.save(complex, overwrite)
    }

    inline fun <reified P : Any> get(primitive: P) {
        managers[P::class.java]?.get(primitive)
    }

    private fun <C, P> set(manager: DataManager<C, P>, complex: C, overwrite: Boolean = false): P {
        return manager.save(complex, overwrite)
    }

    private fun <C, P> get(manager: DataManager<C, P>, primitive: P): C {
        return manager.get(primitive)
    }

}