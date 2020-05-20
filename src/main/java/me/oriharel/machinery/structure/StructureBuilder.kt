package me.oriharel.machinery.structure

import me.oriharel.machinery.Machinery
import me.oriharel.machinery.structure.schematic.Schematic
import me.oriharel.machinery.utilities.extensions.execute
import java.nio.file.Path

class StructureBuilder(val machinery: Machinery, private val structures: MutableMap<String, Schematic> = mutableMapOf()) {

    fun getStructureById(key: String): Schematic = structures[key]
            ?: throw RuntimeException("Structure with key $key not found")

    inline fun <reified T : Schematic> registerStructures(structures: List<Path>, async: Boolean = false) {
        execute(async, machinery) {
            registerStructures<T>(structures.associateBy { it.fileName.toString() }, async)
        }
    }

    inline fun <reified T : Schematic> registerStructures(structures: Map<String, Path>, async: Boolean = false) {
        execute(async, machinery) {
            registerStructures<T>(async, *structures.entries.map { Pair(it.key, it.value) }.toTypedArray())
        }
    }

    inline fun <reified T : Schematic> registerStructures(async: Boolean = false, vararg structures: Pair<String, Path>) {
        execute(async, machinery) {
            structures.forEach { registerStructure<T>(it.first, it.second, async) }
        }
    }

    inline fun <reified T : Schematic> registerStructure(key: String, schematic: Path, async: Boolean = false) {
        execute(async, machinery) {
            registerStructure(key, T::class::constructors.get().first().call(machinery, schematic))
        }
    }

    fun registerStructure(key: String, schematic: Schematic) {
        schematic.loadSchematic()
        structures[key] = schematic
    }
}