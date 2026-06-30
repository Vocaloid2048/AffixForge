package com.voc2048.affixForge.listener

import com.voc2048.affixForge.util.getAffixes
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class AffixGameplayListener(private val plugin: JavaPlugin) : Listener {

    private val comboCache = ConcurrentHashMap<UUID, ComboData>()
    private val phaseShiftInvuln = ConcurrentHashMap<UUID, Long>()
    private val phaseShiftCooldown = ConcurrentHashMap<UUID, Long>()
    private val lifelineCooldown = ConcurrentHashMap<UUID, Long>()
    private val sneakingPlayers = mutableSetOf<UUID>()

    data class ComboData(var count: Int, var lastHit: Long)

    init {
        // 定時任務：處理每 2 秒一次的雷達掃描與每 tick 的吸引/粒子效果
        Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            processTimedEffects()
        }, 20L, 20L) // 每秒執行一次主要的邏輯檢查

        Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            processFastTimedEffects()
        }, 1L, 5L) // 每 5 tick 執行吸引與粒子
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onCombat(event: EntityDamageByEntityEvent) {
        val attacker = event.damager as? Player
        val victim = event.entity as? LivingEntity ?: return

        // --- 攻擊者邏輯 ---
        if (attacker != null) {
            val weapon = attacker.inventory.itemInMainHand
            val affixes = weapon.getAffixes()
            var multiplier = 1.0

            for (affix in affixes) {
                when (affix.id) {
                    "berserker" -> multiplier += affix.level * 0.08
                    "armor_piercing" -> {
                        val armor = victim.getAttribute(Attribute.ARMOR)?.value ?: 0.0
                        if (armor > 0) {
                            val effectiveArmor = armor.coerceAtMost(20.0)
                            multiplier += (affix.level * 0.10 * effectiveArmor) / (25.0 - effectiveArmor)
                        }
                    }
                    "backstab" -> {
                        if (isBehind(attacker, victim)) {
                            multiplier += affix.level * 0.15
                            attacker.playSound(attacker.location, Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.5f, 1.5f)
                        }
                    }
                    "last_stand" -> {
                        val maxHp = attacker.getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0
                        val missing = (1.0 - (attacker.health / maxHp)).coerceIn(0.0, 1.0)
                        multiplier += (missing / 0.1).toInt() * (affix.level * 0.02)
                    }
                    "combo_finisher" -> {
                        val data = comboCache.getOrPut(attacker.uniqueId) { ComboData(0, System.currentTimeMillis()) }
                        if (System.currentTimeMillis() - data.lastHit > 5000L) data.count = 0
                        data.count++
                        data.lastHit = System.currentTimeMillis()
                        if (data.count >= 6) {
                            multiplier *= 2.0
                            data.count = 0
                            victim.world.spawnParticle(Particle.DAMAGE_INDICATOR, victim.location.add(0.0, 1.0, 0.0), 10)
                        }
                    }
                    "life_steal" -> {
                        if (event.cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                            val heal = event.finalDamage * (affix.level * 0.03)
                            attacker.health = (attacker.health + heal).coerceAtMost(attacker.getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0)
                        }
                    }
                }
            }
            event.damage *= multiplier
        }

        // --- 受害者邏輯 (反震甲殼) ---
        if (victim is Player) {
            val shellLevel = getHighestAffixLevel(victim, "reflective_shell")
            if (shellLevel > 0 && event.damager is Projectile) {
                if (Random().nextDouble() < shellLevel * 0.15) {
                    event.isCancelled = true
                    val shooter = (event.damager as Projectile).shooter as? LivingEntity
                    shooter?.damage(event.damage, victim)
                    victim.playSound(victim.location, Sound.ITEM_SHIELD_BLOCK, 0.5f, 2.0f)
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onTakeDamage(event: EntityDamageEvent) {
        val player = event.entity as? Player ?: return

        // 相位閃避 (無敵中)
        if (phaseShiftInvuln.containsKey(player.uniqueId) && phaseShiftInvuln[player.uniqueId]!! > System.currentTimeMillis()) {
            event.isCancelled = true
            return
        }

        // 相位閃避 (觸發機率)
        val phaseLevel = getHighestAffixLevel(player, "phase_shift")
        if (phaseLevel > 0 && !event.isCancelled) {
            val now = System.currentTimeMillis()
            val lastUsed = phaseShiftCooldown.getOrDefault(player.uniqueId, 0L)
            if (now - lastUsed > 15000L && Random().nextDouble() < phaseLevel * 0.05) {
                event.isCancelled = true
                phaseShiftInvuln[player.uniqueId] = now + 500L
                phaseShiftCooldown[player.uniqueId] = now
                player.playSound(player.location, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 0.5f, 1.5f)
                player.world.spawnParticle(Particle.WITCH, player.location.add(0.0, 1.0, 0.0), 20, 0.3, 0.5, 0.3)
                return
            }
        }

        // 瀕死護盾
        val shieldLevel = getHighestAffixLevel(player, "lifeline_shield")
        if (shieldLevel > 0) {
            val maxHp = player.getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0
            if ((player.health - event.finalDamage) / maxHp < 0.2) {
                val now = System.currentTimeMillis()
                val lastUsed = lifelineCooldown.getOrDefault(player.uniqueId, 0L)
                if (now - lastUsed > 60000L) {
                    lifelineCooldown[player.uniqueId] = now
                    player.addPotionEffect(PotionEffect(PotionEffectType.ABSORPTION, shieldLevel * 3 * 20, shieldLevel - 1))
                    player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GOLD, 1.0f, 1.0f)
                }
            }
        }
    }

    @EventHandler
    fun onDeath(event: EntityDeathEvent) {
        val killer = event.entity.killer ?: return
        val weapon = killer.inventory.itemInMainHand
        val affixes = weapon.getAffixes()

        // 幸運眷顧
        val lootingLevel = affixes.find { it.id == "looting_mastery" }?.level ?: 0
        if (lootingLevel > 0) {
            // 注意：此處僅模擬加成，真正精確的 Looting 需攔截 LootTable，此處採簡單增加掉落量方式
            event.drops.forEach { it.amount = (it.amount * (1.0 + lootingLevel * 0.5)).toInt().coerceAtLeast(it.amount) }
        }

        // 珍珠銷毀
        if (event.entityType == EntityType.ENDERMAN) {
            val hasPearlVoid = affixes.any { it.id == "pearl_void" }
            if (hasPearlVoid) {
                event.drops.removeIf { it.type == Material.ENDER_PEARL }
            }
        }
    }

    @EventHandler
    fun onItemDamage(event: PlayerItemDamageEvent) {
        val affixes = event.item.getAffixes()
        if (affixes.any { it.id == "fragile" }) {
            event.damage += 3
        }
    }

    @EventHandler
    fun onSneak(event: PlayerToggleSneakEvent) {
        if (event.isSneaking) sneakingPlayers.add(event.player.uniqueId)
        else sneakingPlayers.remove(event.player.uniqueId)
    }

    @EventHandler
    fun onBreakBlock(event: BlockBreakEvent) {
        // 液體排斥 Lv II 挖礦速度在 processTimedEffects 賦予 Haste
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        comboCache.remove(event.player.uniqueId)
        phaseShiftInvuln.remove(event.player.uniqueId)
        sneakingPlayers.remove(event.player.uniqueId)
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (event.action == Action.LEFT_CLICK_AIR || event.action == Action.LEFT_CLICK_BLOCK) {
            comboCache.remove(event.player.uniqueId)
        }
    }

    private fun processTimedEffects() {
        for (player in Bukkit.getOnlinePlayers()) {
            // 液體排斥
            val hydroLevel = getHighestAffixLevel(player, "hydro_phobic")
            if (hydroLevel > 0 && player.location.block.type == Material.WATER) {
                player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 25, (hydroLevel * 1) - 1, false, false))
                if (hydroLevel >= 2) {
                    player.addPotionEffect(PotionEffect(PotionEffectType.HASTE, 25, 1, false, false))
                }
            }

            // 地質雷達 (2秒一次)
            if (sneakingPlayers.contains(player.uniqueId)) {
                val radarLevel = getHighestAffixLevel(player, "geological_radar")
                if (radarLevel > 0 && Bukkit.getCurrentTick() % 40 == 0) {
                    scanOres(player, radarLevel * 4)
                }
            }
        }
    }

    private fun processFastTimedEffects() {
        for (player in Bukkit.getOnlinePlayers()) {
            val vacuumLevel = getHighestAffixLevel(player, "xp_vacuum")
            if (vacuumLevel > 0) {
                val radius = (vacuumLevel * 2).toDouble()
                player.getNearbyEntities(radius, radius, radius).forEach { entity ->
                    if (entity is ExperienceOrb || entity is Item) {
                        val vec = player.location.toVector().subtract(entity.location.toVector()).normalize().multiply(0.2)
                        entity.velocity = entity.velocity.add(vec)
                    }
                }
            }

            // 裝飾效果
            val allEquip = player.inventory.armorContents.toMutableList().apply { 
                add(player.inventory.itemInMainHand)
                add(player.inventory.itemInOffHand)
            }
            var hasRainbow = false
            var hasShimmer = false
            for (item in allEquip.filterNotNull()) {
                val affs = item.getAffixes()
                if (affs.any { it.id == "rainbow" }) hasRainbow = true
                if (affs.any { it.id == "shimmer" }) hasShimmer = true
            }

            if (hasRainbow) {
                val loc = player.location.add(0.0, 1.0, 0.0)
                player.world.spawnParticle(Particle.DUST, loc, 3, 0.5, 0.5, 0.5, 
                    Particle.DustOptions(org.bukkit.Color.fromRGB(Random().nextInt(256), Random().nextInt(256), Random().nextInt(256)), 1f))
            }
            if (hasShimmer) {
                player.world.spawnParticle(Particle.END_ROD, player.location.add(0.0, 1.0, 0.0), 1, 0.4, 0.6, 0.4, 0.01)
            }
        }
    }

    private fun scanOres(player: Player, radius: Int) {
        val loc = player.location
        val ores = setOf(Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE, Material.ANCIENT_DEBRIS, Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE)
        for (x in -radius..radius) {
            for (y in -radius..radius) {
                for (z in -radius..radius) {
                    val block = loc.clone().add(x.toDouble(), y.toDouble(), z.toDouble()).block
                    if (ores.contains(block.type)) {
                        player.spawnParticle(Particle.GLOW, block.location.add(0.5, 0.5, 0.5), 5, 0.1, 0.1, 0.1, 0.05)
                    }
                }
            }
        }
    }

    private fun getHighestAffixLevel(player: Player, id: String): Int {
        var maxLevel = 0
        val items = mutableListOf<ItemStack?>()
        items.addAll(player.inventory.armorContents)
        items.add(player.inventory.itemInMainHand)
        items.add(player.inventory.itemInOffHand)
        
        for (item in items.filterNotNull()) {
            val level = item.getAffixes().find { it.id == id }?.level ?: 0
            if (level > maxLevel) maxLevel = level
        }
        return maxLevel
    }

    private fun isBehind(attacker: Player, victim: LivingEntity): Boolean {
        val victimDir = victim.location.direction.setY(0).normalize()
        val toVictim = victim.location.toVector().subtract(attacker.location.toVector()).setY(0).normalize()
        return victimDir.dot(toVictim) > 0.707
    }
}
