package com.voc2048.affixForge.data

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object CustomItems {
    fun getChalcedonyIngot(): ItemStack {
        return ItemStack(Material.COPPER_INGOT).apply {
            editMeta { meta ->
                meta.displayName(
                    Component.text("玉髓錠")
                        .color(NamedTextColor.LIGHT_PURPLE)
                        .decoration(TextDecoration.ITALIC, false)
                )
                meta.lore(listOf(
                    Component.text("極其稀有的鍛造材料").color(NamedTextColor.GRAY),
                    Component.text("可用於擴張裝備的槽位上限").color(NamedTextColor.YELLOW)
                ))
            }
        }
    }

    fun isChalcedonyIngot(item: ItemStack?): Boolean {
        if (item == null || item.type != Material.COPPER_INGOT) return false
        val meta = item.itemMeta ?: return false
        return meta.hasDisplayName() && meta.displayName()?.let { 
            it is Component && (it as Component).contains(Component.text("玉髓錠"))
        } ?: false
    }
}
