package com.voc2048.affixForge.gui

import com.voc2048.affixForge.data.AffixListDataType
import com.voc2048.affixForge.data.Keys
import com.voc2048.affixForge.logic.ReforgeManager
import com.voc2048.affixForge.model.ReforgeResult
import com.voc2048.affixForge.util.isAuthenticated
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
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

class ReforgingGUI(private val plugin: JavaPlugin, private val player: Player) : Listener {

    private val inventory: Inventory = Bukkit.createInventory(null, 54, Component.text("鍛造洗練與鎖定台"))
    private val lockedIndices = mutableSetOf<Int>()

    companion object {
        const val EQUIPMENT_SLOT = 13
        const val LAPIS_SLOT = 21
        const val DIAMOND_BLOCK_SLOT = 23
        const val REFORGE_BUTTON_SLOT = 31
        
        // 鎖定區域為 36-53
        val LOCK_ZONE = 36..53
        val LOCK_SLOTS = listOf(47, 48, 49, 50, 51)
        
        private val ROMAN_NUMERALS = mapOf(1 to "I", 2 to "II", 3 to "III", 4 to "IV", 5 to "V")
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
            if (i == EQUIPMENT_SLOT || i == LAPIS_SLOT || i == DIAMOND_BLOCK_SLOT || 
                i == REFORGE_BUTTON_SLOT || i in LOCK_ZONE) continue
            inventory.setItem(i, glass)
        }
        updateReforgeButton()
        updateAffixDisplay()
    }

    private fun updateReforgeButton() {
        val button = ItemStack(Material.ANVIL).apply {
            editMeta { meta ->
                meta.displayName(Component.text("開始洗練").color(NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false))
                val lore = mutableListOf<Component>()
                lore.add(Component.text("點擊隨機重置未鎖定詞條").color(NamedTextColor.YELLOW))
                lore.add(Component.empty())
                lore.add(Component.text("消耗: 3x 青金石").color(NamedTextColor.GRAY))
                
                // n^2 鑽石磚消耗
                val n = lockedIndices.size
                val diamondCost = n * n
                if (diamondCost > 0) {
                    lore.add(Component.text("鎖定消耗: $diamondCost x 鑽石磚").color(NamedTextColor.AQUA))
                }
                
                meta.lore(lore)
            }
        }
        inventory.setItem(REFORGE_BUTTON_SLOT, button)
    }

    private fun updateAffixDisplay() {
        // 清空鎖定區域
        for (slot in LOCK_ZONE) {
            inventory.setItem(slot, null)
        }

        val item = inventory.getItem(EQUIPMENT_SLOT)
        if (item == null || !item.isAuthenticated()) {
            lockedIndices.clear()
            updateReforgeButton()
            return
        }

        val affixes = item.itemMeta?.persistentDataContainer?.get(Keys.AFFIXES, AffixListDataType) ?: emptyList()

        // 依序擺放紙張/鎖定按鈕
        for (i in affixes.indices) {
            if (i >= LOCK_SLOTS.size) break
            val slot = LOCK_SLOTS[i]
            val affix = affixes[i]
            val isLocked = lockedIndices.contains(i)
            
            val roman = ROMAN_NUMERALS[affix.level] ?: affix.level.toString()
            val displayName = "§b鎖定詞條：§e【${affix.name} $roman】"
            
            val icon = ItemStack(if (isLocked) Material.ENCHANTED_BOOK else Material.PAPER).apply {
                editMeta { meta ->
                    meta.displayName(LegacyComponentSerializer.legacySection().deserialize(displayName).decoration(TextDecoration.ITALIC, false))
                    val lore = mutableListOf<Component>()
                    lore.add(Component.text(if (isLocked) "§c[ 已鎖定 ]" else "§7[ 未鎖定 ]").decoration(TextDecoration.ITALIC, false))
                    lore.add(Component.text("點擊切換鎖定狀態").color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
                    if (i == 4) {
                        lore.add(Component.text("第 5 個詞條無法鎖定").color(NamedTextColor.DARK_RED))
                    }
                    meta.lore(lore)
                }
            }
            inventory.setItem(slot, icon)
        }
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        if (event.inventory != inventory) return
        val slot = event.rawSlot
        
        if (slot < 54) {
            val item = inventory.getItem(slot)
            if (item?.type == Material.GRAY_STAINED_GLASS_PANE || slot == REFORGE_BUTTON_SLOT) {
                event.isCancelled = true
            }
            if (slot in LOCK_ZONE) {
                event.isCancelled = true
                handleLockClick(slot)
                return
            }
        }

        if (slot == REFORGE_BUTTON_SLOT) {
            event.isCancelled = true
            handleReforge()
            return
        }

        // 當物品放入或取出時更新
        Bukkit.getScheduler().runTask(plugin, Runnable {
            updateAffixDisplay()
            updateReforgeButton()
        })
    }

    private fun handleLockClick(slot: Int) {
        val index = LOCK_SLOTS.indexOf(slot)
        if (index == -1 || index == 4) return // 第 5 個詞條無法鎖定

        val item = inventory.getItem(EQUIPMENT_SLOT)
        if (item == null || !item.isAuthenticated()) return
        
        val affixes = item.itemMeta?.persistentDataContainer?.get(Keys.AFFIXES, AffixListDataType) ?: emptyList()
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
        player.playSound(player.location, Sound.UI_BUTTON_CLICK, 0.5f, 1.0f)
        updateAffixDisplay()
        updateReforgeButton()
    }

    private fun handleReforge() {
        val item = inventory.getItem(EQUIPMENT_SLOT)
        if (item == null || !item.isAuthenticated()) {
            player.sendMessage(Component.text("請放入已鑑定的裝備").color(NamedTextColor.RED))
            return
        }
        
        val lapis = inventory.getItem(LAPIS_SLOT) ?: ItemStack(Material.AIR)
        val diamonds = inventory.getItem(DIAMOND_BLOCK_SLOT) ?: ItemStack(Material.AIR)

        if (lapis.type != Material.LAPIS_LAZULI || lapis.amount < 3) {
            player.sendMessage(Component.text("青金石不足 (需要 3 個)").color(NamedTextColor.RED))
            return
        }

        val n = lockedIndices.size
        val diamondCost = n * n
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
            
            // 強制將物品重新放回 Slot 確保 GUI 刷新
            inventory.setItem(EQUIPMENT_SLOT, item)
            
            player.playSound(player.location, Sound.BLOCK_ANVIL_USE, 1f, 1.5f)
            player.sendMessage(Component.text("洗練完成！").color(NamedTextColor.GREEN))
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
