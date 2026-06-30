package com.voc2048.affixForge.data

import com.voc2048.affixForge.model.AffixType
import com.voc2048.affixForge.model.EquipmentAffix
import com.voc2048.affixForge.model.ReforgeQuality
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

data class AffixTemplate(
    val id: String,
    val displayName: String,
    val type: AffixType,
    val maxLevel: Int,
    val weight: Int,
    val baseValue: Double,
    val valuePerLevel: Double
)

object AffixRegistry {
    private val templates = mutableMapOf<String, AffixTemplate>()
    private var totalWeight = 0

    fun load(plugin: JavaPlugin) {
        val file = File(plugin.dataFolder, "affixes.yml")
        if (!file.exists()) {
            plugin.saveResource("affixes.yml", false)
        }

        val config = YamlConfiguration.loadConfiguration(file)
        templates.clear()
        totalWeight = 0

        val section = config.getConfigurationSection("affixes") ?: return
        for (id in section.getKeys(false)) {
            val displayName = section.getString("$id.display_name") ?: id
            val typeStr = section.getString("$id.type") ?: "ABILITY"
            val type = try { AffixType.valueOf(typeStr) } catch (e: Exception) { AffixType.ABILITY }
            val maxLevel = section.getInt("$id.max_level", 1)
            val weight = section.getInt("$id.weight", 10)
            val baseValue = section.getDouble("$id.base_value", 0.0)
            val valuePerLevel = section.getDouble("$id.value_per_level", 0.0)

            val template = AffixTemplate(id, displayName, type, maxLevel, weight, baseValue, valuePerLevel)
            templates[id] = template
            totalWeight += weight
        }
        
        plugin.logger.info("已加載 ${templates.size} 個全域詞條模板 (總權重: $totalWeight)")
    }

    fun rollRandomAffixes(quality: ReforgeQuality): List<EquipmentAffix> {
        val count = if (quality.minAffixes == quality.maxAffixes) {
            quality.minAffixes
        } else {
            (quality.minAffixes..quality.maxAffixes).random()
        }

        val rolledAffixes = mutableListOf<EquipmentAffix>()
        val availableTemplates = templates.values.toMutableList()

        repeat(count) {
            if (availableTemplates.isEmpty()) return@repeat
            
            val template = pickWeighted(availableTemplates)
            if (template != null) {
                rolledAffixes.add(
                    EquipmentAffix(
                        id = template.id,
                        name = template.displayName,
                        type = template.type,
                        value = template.baseValue,
                        level = 1
                    )
                )
                availableTemplates.remove(template)
            }
        }
        return rolledAffixes
    }

    private fun pickWeighted(pool: List<AffixTemplate>): AffixTemplate? {
        val currentTotalWeight = pool.sumOf { it.weight }
        if (currentTotalWeight <= 0) return null
        
        var random = (1..currentTotalWeight).random()
        for (template in pool) {
            random -= template.weight
            if (random <= 0) return template
        }
        return pool.random()
    }

    fun getTemplate(id: String): AffixTemplate? = templates[id]
}
