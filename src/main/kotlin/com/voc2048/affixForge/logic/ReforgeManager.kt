package com.voc2048.affixForge.logic

import com.voc2048.affixForge.data.AffixListDataType
import com.voc2048.affixForge.data.AffixPool
import com.voc2048.affixForge.data.Keys
import com.voc2048.affixForge.model.EquipmentAffix
import com.voc2048.affixForge.model.ReforgeQuality
import com.voc2048.affixForge.model.ReforgeResult
import com.voc2048.affixForge.renderer.AffixLoreRenderer
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import kotlin.math.pow

object ReforgeManager {
    private const val BASE_LAPIS_COST = 3

    fun reforgeItem(item: ItemStack, lockedIndices: List<Int>): ReforgeResult {
        val meta = item.itemMeta ?: return ReforgeResult.Failure("該物品無法重鑄")
        
        // 1. 讀取現有數據
        val qualityStr = meta.persistentDataContainer.get(Keys.QUALITY, PersistentDataType.STRING)
            ?: return ReforgeResult.Failure("該物品沒有品質數據")
        val quality = try { ReforgeQuality.valueOf(qualityStr) } catch (e: Exception) { return ReforgeResult.Failure("無效的品質數據") }
        
        val currentAffixes = meta.persistentDataContainer.get(Keys.AFFIXES, AffixListDataType)
            ?: return ReforgeResult.Failure("該物品沒有詞條數據")

        // 2. 驗證鎖定索引
        if (lockedIndices.size > 4) {
            return ReforgeResult.Failure("最多隻能鎖定 4 個詞條")
        }
        if (lockedIndices.any { it >= currentAffixes.size }) {
            return ReforgeResult.Failure("無效的鎖定索引")
        }
        if (lockedIndices.contains(4)) {
            return ReforgeResult.Failure("第 5 個詞條無法被鎖定")
        }

        // 3. 計算消耗
        val diamondBlockCost = if (lockedIndices.isEmpty()) 0 else 2.0.pow(lockedIndices.size - 1).toInt()
        
        // 4. 生成新詞條 (保留鎖定的)
        val newAffixesCount = if (quality.minAffixes == quality.maxAffixes) {
            quality.minAffixes
        } else {
            (quality.minAffixes..quality.maxAffixes).random()
        }

        val finalAffixes = mutableListOf<EquipmentAffix?>()
        repeat(newAffixesCount) { finalAffixes.add(null) }

        // 先填入鎖定的詞條
        val usedTemplateIds = mutableSetOf<String>()
        for (index in lockedIndices) {
            if (index < newAffixesCount) {
                val lockedAffix = currentAffixes[index]
                finalAffixes[index] = lockedAffix
                usedTemplateIds.add(lockedAffix.id)
            }
        }

        // 填充其餘位置
        for (i in 0 until newAffixesCount) {
            if (finalAffixes[i] == null) {
                var attempts = 0
                var newAffix: EquipmentAffix? = null
                
                while (attempts < 10) {
                    val template = AffixPool.getRandomTemplate()
                    if (template != null && !usedTemplateIds.contains(template.id)) {
                        val rolledValue = rollValue(template.minValue, template.maxValue)
                        newAffix = EquipmentAffix(template.id, template.name, template.type, rolledValue)
                        usedTemplateIds.add(template.id)
                        break
                    }
                    attempts++
                }
                
                finalAffixes[i] = newAffix ?: currentAffixes.getOrNull(i) // 保底
            }
        }

        val resultList = finalAffixes.filterNotNull()

        // 5. 更新 NBT 與 Lore
        meta.persistentDataContainer.set(Keys.AFFIXES, AffixListDataType, resultList)
        item.itemMeta = meta
        AffixLoreRenderer.render(item, quality, resultList)

        return ReforgeResult.Success(BASE_LAPIS_COST, diamondBlockCost, resultList)
    }

    private fun rollValue(min: Double, max: Double): Double {
        val rawValue = min + (Math.random() * (max - min))
        return (Math.round(rawValue * 100) / 100.0)
    }
}
