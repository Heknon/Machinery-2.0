package me.oriharel.machinery

import org.bukkit.plugin.java.JavaPlugin

class Machinery : JavaPlugin() {

    override fun onLoad() {
        INSTANCE = this
    }

    override fun onEnable() {
    }

    override fun onDisable() {

    }

    companion object {
        lateinit var INSTANCE: Machinery
    }

}