package com.voc2048.affixForge.gui

import com.voc2048.affixForge.data.CustomItems
import com.voc2048.affixForge.data.Keys
import com.voc2048.affixForge.data.AffixListDataType
import com.voc2048.affixForge.data.AffixRegistry
import com.voc2048.affixForge.logic.AffixLogicManager
import com.voc2048.affixForge.util.isAuthenticated
import com.voc2048.affixForge.util.maxSlots
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

class UpgradeGUI(private val plugin: JavaPlugin, private val player: Player) : Listener {

    private val inventory: Inventory = Bukkit.createInventory(null, 54, Component.text("玉髓開槽與鑽石升級台"))
    
    companion object {
        const val EQUIPMENT_SLOT = 13
        const val MATERIAL_SLOT = 22
        val AFFIX_UPGRADE_SLOTS = listOf(37, 38, 39, 40, 41)
        const val EXPAND_BUTTON_SLOT = 49
    }

    init {
        setupDecorations()
        player.openInventory(inventory)
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    private fun setupDecorations() {
        val glass = ItemStack(Material.GRAY_STAINED_GLASS_PANE).apply {
            editMeta { it.displayName(Component.empty()) }
        }
        for (i in 0 until 54) {
            if (i == EQUIPMENT_SLOT || i == MATERIAL_SLOT || i in AFFIX_UPGRADE_SLOTS || i == EXPAND_BUTTON_SLOT) continue
            inventory.setItem(i, glass)
        }
        updateDisplay()
    }

    private fun updateDisplay() {
        val equipment = inventory.getItem(EQUIPMENT_SLOT)
        val material = inventory.getItem(MATERIAL_SLOT)
        
        // Expand Button
        val expandButton = ItemStack(Material.PURPLE_SHULKER_BOX).apply {
            editMeta { meta ->
                meta.displayName(Component.text("槽位擴張").color(NamedTextColor.LIGHT_PURPLE))
                val lore = mutableListOf<Component>()
                if (equipment == null || !equipment.isAuthenticated()) {
                    lore.add(Component.text("請放入已鑑定的裝備").color(NamedTextColor.RED))
                } else if (equipment.maxSlots >= 16) {
                    lore.add(Component.text("槽位已達上限 (16)").color(NamedTextColor.RED))
                } else {
                    lore.add(Component.text("消耗: 1x 玉髓錠").color(NamedTextColor.GRAY))
                    if (!CustomItems.isChalcedonyIngot(material)) {
                        lore.add(Component.text("缺少材料").color(NamedTextColor.RED))
                    } else {
                        lore.add(Component.text("點擊開始擴張").color(NamedTextColor.GREEN))
                    }
                }
                meta.lore(lore)
            }
        }
        inventory.setItem(EXPAND_BUTTON_SLOT, expandButton)

        // Affix Upgrades
        val affixes = equipment?.itemMeta?.persistentDataContainer?.get(Keys.AFFIXES, AffixListDataType) ?: emptyList()
        for (i in 0 until 5) {
            val slot = AFFIX_UPGRADE_SLOTS[i]
            if (i < affixes.size) {
                val affix = affixes[i]
                val template = AffixRegistry.getTemplate(affix.id)
                val upgradeItem = ItemStack(Material.DIAMOND_BLOCK).apply {
                    editMeta { meta ->
                        meta.displayName(Component.text("升級: ${affix.name}").color(NamedTextColor.AQUA))
                        val lore = mutableListOf<Component>()
                        lore.add(Component.text("當前等級: ${affix.level}").color(NamedTextColor.GRAY))
                        if (template != null && affix.level >= template.maxLevel) {
                            lore.add(Component.text("等級已達上限").color(NamedTextColor.RED))
                        } else {
                            lore.add(Component.text("消耗: 1x 鑽石磚").color(NamedTextColor.GRAY))
                            if (material?.type != Material.DIAMOND_BLOCK) {
                                lore.add(Component.text("缺少材料").color(NamedTextColor.RED))
                            } else {
                                lore.add(Component.text("點擊開始升級").color(NamedTextColor.GREEN))
                            }
                        }
                        meta.lore(lore)
                    }
                }
                inventory.setItem(slot, upgradeItem)
            } else {
                inventory.setItem(slot, null)
            }
        }
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        if (event.inventory != inventory) return
        val slot = event.rawSlot

        if (slot < 54 && slot != EQUIPMENT_SLOT && slot != MATERIAL_SLOT) {
            event.isCancelled = true
        }

        if (slot == EXPAND_BUTTON_SLOT) {
            handleExpand()
        } else if (slot in AFFIX_UPGRADE_SLOTS) {
            val index = AFFIX_UPGRADE_SLOTS.indexOf(slot)
            handleUpgrade(index)
        }

        Bukkit.getScheduler().runTask(plugin, Runnable { updateDisplay() })
    }

    private fun handleExpand() {
        val equipment = inventory.getItem(EQUIPMENT_SLOT) ?: return
        val material = inventory.getItem(MATERIAL_SLOT) ?: return

        if (!CustomItems.isChalcedonyIngot(material)) {
            player.sendMessage(Component.text("請放入玉髓錠").color(NamedTextColor.RED))
            return
        }

        if (AffixLogicManager.expandSlot(equipment)) {
            material.amount -= 1
            player.playSound(player.location, Sound.BLOCK_ANVIL_USE, 1f, 1f)
            player.sendMessage(Component.text("槽位擴張成功！").color(NamedTextColor.GREEN))
        } else {
            player.sendMessage(Component.text("擴張失敗：可能已達上限").color(NamedTextColor.RED))
        }
    }

    private fun handleUpgrade(affixIndex: Int) {
        val equipment = inventory.getItem(EQUIPMENT_SLOT) ?: return
        val material = inventory.getItem(MATERIAL_SLOT) ?: return
        
        if (material.type != Material.DIAMOND_BLOCK) {
            player.sendMessage(Component.text("請放入鑽石磚").color(NamedTextColor.RED))
            return
        }

        val affixes = equipment.itemMeta?.persistentDataContainer?.get(Keys.AFFIXES, AffixListDataType) ?: emptyList()
        if (affixIndex >= affixes.size) return
        val affix = affixes[affixIndex]
        val template = AffixRegistry.getTemplate(affix.id)

        if (template != null && AffixLogicManager.upgradeAffixLevel(equipment, affix.id, template.maxLevel)) {
            material.amount -= 1
            player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
            player.sendMessage(Component.text("詞條升級成功！").color(NamedTextColor.GREEN))
        } else {
            player.sendMessage(Component.text("升級失敗：可能已達上限").color(NamedTextColor.RED))
        }
    }

    @EventHandler
    fun onClose(event: InventoryCloseEvent) {
        if (event.inventory != inventory) return
        listOf(EQUIPMENT_SLOT, MATERIAL_SLOT).forEach { slot ->
            val item = inventory.getItem(slot)
            if (item != null && item.type != Material.AIR) {
                player.inventory.addItem(item).values.forEach { 
                    player.world.dropItemNaturally(player.location, it)
                }
            }
        }
        InventoryClickEvent.getHandlerList().unregister(this)
        InventoryCloseEvent.getHandlerList().unregister(this)
    }
}
