package me.oriharel.machinery.utilities.extensions

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.nio.file.Files
import java.nio.file.Path

fun <T : Path> T.searchForFilesWithExtension(extension: String): List<Path> {
    if (!Files.isDirectory(this)) throw RuntimeException("Attempted to search for files with extension in a non-directory")

    val matches: MutableList<Path> = mutableListOf()

    walk {
        if (it.toString().endsWith(extension)) matches.add(it)
    }

    return matches
}

fun <T : Path> T.searchForFilesWithExtensionAsync(extension: String, plugin: JavaPlugin, callback: (List<Path>) -> Unit) {
    Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
        callback(searchForFilesWithExtension(extension))
    })
}

fun <T : Path> T.walk(callback: (Path) -> Unit): T {
    Files.walk(this).forEach { callback(it) }
    return this
}