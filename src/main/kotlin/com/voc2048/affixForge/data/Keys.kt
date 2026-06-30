package com.voc2048.affixForge.data

import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

object Keys {
    lateinit var AFFIXES: NamespacedKey
    lateinit var QUALITY: NamespacedKey

    fun init(plugin: JavaPlugin) {
        AFFIXES = NamespacedKey(plugin, "affixes")
        QUALITY = NamespacedKey(plugin, "quality")
    }
}
