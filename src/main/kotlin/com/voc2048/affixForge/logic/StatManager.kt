package com.voc2048.affixForge.logic

import com.voc2048.affixForge.data.AffixListDataType
import com.voc2048.affixForge.data.Keys
import com.voc2048.affixForge.model.AffixType
import com.voc2048.affixForge.model.EquipmentAffix
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.attribute.AttributeModifier.Operation
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin

object StatManager {
    private lateinit var plugin: JavaPlugin

    fun init(plugin: JavaPlugin) {
        this.plugin = plugin
    }

    private val ATTR_MAX_HEALTH = getAttribute("MAX_HEALTH", "GENERIC_MAX_HEALTH")
    private val ATTR_ATTACK_DAMAGE = getAttribute("ATTACK_DAMAGE", "GENERIC_ATTACK_DAMAGE")
    private val ATTR_MOVEMENT_SPEED = getAttribute("MOVEMENT_SPEED", "GENERIC_MOVEMENT_SPEED")
    private val ATTR_SCALE = getAttribute("SCALE", "GENERIC_SCALE")

    private fun getAttribute(vararg names: String): Attribute? {
        for (name in names) {
            try { return Attribute.valueOf(name) } catch (e: Exception) { continue }
        }
        return null
    }

    fun updatePlayerStats(player: Player) {
        val allAffixes = mutableListOf<EquipmentAffix>()
        val setCounts = mutableMapOf<String, Int>()

        val itemsToScan = mutableListOf<ItemStack?>()
        itemsToScan.addAll(player.inventory.armorContents)
        itemsToScan.add(player.inventory.itemInMainHand)
        itemsToScan.add(player.inventory.itemInOffHand)
        
        // 修正：不再掃描整個背包，僅限裝備欄與手持物品
        // player.inventory.contents.forEach { item -> ... }

        itemsToScan.filterNotNull().distinct().forEach { item ->
            val meta = item.itemMeta ?: return@forEach
            val affixes = meta.persistentDataContainer.get(Keys.AFFIXES, AffixListDataType) ?: return@forEach
            allAffixes.addAll(affixes)

            val setId = meta.persistentDataContainer.get(Keys.SET_ID, PersistentDataType.STRING)
            if (setId != null) {
                setCounts[setId] = setCounts.getOrDefault(setId, 0) + 1
            }
        }

        val statTotals = mutableMapOf<String, Double>()
        allAffixes.forEach { affix ->
            if (affix.type == AffixType.STAT) {
                statTotals[affix.id] = statTotals.getOrDefault(affix.id, 0.0) + (affix.value * affix.level)
            }
            // 特別處理裝飾類體型變化
            if (affix.id == "size_reduce") {
                statTotals["scale"] = statTotals.getOrDefault("scale", 0.0) - (affix.level * 0.20)
            }
            if (affix.id == "size_enlarge") {
                statTotals["scale"] = statTotals.getOrDefault("scale", 0.0) + (affix.level * 0.20)
            }
        }

        // 套裝加成
        var extraHealthFromSets = 0.0
        setCounts.forEach { (_, count) ->
            if (count >= 4) extraHealthFromSets += 10.0
            else if (count >= 2) extraHealthFromSets += 5.0
        }

        // 應用屬性
        ATTR_MAX_HEALTH?.let { applyAttribute(player, it, (statTotals["max_health"] ?: 0.0) + extraHealthFromSets) }
        ATTR_ATTACK_DAMAGE?.let { applyAttribute(player, it, statTotals["attack_damage"] ?: 0.0) }
        ATTR_MOVEMENT_SPEED?.let { applyAttribute(player, it, statTotals["movement_speed"] ?: 0.0) }
        ATTR_SCALE?.let { applyAttribute(player, it, statTotals["scale"] ?: 0.0) }
    }

    private fun applyAttribute(player: Player, attribute: Attribute, value: Double) {
        val instance = player.getAttribute(attribute) ?: return
        val key = NamespacedKey(plugin, "affix_${attribute.name().lowercase()}")

        instance.modifiers.forEach { modifier ->
            if (modifier.key == key) {
                instance.removeModifier(modifier)
            }
        }

        if (value != 0.0) {
            val modifier = AttributeModifier(key, value, Operation.ADD_NUMBER, EquipmentSlotGroup.ANY)
            instance.addModifier(modifier)
        }
    }
}
