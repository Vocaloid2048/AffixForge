package com.voc2048.affixForge.data

object AffixValueRegistry {

    /**
     * 根據詞條 ID 與等級計算其數值並返回描述文字
     */
    fun getDescription(id: String, level: Int): String {
        return when (id) {
            "berserker" -> "造成的傷害提升 ${level * 8}%，但自身受到的所有傷害也提升 ${level * 4}%。"
            "armor_piercing" -> "攻擊時無視目標 ${level * 10}% 的護甲值。"
            "backstab" -> "從背後襲擊敵人時，造成的傷害額外提升 ${level * 15}%。"
            "last_stand" -> "自身血量每降低 10%，造成的傷害便提升 ${level * 2}%。"
            "combo_finisher" -> "連續 5 次近戰擊中目標未中斷，第 6 次攻擊造成 200% 傷害。"

            "phase_shift" -> "受到傷害時，有 ${level * 5}% 的機率進入 0.5 秒的完全無敵狀態（冷卻 15 秒）。"
            "reflective_shell" -> "受到遠程投射物傷害時，有 ${level * 15}% 的機率將彈頭原路反彈給攻擊者。"
            "life_steal" -> "造成近戰傷害時，將傷害量的 ${level * 3}% 轉化為自身生命值。"
            "lifeline_shield" -> "當血量低於 20% 時，獲得等同最大生命值 ${level * 15}% 的傷害吸收盾（冷卻 60 秒）。"

            "geological_radar" -> "切換為蹲下狀態時，顯示周圍 ${level * 4} 格內稀有礦石的輪廓。"
            "xp_vacuum" -> "自動吸附周圍經驗球與掉落物的半徑增加 ${level * 2} 格。"
            "hydro_phobic" -> if (level >= 2) "在水中移動速度增加 ${level * 25}%，且完全免疫挖掘減速。" else "在水中移動速度增加 ${level * 25}%。"
            "looting_mastery" -> "擊殺生物時的「掠奪」效果額外提升 $level 級。"

            "fragile" -> "每次消耗道具耐久度時，額外多扣除 3 點耐久。"
            "pearl_void" -> "擊殺終界使者時，絕對不會掉落終界珍珠。"

            "rainbow" -> "武器或飾物在世界中會呈現彩虹色的粒子效果。"
            "shimmer" -> "武器或飾物在世界中會呈現光影流動的粒子效果。"
            "size_reduce" -> "讓用戶能夠變小，縮小比例為 ${level * 20}%。"
            "size_enlarge" -> "讓用戶能夠變大，放大比例為 ${level * 20}%。"

            "max_health" -> "生命值上限提升 ${level * 2} 點。"
            "attack_damage" -> "基礎攻擊力提升 ${level} 點。"
            "movement_speed" -> "移動速度提升 ${level * 2}%。"

            else -> "未知的詞條效果"
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
