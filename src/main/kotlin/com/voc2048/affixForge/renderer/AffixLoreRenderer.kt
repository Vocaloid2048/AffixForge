package com.voc2048.affixForge.renderer

import com.voc2048.affixForge.model.EquipmentAffix
import com.voc2048.affixForge.model.ReforgeQuality
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.inventory.ItemStack

object AffixLoreRenderer {
    fun render(item: ItemStack, quality: ReforgeQuality, affixes: List<EquipmentAffix>) {
        val meta = item.itemMeta ?: return
        val lore = mutableListOf<Component>()

        // 空行分隔
        lore.add(Component.empty())

        // 品級顯示
        lore.add(
            Component.text("品級: ")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)
                .append(quality.getFormattedName().decoration(TextDecoration.BOLD, true))
        )

        lore.add(Component.text("--------------------").color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false))

        // 詞條顯示
        if (affixes.isEmpty()) {
            lore.add(Component.text("尚未鑑定").color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, true))
        } else {
            affixes.forEach { affix ->
                val prefix = if (affix.value >= 0) "+" else ""
                lore.add(
                    Component.text(" • ")
                        .color(NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false)
                        .append(Component.text(affix.name).color(NamedTextColor.GRAY))
                        .append(Component.text(": ").color(NamedTextColor.GRAY))
                        .append(Component.text("$prefix${affix.value}").color(NamedTextColor.YELLOW))
                )
            }
        }

        lore.add(Component.text("--------------------").color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false))

        meta.lore(lore)
        item.itemMeta = meta
    }
}
