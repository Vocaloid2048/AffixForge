package com.voc2048.affixForge.logic

import com.voc2048.affixForge.data.AffixListDataType
import com.voc2048.affixForge.data.Keys
import com.voc2048.affixForge.model.AffixType
import com.voc2048.affixForge.model.EquipmentAffix
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

object StatManager {
    private val MODIFIER_NAME = "AffixForge_Stat"
    
    // 兼容 1.20.5+ 的屬性名稱
    private val ATTR_MAX_HEALTH = try { Attribute.valueOf("MAX_HEALTH") } catch (e: Exception) { Attribute.valueOf("GENERIC_MAX_HEALTH") }
    private val ATTR_ATTACK_DAMAGE = try { Attribute.valueOf("ATTACK_DAMAGE") } catch (e: Exception) { Attribute.valueOf("GENERIC_ATTACK_DAMAGE") }
    private val ATTR_MOVEMENT_SPEED = try { Attribute.valueOf("MOVEMENT_SPEED") } catch (e: Exception) { Attribute.valueOf("GENERIC_MOVEMENT_SPEED") }

    fun updatePlayerStats(player: Player) {
        val allAffixes = mutableListOf<EquipmentAffix>()
        val setCounts = mutableMapOf<String, Int>()

        // 1. 收集裝備與飾品的詞條
        val itemsToScan = mutableListOf<ItemStack?>()
        itemsToScan.addAll(player.inventory.armorContents)
        itemsToScan.add(player.inventory.itemInMainHand)
        itemsToScan.add(player.inventory.itemInOffHand)
        
        // 掃描背包中的自定義飾品
        player.inventory.contents.forEach { item ->
            if (item != null && item.itemMeta?.persistentDataContainer?.has(Keys.CUSTOM_ACCESSORY, PersistentDataType.BYTE) == true) {
                itemsToScan.add(item)
            }
        }

        itemsToScan.filterNotNull().distinct().forEach { item ->
            val meta = item.itemMeta ?: return@forEach
            val affixes = meta.persistentDataContainer.get(Keys.AFFIXES, AffixListDataType) ?: return@forEach
            allAffixes.addAll(affixes)

            // 套裝檢查
            val setId = meta.persistentDataContainer.get(Keys.SET_ID, PersistentDataType.STRING)
            if (setId != null) {
                setCounts[setId] = setCounts.getOrDefault(setId, 0) + 1
            }
        }

        // 2. 彙總屬性
        val statTotals = mutableMapOf<String, Double>()
        allAffixes.filter { it.type == AffixType.BASE_STAT || it.type == AffixType.SPECIAL_MECHANIC }.forEach { affix ->
            statTotals[affix.id] = statTotals.getOrDefault(affix.id, 0.0) + affix.value
        }

        // 3. 應用屬性 (Vanilla Attributes)
        var extraHealthFromSets = 0.0
        setCounts.forEach { (_, count) ->
            if (count >= 4) {
                extraHealthFromSets += 10.0
            } else if (count >= 2) {
                extraHealthFromSets += 5.0
            }
        }

        applyVanillaAttribute(player, ATTR_MAX_HEALTH, (statTotals["max_health"] ?: 0.0) + extraHealthFromSets)
        applyVanillaAttribute(player, ATTR_ATTACK_DAMAGE, statTotals["attack_damage"] ?: 0.0)
        applyVanillaAttribute(player, ATTR_MOVEMENT_SPEED, statTotals["movement_speed"] ?: 0.0)
    }

    private fun applyVanillaAttribute(player: Player, attribute: Attribute, value: Double) {
        val instance = player.getAttribute(attribute) ?: return
        
        // 移除舊的加成
        instance.modifiers.forEach { modifier ->
            if (modifier.name == MODIFIER_NAME) {
                instance.removeModifier(modifier)
            }
        }

        if (value != 0.0) {
            // 使用基於屬性名稱的 UUID 保持一致性
            val uuid = UUID.nameUUIDFromBytes(attribute.name().toByteArray())
            @Suppress("DEPRECATION")
            val modifier = AttributeModifier(uuid, MODIFIER_NAME, value, AttributeModifier.Operation.ADD_NUMBER)
            instance.addModifier(modifier)
        }
    }
}
