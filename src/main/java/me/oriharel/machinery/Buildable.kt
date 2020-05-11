package me.oriharel.machinery

import org.bukkit.Location

interface Buildable {
    fun build(location: Location)
}