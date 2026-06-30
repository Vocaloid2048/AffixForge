package com.voc2048.affixForge.listener

import com.voc2048.affixForge.gui.AuthenticationGUI
import com.voc2048.affixForge.gui.MenuGUI
import com.voc2048.affixForge.gui.ReforgingGUI
import com.voc2048.affixForge.gui.UpgradeGUI
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.plugin.java.JavaPlugin

class MenuListener(private val plugin: JavaPlugin) : Listener {

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val viewTitle = event.view.title()
        
        // 檢查標題是否包含 "🌌 AffixForge 主選單"
        val titleString = LegacyComponentSerializer.legacySection().serialize(viewTitle)
        if (!titleString.contains("🌌 AffixForge 主選單")) return

        event.isCancelled = true

        val slot = event.rawSlot
        if (slot == 17) {
            player.closeInventory()
            return
        }

        when (slot) {
            9, 10 -> {
                player.closeInventory()
                AuthenticationGUI(plugin, player)
            }
            11 -> {
                player.closeInventory()
                ReforgingGUI(plugin, player)
            }
            12 -> {
                player.closeInventory()
                UpgradeGUI(plugin, player)
            }
        }
    }
}
