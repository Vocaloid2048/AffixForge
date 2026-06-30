package com.voc2048.affixForge.gui

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

class MenuGUI(private val plugin: JavaPlugin, private val player: Player) {

    companion object {
        val TITLE = LegacyComponentSerializer.legacySection().deserialize("§0🌌 AffixForge 主選單")
    }

    private val inventory: Inventory = Bukkit.createInventory(null, 27, TITLE)

    init {
        setupMenu()
        player.openInventory(inventory)
    }

    private fun setupMenu() {
        val glass = ItemStack(Material.GRAY_STAINED_GLASS_PANE).apply {
            editMeta { it.displayName(Component.empty()) }
        }
        for (i in 0 until 27) {
            inventory.setItem(i, glass)
        }

        // Slot 9: BOOK -> 【附魔轉移】
        inventory.setItem(9, createMenuItem(Material.BOOK, "§b【附魔轉移】", "§7點擊開啟附魔轉移功能。"))
        
        // Slot 10: LAPIS_BLOCK -> 【命運鑑定】
        inventory.setItem(10, createMenuItem(Material.LAPIS_BLOCK, "§d【命運鑑定】", "§7點擊開啟裝備鑑定面板。"))
        
        // Slot 11: ANVIL -> 【詞條洗練】
        inventory.setItem(11, createMenuItem(Material.ANVIL, "§6【詞條洗練】", "§7點擊開啟詞條重置與鎖定面板。"))
        
        // Slot 12: SMITHING_TABLE -> 【極限突破】
        inventory.setItem(12, createMenuItem(Material.SMITHING_TABLE, "§a【極限突破】", "§7點擊開啟玉髓擴充與升級面板。"))
        
        // Slot 17: BARRIER -> 關閉選單
        inventory.setItem(17, createMenuItem(Material.BARRIER, "§c關閉選單"))
    }

    private fun createMenuItem(material: Material, name: String, loreText: String? = null): ItemStack {
        return ItemStack(material).apply {
            editMeta { meta ->
                meta.displayName(LegacyComponentSerializer.legacySection().deserialize(name))
                if (loreText != null) {
                    meta.lore(listOf(LegacyComponentSerializer.legacySection().deserialize(loreText)))
                }
            }
        }
    }
}
