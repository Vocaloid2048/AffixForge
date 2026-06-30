package com.voc2048.affixForge.model

enum class AffixType {
    BASE_STAT,         // 血量上限、攻擊力、移動速度等
    ELEMENTAL_EFFECT,  // 攻擊附帶凋零/燃燒，受擊觸發機率護盾
    SPECIAL_MECHANIC,  // 暴擊率/暴擊傷害、吸血、背擊加成
    SET_BONUS          // 套裝共鳴
}

data class EquipmentAffix(
    val id: String,
    val name: String,
    val type: AffixType,
    val value: Double,
    val level: Int = 1
)
