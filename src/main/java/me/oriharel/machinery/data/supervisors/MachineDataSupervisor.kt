package me.oriharel.machinery.data.supervisors

import me.oriharel.machinery.machine.Machine
import org.bukkit.Bukkit
import java.nio.file.Files
import java.nio.file.Path

class MachineDataSupervisor : DataSupervisor<Machine, ByteArray> {
    override val trackedFiles: MutableSet<Path> = Bukkit.getWorlds().map { it.worldFolder.resolve(FILE_NAME).toPath() }.toMutableSet()
    override val cache: MutableMap<ByteArray, Machine> = mutableMapOf()

    override fun init() {
        trackedFiles.forEach {
            if (!Files.exists(it)) {
                Files.createFile(it)
            }
        }
    }

    override fun save(file: Path, save: Machine, overwrite: Boolean): ByteArray {
        TODO("Not yet implemented")
    }

    override fun get(file: Path, primitive: ByteArray): Machine {
        TODO("Not yet implemented")
    }

    override fun toPrimitive(complex: Machine): ByteArray {
        TODO("Not yet implemented")
    }

    override fun fromPrimitive(primitive: ByteArray): Machine {
        TODO("Not yet implemented")
    }

    companion object {
        const val FILE_NAME = "machines.dat"
    }

}