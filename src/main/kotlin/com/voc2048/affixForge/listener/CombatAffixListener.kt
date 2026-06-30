package com.voc2048.affixForge.listener

import com.voc2048.affixForge.util.getAffixes
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 實作 A 流派（戰術輸出與機制）的實際戰鬥效果監聽器
 */
class CombatAffixListener(private val plugin: JavaPlugin) : Listener {

    // 用於精準連擊 (Combo Finisher) 的快取
    private val comboCache = ConcurrentHashMap<UUID, ComboData>()
    private val COMBO_TIMEOUT_MS = 5000L

    data class ComboData(var count: Int, var lastHit: Long)

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onCombatDealt(event: EntityDamageByEntityEvent) {
        val attacker = event.damager as? Player ?: return
        val victim = event.entity as? LivingEntity ?: return

        val weapon = attacker.inventory.itemInMainHand
        val affixes = weapon.getAffixes()
        if (affixes.isEmpty()) return

        var multiplier = 1.0

        for (affix in affixes) {
            when (affix.id) {
                "berserker" -> {
                    // 狂戰士契約：傷害提升 Level * 8%
                    multiplier += affix.level * 0.08
                }
                
                "armor_piercing" -> {
                    // 破甲：忽略 Level * 10% 護甲值
                    // 以等比例傷害加成模擬：bonus = (ignore_pct * armor) / (25 - armor)
                    val armor = victim.getAttribute(Attribute.ARMOR)?.value ?: 0.0
                    if (armor > 0) {
                        val effectiveArmor = armor.coerceAtMost(20.0) // 原版減傷上限通常對應 20 點護甲
                        val ignorePct = affix.level * 0.10
                        val bonusMult = (ignorePct * effectiveArmor) / (25.0 - effectiveArmor)
                        multiplier += bonusMult
                    }
                }

                "backstab" -> {
                    // 背擊加成：背後 90 度範圍內傷害提升 Level * 15%
                    if (isBehind(attacker, victim)) {
                        multiplier += affix.level * 0.15
                        attacker.playSound(attacker.location, Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.6f, 1.5f)
                    }
                }

                "last_stand" -> {
                    // 絕地反擊：每降低 10% 血量，傷害提升 Level * 2%
                    val maxHp = attacker.getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0
                    val hpPercent = attacker.health / maxHp
                    val missingPercent = (1.0 - hpPercent).coerceIn(0.0, 1.0)
                    val stacks = (missingPercent / 0.1).toInt()
                    multiplier += stacks * (affix.level * 0.02)
                }

                "combo_finisher" -> {
                    // 精準連擊：第 6 次傷害 200%
                    val data = comboCache.getOrPut(attacker.uniqueId) { ComboData(0, System.currentTimeMillis()) }
                    val now = System.currentTimeMillis()
                    
                    if (now - data.lastHit > COMBO_TIMEOUT_MS) {
                        data.count = 0
                    }
                    
                    data.count++
                    data.lastHit = now

                    if (data.count >= 6) {
                        multiplier *= 2.0
                        data.count = 0
                        // 紅色粒子特效
                        victim.world.spawnParticle(
                            Particle.DAMAGE_INDICATOR,
                            victim.location.add(0.0, 1.0, 0.0),
                            15, 0.2, 0.5, 0.2, 0.1
                        )
                    }
                }
            }
        }
        
        event.damage *= multiplier
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onCombatReceived(event: EntityDamageByEntityEvent) {
        val victim = event.entity as? Player ?: return
        
        // 狂戰士契約：受到的傷害也提升 Level * 4%
        // 檢查手持或裝備的所有物品中的最大等級
        var maxBerserkerLevel = 0
        
        val equipment = mutableListOf<org.bukkit.inventory.ItemStack>()
        equipment.add(victim.inventory.itemInMainHand)
        equipment.add(victim.inventory.itemInOffHand)
        equipment.addAll(victim.inventory.armorContents.filterNotNull())
        
        for (item in equipment) {
            val level = item.getAffixes().find { it.id == "berserker" }?.level ?: 0
            if (level > maxBerserkerLevel) maxBerserkerLevel = level
        }
        
        if (maxBerserkerLevel > 0) {
            event.damage *= (1.0 + maxBerserkerLevel * 0.04)
        }
    }

    @EventHandler
    fun onPlayerMiss(event: PlayerInteractEvent) {
        // 揮空或點擊方塊時清空連擊計數
        if (event.action == Action.LEFT_CLICK_AIR || event.action == Action.LEFT_CLICK_BLOCK) {
            comboCache.remove(event.player.uniqueId)
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        comboCache.remove(event.player.uniqueId)
    }

    private fun isBehind(attacker: Player, victim: LivingEntity): Boolean {
        // 判斷攻擊者是否處於受害者背後的 90 度錐形區域內
        val victimDir = victim.location.direction.setY(0).normalize()
        val toVictim = victim.location.toVector().subtract(attacker.location.toVector()).setY(0).normalize()
        
        // 內積 > cos(45度) = 0.707 代表夾角小於 45 度 (兩側共 90 度)
        return victimDir.dot(toVictim) > 0.707
    }
}
