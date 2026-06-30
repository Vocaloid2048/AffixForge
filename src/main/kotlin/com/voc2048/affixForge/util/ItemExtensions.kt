package com.voc2048.affixForge.util

import com.voc2048.affixForge.data.AffixListDataType
import com.voc2048.affixForge.data.Keys
import com.voc2048.affixForge.model.EquipmentAffix
import com.voc2048.affixForge.model.ReforgeQuality
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

fun ItemStack.getAffixes(): List<EquipmentAffix> {
    return itemMeta?.persistentDataContainer?.get(Keys.AFFIXES, AffixListDataType) ?: emptyList()
}

fun ItemStack.setAffixes(affixes: List<EquipmentAffix>) {
    editMeta { meta ->
        meta.persistentDataContainer.set(Keys.AFFIXES, AffixListDataType, affixes)
    }
}

var ItemStack.maxSlots: Int
    get() = itemMeta?.persistentDataContainer?.get(Keys.MAX_SLOTS, PersistentDataType.INTEGER) ?: 0
    set(value) {
        editMeta { meta ->
            meta.persistentDataContainer.set(Keys.MAX_SLOTS, PersistentDataType.INTEGER, value)
        }
    }

var ItemStack.quality: ReforgeQuality?
    get() {
        val name = itemMeta?.persistentDataContainer?.get(Keys.QUALITY, PersistentDataType.STRING) ?: return null
        return try { ReforgeQuality.valueOf(name) } catch (e: Exception) { null }
    }
    set(value) {
        editMeta { meta ->
            if (value == null) meta.persistentDataContainer.remove(Keys.QUALITY)
            else meta.persistentDataContainer.set(Keys.QUALITY, PersistentDataType.STRING, value.name)
        }
    }

fun ItemStack.isAuthenticated(): Boolean = quality != null

fun ItemStack.updateLore() {
    val affixes = this.getAffixes()
    val quality = this.quality ?: return
    com.voc2048.affixForge.renderer.AffixLoreRenderer.render(this, quality, affixes)
}
