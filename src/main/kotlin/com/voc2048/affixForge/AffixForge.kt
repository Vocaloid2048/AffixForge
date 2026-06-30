package com.voc2048.affixForge

import com.voc2048.affixForge.command.AffixCommand
import com.voc2048.affixForge.data.AffixRegistry
import com.voc2048.affixForge.data.Keys
import com.voc2048.affixForge.listener.MenuListener
import com.voc2048.affixForge.listener.PlayerStatListener
import com.voc2048.affixForge.logic.StatManager
import org.bukkit.plugin.java.JavaPlugin

class AffixForge : JavaPlugin() {

    override fun onEnable() {
        Keys.init(this)
        AffixRegistry.load(this)
        StatManager.init(this)
        
        getCommand("affix")?.setExecutor(AffixCommand(this))
        server.pluginManager.registerEvents(PlayerStatListener(this), this)
        server.pluginManager.registerEvents(MenuListener(this), this)

        logger.info("AffixForge has been enabled!")
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
