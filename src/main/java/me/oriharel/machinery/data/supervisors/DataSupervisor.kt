package me.oriharel.machinery.data.supervisors

import java.nio.file.Path

interface DataSupervisor<C, P> {

    val trackedFiles: MutableSet<Path>
    val cache: MutableMap<P, C>

    /**
     * responsible for initializing the Data Supervisor.
     * This will instantiate the tracked files list and do other supervisor specific tasks
     */
    fun init()

    fun save(file: Path, save: C, overwrite: Boolean = false): P

    fun get(file: Path, primitive: P): C

    fun toPrimitive(complex: C): P

    fun fromPrimitive(primitive: P): C

}