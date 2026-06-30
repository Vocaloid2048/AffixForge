package com.voc2048.affixForge.renderer

import com.voc2048.affixForge.data.AffixValueRegistry
import com.voc2048.affixForge.model.EquipmentAffix
import com.voc2048.affixForge.model.ReforgeQuality
import com.voc2048.affixForge.util.maxSlots
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.inventory.ItemStack

object AffixLoreRenderer {
    
    private val ROMAN_NUMERALS = mapOf(
        1 to "I", 2 to "II", 3 to "III", 4 to "IV", 5 to "V",
        6 to "VI", 7 to "VII", 8 to "VIII", 9 to "IX", 10 to "X"
    )

    /**
     * 渲染裝備的詞條 Lore
     * 確保品質名與詞條描述均能正確顯示
     */
    fun render(item: ItemStack, quality: ReforgeQuality, affixes: List<EquipmentAffix>) {
        item.editMeta { meta ->
            renderWithMeta(item, meta, quality, affixes)
        }
    }

    fun renderWithMeta(item: ItemStack, meta: org.bukkit.inventory.meta.ItemMeta, quality: ReforgeQuality, affixes: List<EquipmentAffix>) {
        val lore = mutableListOf<Component>()

        // 1. 品質與槽位信息
        // 注意：這裡直接從傳入的 meta 讀取或使用傳入的參數
        val maxSlots = meta.persistentDataContainer.get(com.voc2048.affixForge.data.Keys.MAX_SLOTS, org.bukkit.persistence.PersistentDataType.INTEGER) ?: 0
        val usedSlots = affixes.size 
        
        lore.add(Component.empty())
        
        // 品質顯示 (例如: [ 傳奇 ]  槽位: 3/5)
        val qualityComponent = quality.getFormattedName()
            .decoration(TextDecoration.BOLD, true)
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text("  "))
            .append(Component.text("槽位: ").color(NamedTextColor.GRAY).decoration(TextDecoration.BOLD, false))
            .append(Component.text("$usedSlots/$maxSlots").color(NamedTextColor.WHITE).decoration(TextDecoration.BOLD, false))
        
        lore.add(qualityComponent)
        lore.add(Component.text("--------------------").color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false))

        // 2. 詞條列表
        if (affixes.isEmpty()) {
            lore.add(Component.text("  未發現任何詞條").color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, true))
        } else {
            affixes.forEach { affix ->
                val romanLevel = ROMAN_NUMERALS[affix.level] ?: affix.level.toString()
                val description = AffixValueRegistry.getDescription(affix.id, affix.level)

                // 詞條行渲染:  ◆ 狂戰士契約 V (描述...)
                // 使用單個 Component 構建，避免 newline() 導致的組件分離問題，並確保描述緊跟其後
                val affixLine = Component.text(" ◆ ")
                    .color(NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text("${affix.name} $romanLevel").color(NamedTextColor.YELLOW))
                    .append(Component.text("\n   └ ").color(NamedTextColor.DARK_GRAY))
                    .append(Component.text(description).color(NamedTextColor.GRAY))
                
                lore.add(affixLine)
            }
        }

        // 3. 底部裝飾與提示
        val enchants = item.enchantments
        if (enchants.isNotEmpty()) {
            lore.add(Component.empty())
            lore.add(Component.text(" ! ").color(NamedTextColor.RED).append(Component.text("原版附魔已共鳴").color(NamedTextColor.DARK_RED).decoration(TextDecoration.ITALIC, true)))
        }

        lore.add(Component.text("--------------------").color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false))

        meta.lore(lore)
    }
}
