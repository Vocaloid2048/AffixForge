package com.voc2048.affixForge.gui

import com.voc2048.affixForge.data.AffixListDataType
import com.voc2048.affixForge.data.Keys
import com.voc2048.affixForge.logic.ReforgeManager
import com.voc2048.affixForge.model.ReforgeResult
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

class ReforgeGUI(private val plugin: JavaPlugin, private val player: Player) : Listener {

    private val inventory: Inventory = Bukkit.createInventory(null, 54, Component.text("裝備重鑄台"))
    private val lockedIndices = mutableSetOf<Int>()

    companion object {
        const val EQUIPMENT_SLOT = 13
        const val LAPIS_SLOT = 21
        const val DIAMOND_BLOCK_SLOT = 23
        const val REFORGE_BUTTON_SLOT = 31
        val AFFIX_DISPLAY_SLOTS = listOf(37, 38, 39, 40, 41)
        val LOCK_BUTTON_SLOTS = listOf(46, 47, 48, 49, 50)
    }

    init {
        setupDecorations()
        player.openInventory(inventory)
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    private fun setupDecorations() {
        val glass = ItemStack(Material.GRAY_STAINED_GLASS_PANE).apply {
            val meta = itemMeta
            meta.displayName(Component.empty())
            itemMeta = meta
        }
        for (i in 0 until 54) {
            if (i == EQUIPMENT_SLOT || i == LAPIS_SLOT || i == DIAMOND_BLOCK_SLOT || 
                i == REFORGE_BUTTON_SLOT || i in AFFIX_DISPLAY_SLOTS || i in LOCK_BUTTON_SLOTS) continue
            inventory.setItem(i, glass)
        }

        updateReforgeButton()
        updateAffixDisplay()
    }

    private fun updateReforgeButton() {
        val button = ItemStack(Material.ANVIL).apply {
            val meta = itemMeta
            meta.displayName(Component.text("開始重鑄").color(NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false))
            val lore = mutableListOf<Component>()
            lore.add(Component.text("點擊開始洗煉裝備").color(NamedTextColor.YELLOW))
            lore.add(Component.empty())
            lore.add(Component.text("基礎消耗: 3x 青金石").color(NamedTextColor.GRAY))
            if (lockedIndices.isNotEmpty()) {
                val cost = Math.pow(2.0, (lockedIndices.size - 1).toDouble()).toInt()
                lore.add(Component.text("鎖定消耗: $cost x 鑽石磚").color(NamedTextColor.AQUA))
            }
            meta.lore(lore)
            itemMeta = meta
        }
        inventory.setItem(REFORGE_BUTTON_SLOT, button)
    }

    private fun updateAffixDisplay() {
        val item = inventory.getItem(EQUIPMENT_SLOT)
        val affixes = item?.itemMeta?.persistentDataContainer?.get(Keys.AFFIXES, AffixListDataType) ?: emptyList()

        for (i in 0 until 5) {
            val displaySlot = AFFIX_DISPLAY_SLOTS[i]
            val lockSlot = LOCK_BUTTON_SLOTS[i]

            if (i < affixes.size) {
                val affix = affixes[i]
                val displayItem = ItemStack(Material.PAPER).apply {
                    val meta = itemMeta
                    meta.displayName(Component.text(affix.name).color(NamedTextColor.GREEN))
                    meta.lore(listOf(Component.text("數值: ${affix.value}").color(NamedTextColor.YELLOW)))
                    itemMeta = meta
                }
                inventory.setItem(displaySlot, displayItem)

                val isLocked = lockedIndices.contains(i)
                val lockItem = ItemStack(if (isLocked) Material.IRON_TRAPDOOR else Material.OAK_TRAPDOOR).apply {
                    val meta = itemMeta
                    meta.displayName(
                        Component.text(if (isLocked) "已鎖定" else "未鎖定")
                            .color(if (isLocked) NamedTextColor.RED else NamedTextColor.GRAY)
                    )
                    if (i == 4) {
                        meta.lore(listOf(Component.text("第 5 個詞條無法鎖定").color(NamedTextColor.DARK_RED)))
                    } else {
                        meta.lore(listOf(Component.text("點擊切換鎖定狀態").color(NamedTextColor.YELLOW)))
                    }
                    itemMeta = meta
                }
                inventory.setItem(lockSlot, lockItem)
            } else {
                inventory.setItem(displaySlot, null)
                inventory.setItem(lockSlot, null)
            }
        }
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        if (event.inventory != inventory) return
        
        val slot = event.rawSlot
        
        if (slot < 54 && (inventory.getItem(slot)?.type == Material.GRAY_STAINED_GLASS_PANE || slot == REFORGE_BUTTON_SLOT || slot in AFFIX_DISPLAY_SLOTS)) {
            event.isCancelled = true
        }

        if (slot in LOCK_BUTTON_SLOTS) {
            event.isCancelled = true
            val index = LOCK_BUTTON_SLOTS.indexOf(slot)
            if (index == 4) return
            
            val item = inventory.getItem(EQUIPMENT_SLOT)
            val affixes = item?.itemMeta?.persistentDataContainer?.get(Keys.AFFIXES, AffixListDataType) ?: emptyList()
            if (index >= affixes.size) return

            if (lockedIndices.contains(index)) {
                lockedIndices.remove(index)
            } else {
                if (lockedIndices.size < 4) {
                    lockedIndices.add(index)
                } else {
                    player.sendMessage(Component.text("最多隻能鎖定 4 個詞條").color(NamedTextColor.RED))
                }
            }
            updateAffixDisplay()
            updateReforgeButton()
            return
        }

        if (slot == REFORGE_BUTTON_SLOT) {
            event.isCancelled = true
            handleReforge()
            return
        }

        Bukkit.getScheduler().runTask(plugin, Runnable {
            if (slot == EQUIPMENT_SLOT || event.isShiftClick) {
                updateAffixDisplay()
            }
        })
    }

    private fun handleReforge() {
        val item = inventory.getItem(EQUIPMENT_SLOT) ?: return
        val lapis = inventory.getItem(LAPIS_SLOT) ?: ItemStack(Material.AIR)
        val diamonds = inventory.getItem(DIAMOND_BLOCK_SLOT) ?: ItemStack(Material.AIR)

        if (lapis.type != Material.LAPIS_LAZULI || lapis.amount < 3) {
            player.sendMessage(Component.text("青金石不足 (需要 3 個)").color(NamedTextColor.RED))
            return
        }

        val diamondCost = if (lockedIndices.isEmpty()) 0 else Math.pow(2.0, (lockedIndices.size - 1).toDouble()).toInt()
        if (diamondCost > 0) {
            if (diamonds.type != Material.DIAMOND_BLOCK || diamonds.amount < diamondCost) {
                player.sendMessage(Component.text("鑽石磚不足 (需要 $diamondCost 個)").color(NamedTextColor.RED))
                return
            }
        }

        val result = ReforgeManager.reforgeItem(item, lockedIndices.toList())
        if (result is ReforgeResult.Success) {
            lapis.amount -= 3
            if (diamondCost > 0) diamonds.amount -= diamondCost
            
            player.sendMessage(Component.text("重鑄成功！").color(NamedTextColor.GREEN))
            updateAffixDisplay()
        } else if (result is ReforgeResult.Failure) {
            player.sendMessage(Component.text("失敗: ${result.message}").color(NamedTextColor.RED))
        }
    }

    @EventHandler
    fun onClose(event: InventoryCloseEvent) {
        if (event.inventory != inventory) return
        
        listOf(EQUIPMENT_SLOT, LAPIS_SLOT, DIAMOND_BLOCK_SLOT).forEach { slot ->
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
