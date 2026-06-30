package com.voc2048.affixForge

import com.voc2048.affixForge.command.AffixCommand
import com.voc2048.affixForge.data.AffixPool
import com.voc2048.affixForge.data.Keys
import com.voc2048.affixForge.listener.PlayerStatListener
import org.bukkit.plugin.java.JavaPlugin

class AffixForge : JavaPlugin() {

    override fun onEnable() {
        Keys.init(this)
        AffixPool.load(this)
        
        getCommand("affix")?.setExecutor(AffixCommand())
        server.pluginManager.registerEvents(PlayerStatListener(this), this)

        logger.info("AffixForge has been enabled!")
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
