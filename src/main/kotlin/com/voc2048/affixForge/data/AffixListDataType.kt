package com.voc2048.affixForge.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.voc2048.affixForge.model.EquipmentAffix
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import java.nio.charset.StandardCharsets

object AffixListDataType : PersistentDataType<ByteArray, List<EquipmentAffix>> {
    private val gson = Gson()
    private val type = object : TypeToken<List<EquipmentAffix>>() {}.type

    override fun getPrimitiveType(): Class<ByteArray> = ByteArray::class.java

    override fun getComplexType(): Class<List<EquipmentAffix>> = List::class.java as Class<List<EquipmentAffix>>

    override fun toPrimitive(complex: List<EquipmentAffix>, context: PersistentDataAdapterContext): ByteArray {
        return gson.toJson(complex, type).toByteArray(StandardCharsets.UTF_8)
    }

    override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): List<EquipmentAffix> {
        return gson.fromJson(String(primitive, StandardCharsets.UTF_8), type)
    }
}
