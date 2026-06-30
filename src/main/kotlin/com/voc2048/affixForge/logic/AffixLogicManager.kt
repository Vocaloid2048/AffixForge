package com.voc2048.affixForge.logic

import com.voc2048.affixForge.data.AffixRegistry
import com.voc2048.affixForge.model.ReforgeQuality
import com.voc2048.affixForge.util.*
import com.voc2048.affixForge.renderer.AffixLoreRenderer
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta

object AffixLogicManager {

    /**
     * 實作「鑑定」邏輯
     * 消耗：1個青金石磚 (在調用處檢查)
     */
    fun authenticateItem(item: ItemStack): Boolean {
        if (item.isAuthenticated()) return false

        // 依照機率賦予品質 (模擬 gameLogic.md 邏輯)
        val roll = (1..100).random()
        val quality = when {
            roll > 95 -> ReforgeQuality.LEGENDARY
            roll > 85 -> ReforgeQuality.EPIC
            roll > 70 -> ReforgeQuality.RARE
            roll > 40 -> ReforgeQuality.UNCOMMON
            else -> ReforgeQuality.COMMON
        }

        item.quality = quality
        // 初始最大槽位依照品質設定 (例如 Common 2, Legendary 5)
        item.maxSlots = quality.maxAffixes 

        // 生成初始詞條
        val initialAffixes = AffixRegistry.rollRandomAffixes(quality)
        item.setAffixes(initialAffixes)

        // 渲染 Lore
        AffixLoreRenderer.render(item, quality, initialAffixes)

        return true
    }

    /**
     * 實作「玉髓錠打洞」API
     * 上限 16
     */
    fun expandSlot(item: ItemStack): Boolean {
        if (!item.isAuthenticated()) return false
        val current = item.maxSlots
        if (current >= 16) return false
        
        item.maxSlots = current + 1
        return true
    }

    /**
     * 實作「鑽石磚升級」API
     */
    fun upgradeAffixLevel(item: ItemStack, affixId: String, maxLevel: Int = 10): Boolean {
        if (!item.isAuthenticated()) return false
        val affixes = item.getAffixes().toMutableList()
        val index = affixes.indexOfFirst { it.id == affixId }
        
        if (index == -1) return false
        val target = affixes[index]
        
        if (target.level >= maxLevel) return false
        
        // 升級邏輯：等級+1，數值按比例提升 (例如每級增加基礎值的 20%)
        val baseValue = target.value / target.level
        val newLevel = target.level + 1
        val newValue = baseValue * newLevel
        
        affixes[index] = target.copy(level = newLevel, value = Math.round(newValue * 100) / 100.0)
        item.setAffixes(affixes)
        
        // 重新渲染 Lore
        item.quality?.let { AffixLoreRenderer.render(item, it, affixes) }
        
        return true
    }

    /**
     * 實作萬用附魔轉移書邏輯
     */
    fun extractEnchantments(equipment: ItemStack, emptyBook: ItemStack): ItemStack? {
        if (emptyBook.type != Material.BOOK && emptyBook.type != Material.ENCHANTED_BOOK) return null
        val enchants = equipment.enchantments
        if (enchants.isEmpty()) return null

        val resultBook = ItemStack(Material.ENCHANTED_BOOK)
        val meta = resultBook.itemMeta
        if (meta is EnchantmentStorageMeta) {
            enchants.forEach { (enchant, level) ->
                meta.addStoredEnchant(enchant, level, true)
                equipment.removeEnchantment(enchant)
            }
            resultBook.itemMeta = meta
        }
        
        return resultBook
    }
}
