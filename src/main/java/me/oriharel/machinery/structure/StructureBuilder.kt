package me.oriharel.machinery.structure

import me.oriharel.machinery.Machinery
import me.oriharel.machinery.structure.schematic.Schematic
import me.oriharel.machinery.utilities.extensions.execute
import java.nio.file.Path

class StructureBuilder(private val machinery: Machinery, private val structures: MutableMap<String, Schematic> = mutableMapOf()) {

    fun getStructureById(key: String): Schematic = structures[key]
            ?: throw RuntimeException("Structure with key $key not found")

    fun registerStructures(structures: List<Path>, async: Boolean = false, callback: ((Unit) -> Unit)? = null) = execute(async, machinery, callback) {
        registerStructures(structures.associateBy { it.fileName.toString() })
    }

    fun registerStructures(structures: Map<String, Path>, async: Boolean = false, callback: ((Unit) -> Unit)? = null) = execute(async, machinery, callback) {
        registerStructures(*structures.entries.map { Pair(it.key, it.value) }.toTypedArray())
    }

    fun registerStructures(vararg structures: Pair<String, Path>, async: Boolean = false, callback: ((Unit) -> Unit)? = null) = execute(async, machinery, callback) {
        structures.forEach { registerStructure(it.first, it.second) }
    }

    fun registerStructure(key: String, schematic: Path, async: Boolean = false, callback: ((Unit) -> Unit)? = null) = execute(async, machinery, callback) {
        registerStructure(key, Schematic(machinery, schematic))
    }

    fun registerStructure(key: String, schematic: Schematic, async: Boolean = false, callback: ((Unit) -> Unit)? = null) {
        execute(async, machinery, callback) {
            schematic.loadSchematic()
            structures[key] = schematic
        }
    }
}