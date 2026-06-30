package com.voc2048.affixForge.data

import kotlin.math.roundToInt

object AffixValueRegistry {

    /**
     * 根據詞條 ID 與等級計算其數值並返回描述文字
     * 確保此處的 ID 與 affixes.yml 中的 Key 完全一致
     */
    fun getDescription(id: String, level: Int): String {
        return when (id) {
            // 戰術輸出類
            "berserker" -> "造成的傷害提升 ${level * 8}%，但自身受到的所有傷害也提升 ${level * 4}%。"
            "armor_piercing" -> "攻擊時無視目標 ${level * 10}% 的護甲值。"
            "backstab" -> "從背後襲擊敵人時，造成的傷害額外提升 ${level * 15}%。"
            "last_stand" -> "自身血量每降低 10%，造成的傷害便提升 ${level * 2}%。"
            "combo_finisher" -> "連續擊中 5 次後，第 6 次近戰攻擊造成 200% 傷害。"

            // 生存防禦類
            "phase_shift" -> "受到傷害時有 ${level * 5}% 機率無敵 0.5 秒（冷卻 15 秒）。"
            "reflective_shell" -> "受到遠程傷害時有 ${level * 15}% 機率將傷害反彈給發射者。"
            "life_steal" -> "造成近戰傷害時，將傷害量的 ${level * 3}% 轉化為自身生命值。"
            "lifeline_shield" -> "血量低於 20% 時，獲得持續 ${level * 3} 秒的傷害吸收護盾（冷卻 60 秒）。"

            // 功能探索類
            "geological_radar" -> "蹲下時掃描周圍 ${level * 4} 格內的稀有礦石並使其發光。"
            "xp_vacuum" -> "自動吸引周圍 ${level * 2} 格半徑內的經驗球與掉落物。"
            "hydro_phobic" -> {
                val speed = level * 25
                if (level >= 2) "在水中移動速度增加 $speed%，且完全免疫挖掘減速。" 
                else "在水中移動速度增加 $speed%。"
            }
            "looting_mastery" -> "擊殺生物時的「掠奪」等級額外提升 $level 級。"

            // 惡魔詛咒類
            "fragile" -> "每次消耗道具耐久度時，額外多扣除 3 點耐久。"
            "pearl_void" -> "擊殺終界使者時，絕對不會掉落終界珍珠。"

            // 裝飾類
            "rainbow" -> "裝備在世界中會呈現彩虹色的粒子效果。"
            "shimmer" -> "裝備在世界中會呈現光影流動的粒子效果。"
            "size_reduce" -> "讓體型縮小 ${level * 20}%。"
            "size_enlarge" -> "讓體型放大 ${level * 20}%。"

            // 基礎屬性類
            "max_health" -> "最大生命值提升 ${level * 2} 點。"
            "attack_damage" -> "基礎攻擊力提升 ${level} 點。"
            "movement_speed" -> "移動速度提升 ${level * 2}%。"

            else -> "（未知詞條效果: $id）"
        }
    }

    /**
     * 獲取詞條的具體數值 (用於邏輯計算)
     */
    fun getValue(id: String, level: Int, key: String = "default"): Double {
        return when (id) {
            "berserker" -> if (key == "boost") level * 0.08 else level * 0.04
            "armor_piercing" -> level * 0.10
            "backstab" -> level * 0.15
            "last_stand" -> level * 0.02
            "phase_shift" -> level * 0.05
            "reflective_shell" -> level * 0.15
            "life_steal" -> level * 0.03
            "lifeline_shield" -> level * 0.15
            "geological_radar" -> (level * 4).toDouble()
            "xp_vacuum" -> (level * 2).toDouble()
            "hydro_phobic" -> level * 0.25
            "size_reduce", "size_enlarge" -> level * 0.20
            "max_health" -> level * 2.0
            "attack_damage" -> level * 1.0
            "movement_speed" -> level * 0.02
            else -> 0.0
        }
    }
}
