package com.voc2048.affixForge.renderer

import com.voc2048.affixForge.data.AffixRegistry
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

    fun render(item: ItemStack, quality: ReforgeQuality, affixes: List<EquipmentAffix>) {
        val meta = item.itemMeta ?: return
        val lore = mutableListOf<Component>()

        // 1. 第一行：品質與槽位狀態
        val maxSlots = item.maxSlots
        val usedSlots = affixes.size // 目前簡單以詞條數量計算，後續若附魔佔位可調整
        
        lore.add(Component.empty())
        lore.add(
            quality.getFormattedName()
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text("  "))
                .append(Component.text("槽位: ").color(NamedTextColor.GRAY))
                .append(Component.text("$usedSlots/$maxSlots").color(NamedTextColor.WHITE))
        )

        lore.add(Component.text("--------------------").color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false))

        // 2. 中間：已激活詞條
        if (affixes.isEmpty()) {
            lore.add(Component.text("尚未鑑定").color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, true))
        } else {
            affixes.forEach { affix ->
                val template = AffixRegistry.getTemplate(affix.id)
                val romanLevel = ROMAN_NUMERALS[affix.level] ?: affix.level.toString()
                
                // 根據數值格式化顯示文字 (例如百分比)
                val isPercentage = affix.id.contains("rate") || affix.id.contains("damage") || affix.id.contains("steal") || affix.id.contains("contract") || affix.id.contains("speed")
                val valueDisplay = if (isPercentage) {
                    String.format("%.0f%%", affix.value * 100)
                } else {
                    String.format("%.1f", affix.value)
                }

                val line = Component.text(" ⚔ ")
                    .color(NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text("${affix.name} $romanLevel").color(NamedTextColor.WHITE))
                    .append(Component.text(" (").color(NamedTextColor.GRAY))
                    .append(Component.text(if (affix.value >= 0) "+$valueDisplay" else valueDisplay).color(NamedTextColor.YELLOW))
                    .append(Component.text(")").color(NamedTextColor.GRAY))
                
                lore.add(line)
            }
        }

        // 3. 底部：原版附魔佔位提示 (如果有附魔)
        val enchants = item.enchantments
        if (enchants.isNotEmpty()) {
            lore.add(Component.empty())
            lore.add(Component.text("⚠️ 原版附魔已佔用額外空間").color(NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, true))
        }

        lore.add(Component.text("--------------------").color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false))

        meta.lore(lore)
        item.itemMeta = meta
    }
}
