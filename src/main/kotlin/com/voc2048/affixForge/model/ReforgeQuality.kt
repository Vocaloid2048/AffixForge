package com.voc2048.affixForge.model

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor

enum class ReforgeQuality(
    val displayName: String,
    val color: TextColor,
    val minAffixes: Int,
    val maxAffixes: Int
) {
    COMMON("普通", NamedTextColor.WHITE, 1, 1),
    UNCOMMON("罕見", NamedTextColor.GREEN, 2, 2),
    RARE("稀有", NamedTextColor.BLUE, 3, 3),
    EPIC("史詩", NamedTextColor.LIGHT_PURPLE, 4, 4),
    LEGENDARY("傳奇", NamedTextColor.GOLD, 5, 5);

    fun getFormattedName(): Component {
        return Component.text(displayName).color(color)
    }
}
