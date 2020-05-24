package me.oriharel.machinery.structure

import me.oriharel.machinery.Machinery
import me.oriharel.machinery.structure.schematic.Schematic
import me.oriharel.machinery.utilities.extensions.execute
import java.nio.file.Path

class StructureRegistry(val machinery: Machinery) : HashMap<Path, Schematic>() {

    inline fun <reified T : Schematic> registerStructures(structures: List<Path>, onlyIfAbsent: Boolean = false, async: Boolean = false) {
        execute(async, machinery) {
            registerStructures<T>(structures.associateBy { it.fileName.toString() }, onlyIfAbsent, async)
        }
    }

    inline fun <reified T : Schematic> registerStructures(structures: Map<String, Path>, onlyIfAbsent: Boolean = false, async: Boolean = false) {
        execute(async, machinery) {
            registerStructures<T>(async, onlyIfAbsent, *structures.entries.map { Pair(it.key, it.value) }.toTypedArray())
        }
    }

    inline fun <reified T : Schematic> registerStructures(async: Boolean = false, onlyIfAbsent: Boolean = false, vararg structures: Pair<String, Path>) {
        execute(async, machinery) {
            structures.forEach { registerStructure<T>(it.second, onlyIfAbsent, async) }
        }
    }

    inline fun <reified T : Schematic> registerStructure(schematic: Path, onlyIfAbsent: Boolean = false, async: Boolean = false) {
        execute(async, machinery) {
            registerStructure(T::class::constructors.get().first().call(machinery, schematic), onlyIfAbsent)
        }
    }

    fun registerStructure(schematic: Schematic, onlyIfAbsent: Boolean) {
        if (!schematic.isLoaded) schematic.loadSchematic()
        if (!onlyIfAbsent) this[schematic.schematic] = schematic
        else putIfAbsent(schematic.schematic, schematic)
    }
}