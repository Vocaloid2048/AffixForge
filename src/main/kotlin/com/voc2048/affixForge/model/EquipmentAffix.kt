package com.voc2048.affixForge.model

enum class AffixType {
    STAT,      // 屬性加成 (由 StatManager 處理)
    ABILITY    // 特殊能力 (由 GameplayListener 處理)
}

data class EquipmentAffix(
    val id: String,
    val name: String,
    val type: AffixType,
    val value: Double,
    val level: Int = 1
)
