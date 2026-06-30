package com.voc2048.affixForge.listener

import com.voc2048.affixForge.logic.StatManager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.plugin.java.JavaPlugin

class PlayerStatListener(private val plugin: JavaPlugin) : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onInventoryClick(event: InventoryClickEvent) {
        updateStats(event.whoClicked as? org.bukkit.entity.Player)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onInventoryDrag(event: InventoryDragEvent) {
        updateStats(event.whoClicked as? org.bukkit.entity.Player)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.hasItem()) {
            updateStats(event.player)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onSwapHand(event: PlayerSwapHandItemsEvent) {
        updateStats(event.player)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onDrop(event: PlayerDropItemEvent) {
        updateStats(event.player)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onJoin(event: PlayerJoinEvent) {
        updateStats(event.player)
    }

    private fun updateStats(player: org.bukkit.entity.Player?) {
        if (player == null) return
        // 延遲一幀以確保物品已經更新到物品欄
        plugin.server.scheduler.runTask(plugin, Runnable {
            StatManager.updatePlayerStats(player)
        })
    }
}
