package com.voc2048.affixForge.logic

import com.voc2048.affixForge.data.AffixPool
import com.voc2048.affixForge.model.EquipmentAffix
import com.voc2048.affixForge.model.ReforgeQuality
import kotlin.math.roundToInt

object AffixGenerator {
    fun generateRandomAffixes(quality: ReforgeQuality): List<EquipmentAffix> {
        val count = if (quality.minAffixes == quality.maxAffixes) {
            quality.minAffixes
        } else {
            (quality.minAffixes..quality.maxAffixes).random()
        }

        val affixes = mutableListOf<EquipmentAffix>()
        val usedTemplates = mutableSetOf<String>()

        repeat(count) {
            val template = AffixPool.getRandomTemplate()
            if (template != null && !usedTemplates.contains(template.id)) {
                val value = rollValue(template.minValue, template.maxValue)
                affixes.add(
                    EquipmentAffix(
                        id = template.id,
                        name = template.name,
                        type = template.type,
                        value = value
                    )
                )
                usedTemplates.add(template.id)
            }
        }

        return affixes
    }

    private fun rollValue(min: Double, max: Double): Double {
        val rawValue = min + (Math.random() * (max - min))
        // 保留兩位小數
        return (rawValue * 100).roundToInt() / 100.0
    }
}
