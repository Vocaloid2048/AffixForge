package com.voc2048.affixForge.model

sealed class ReforgeResult {
    data class Success(
        val lapisCost: Int,
        val diamondBlockCost: Int,
        val newAffixes: List<EquipmentAffix>
    ) : ReforgeResult()

    data class Failure(val message: String) : ReforgeResult()
}
