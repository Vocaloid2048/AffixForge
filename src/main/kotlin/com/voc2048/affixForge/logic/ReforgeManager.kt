package com.voc2048.affixForge.logic

import com.voc2048.affixForge.data.AffixListDataType
import com.voc2048.affixForge.data.AffixRegistry
import com.voc2048.affixForge.data.Keys
import com.voc2048.affixForge.model.EquipmentAffix
import com.voc2048.affixForge.model.ReforgeQuality
import com.voc2048.affixForge.model.ReforgeResult
import com.voc2048.affixForge.renderer.AffixLoreRenderer
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object ReforgeManager {
    private const val BASE_LAPIS_COST = 3

    fun reforgeItem(item: ItemStack, lockedIndices: List<Int>): ReforgeResult {
        val meta = item.itemMeta ?: return ReforgeResult.Failure("該物品無法重鑄")
        
        // 1. 讀取現有數據
        val qualityStr = meta.persistentDataContainer.get(Keys.QUALITY, PersistentDataType.STRING)
            ?: return ReforgeResult.Failure("該物品沒有品質數據")
        val quality = try { ReforgeQuality.valueOf(qualityStr) } catch (e: Exception) { return ReforgeResult.Failure("無效的品質數據") }
        
        val currentAffixes = meta.persistentDataContainer.get(Keys.AFFIXES, AffixListDataType) ?: emptyList()

        // 檢查詞條池是否為空
        if (AffixRegistry.isEmpty()) {
            return ReforgeResult.Failure("詞條池為空，請檢查伺服器日誌或 affixes.yml 配置")
        }

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

        // 3. 計算消耗 (n^2)
        val n = lockedIndices.size
        val diamondBlockCost = n * n
        
        // 4. 生成新詞條 (保留鎖定的)
        val newAffixesCount = quality.minAffixes

        val finalAffixes = mutableListOf<EquipmentAffix?>()
        repeat(newAffixesCount) { finalAffixes.add(null) }

        val usedTemplateIds = mutableSetOf<String>()
        
        // 確保鎖定的詞條 ID 被記錄，避免隨機生成重複
        for (index in lockedIndices) {
            if (index < newAffixesCount) {
                val lockedAffix = currentAffixes.getOrNull(index)
                if (lockedAffix != null) {
                    finalAffixes[index] = lockedAffix
                    usedTemplateIds.add(lockedAffix.id)
                }
            }
        }

        // 填充其餘位置
        for (i in 0 until newAffixesCount) {
            if (finalAffixes[i] == null) {
                val template = AffixRegistry.getRandomTemplate(usedTemplateIds)
                if (template != null) {
                    finalAffixes[i] = EquipmentAffix(
                        id = template.id,
                        name = template.displayName,
                        type = template.type,
                        value = template.baseValue,
                        level = 1
                    )
                    usedTemplateIds.add(template.id)
                }
            }
        }

        val resultList = finalAffixes.filterNotNull()

        // 5. 更新數據並渲染 (統一在一個 editMeta 塊中完成，避免 desync)
        item.editMeta { m ->
            m.persistentDataContainer.set(Keys.AFFIXES, AffixListDataType, resultList)
            // 直接調用 renderWithMeta
            AffixLoreRenderer.renderWithMeta(item, m, quality, resultList)
        }

        return ReforgeResult.Success(BASE_LAPIS_COST, diamondBlockCost, resultList)
    }

    private fun rollValue(min: Double, max: Double): Double {
        val rawValue = min + (Math.random() * (max - min))
        return (Math.round(rawValue * 100) / 100.0)
    }
}
