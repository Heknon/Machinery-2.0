package me.oriharel.machinery

import me.oriharel.machinery.structure.schematic.Schematic
import java.util.*

interface OwnableStructure {
    val structure: Pair<String, Schematic>
    val owner: UUID
    val allowedAccessors: Set<UUID>
}