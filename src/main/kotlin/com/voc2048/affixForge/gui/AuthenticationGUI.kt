package com.voc2048.affixForge.gui

import com.voc2048.affixForge.logic.AffixLogicManager
import com.voc2048.affixForge.util.isAuthenticated
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

class AuthenticationGUI(private val plugin: JavaPlugin, private val player: Player) : Listener {

    private val inventory: Inventory = Bukkit.createInventory(null, 27, Component.text("鑑定與附魔轉移台"))
    
    companion object {
        const val EQUIPMENT_SLOT = 11
        const val MATERIAL_SLOT = 13
        const val ACTION_SLOT = 15
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
        for (i in 0 until 27) {
            if (i == EQUIPMENT_SLOT || i == MATERIAL_SLOT || i == ACTION_SLOT) continue
            inventory.setItem(i, glass)
        }
        updateActionButton()
    }

    private fun updateActionButton() {
        val equipment = inventory.getItem(EQUIPMENT_SLOT)
        val material = inventory.getItem(MATERIAL_SLOT)
        
        val button = ItemStack(Material.ANVIL).apply {
            editMeta { meta ->
                meta.displayName(Component.text("確認操作").color(NamedTextColor.GOLD))
                val lore = mutableListOf<Component>()
                
                if (equipment == null || equipment.type.isAir) {
                    lore.add(Component.text("請放入裝備").color(NamedTextColor.RED))
                } else if (equipment.isAuthenticated()) {
                    lore.add(Component.text("該裝備已鑑定").color(NamedTextColor.RED))
                } else {
                    if (material?.type == Material.BOOK) {
                        lore.add(Component.text("模式: 附魔轉移").color(NamedTextColor.AQUA))
                        lore.add(Component.text("消耗: 1x 書本").color(NamedTextColor.GRAY))
                    } else if (material?.type == Material.LAPIS_BLOCK) {
                        lore.add(Component.text("模式: 裝備鑑定").color(NamedTextColor.GREEN))
                        lore.add(Component.text("消耗: 1x 青金石磚").color(NamedTextColor.GRAY))
                    } else {
                        lore.add(Component.text("請放入書本或青金石磚").color(NamedTextColor.RED))
                    }
                }
                meta.lore(lore)
            }
        }
        inventory.setItem(ACTION_SLOT, button)
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        if (event.inventory != inventory) return
        val slot = event.rawSlot
        
        if (slot < 27 && slot != EQUIPMENT_SLOT && slot != MATERIAL_SLOT) {
            event.isCancelled = true
        }

        if (slot == ACTION_SLOT) {
            handleAction()
        }

        // Delay update to catch item changes
        Bukkit.getScheduler().runTask(plugin, Runnable { updateActionButton() })
    }

    private fun handleAction() {
        val equipment = inventory.getItem(EQUIPMENT_SLOT) ?: return
        val material = inventory.getItem(MATERIAL_SLOT) ?: return

        if (equipment.isAuthenticated()) {
            player.sendMessage(Component.text("該裝備已鑑定").color(NamedTextColor.RED))
            return
        }

        if (material.type == Material.BOOK) {
            val resultBook = AffixLogicManager.extractEnchantments(equipment, material)
            if (resultBook != null) {
                material.amount -= 1
                player.inventory.addItem(resultBook).values.forEach { 
                    player.world.dropItemNaturally(player.location, it) 
                }
                player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
                player.sendMessage(Component.text("附魔轉移完成！").color(NamedTextColor.GREEN))
            } else {
                player.sendMessage(Component.text("轉移失敗：裝備可能沒有附魔").color(NamedTextColor.RED))
            }
        } else if (material.type == Material.LAPIS_BLOCK) {
            if (equipment.enchantments.isNotEmpty()) {
                player.sendMessage(Component.text("請先轉移裝備上的附魔再進行鑑定").color(NamedTextColor.RED))
                return
            }
            if (AffixLogicManager.authenticateItem(equipment)) {
                material.amount -= 1
                player.playSound(player.location, Sound.BLOCK_ANVIL_USE, 1f, 1f)
                player.spawnParticle(Particle.CHERRY_LEAVES, player.location.add(0.0, 1.0, 0.0), 30, 0.5, 0.5, 0.5, 0.1)
                player.sendMessage(Component.text("裝備鑑定成功！").color(NamedTextColor.GREEN))
            }
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
