package com.voc2048.affixForge.data

import com.voc2048.affixForge.model.AffixType
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

data class AffixTemplate(
    val id: String,
    val name: String,
    val type: AffixType,
    val minValue: Double,
    val maxValue: Double,
    val weight: Int
)

object AffixPool {
    private val templates = mutableListOf<AffixTemplate>()
    private var totalWeight = 0

    fun load(plugin: JavaPlugin) {
        val file = File(plugin.dataFolder, "affix_pool.yml")
        if (!file.exists()) {
            plugin.saveResource("affix_pool.yml", false)
        }

        val config = YamlConfiguration.loadConfiguration(file)
        templates.clear()
        totalWeight = 0

        val section = config.getConfigurationSection("affixes") ?: return
        for (id in section.getKeys(false)) {
            val name = section.getString("$id.name") ?: id
            val typeStr = section.getString("$id.type") ?: "BASE_STAT"
            val type = try { AffixType.valueOf(typeStr) } catch (e: Exception) { AffixType.BASE_STAT }
            val min = section.getDouble("$id.min_value")
            val max = section.getDouble("$id.max_value")
            val weight = section.getInt("$id.weight", 1)

            templates.add(AffixTemplate(id, name, type, min, max, weight))
            totalWeight += weight
        }
    }

    fun getRandomTemplate(): AffixTemplate? {
        if (templates.isEmpty()) return null
        var random = (1..totalWeight).random()
        for (template in templates) {
            random -= template.weight
            if (random <= 0) return template
        }
        return templates.random()
    }
}
