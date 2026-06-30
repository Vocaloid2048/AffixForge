package com.voc2048.affixForge.command

import com.voc2048.affixForge.data.AffixListDataType
import com.voc2048.affixForge.data.Keys
import com.voc2048.affixForge.data.AffixRegistry
import com.voc2048.affixForge.gui.ReforgeGUI
import com.voc2048.affixForge.logic.ReforgeManager
import com.voc2048.affixForge.model.ReforgeQuality
import com.voc2048.affixForge.model.ReforgeResult
import com.voc2048.affixForge.renderer.AffixLoreRenderer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class AffixCommand(private val plugin: JavaPlugin) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("只有玩家可以使用此指令")
            return true
        }

        if (args.isEmpty() || args[0] == "gui") {
            ReforgeGUI(plugin, sender)
            return true
        }

        when (args[0]) {
            "give" -> {
                if (args.size < 2) return false
                val qualityStr = args[1].uppercase()
                val quality = try {
                    ReforgeQuality.valueOf(qualityStr)
                } catch (e: Exception) {
                    sender.sendMessage(Component.text("無效的品級: $qualityStr").color(NamedTextColor.RED))
                    return true
                }

                val item = sender.inventory.itemInMainHand
                if (item.type.isAir) {
                    sender.sendMessage(Component.text("請手持物品").color(NamedTextColor.RED))
                    return true
                }

                val affixes = AffixRegistry.rollRandomAffixes(quality)
                val meta = item.itemMeta

                meta.persistentDataContainer.set(Keys.QUALITY, org.bukkit.persistence.PersistentDataType.STRING, quality.name)
                meta.persistentDataContainer.set(Keys.AFFIXES, AffixListDataType, affixes)
                
                item.itemMeta = meta
                
                AffixLoreRenderer.render(item, quality, affixes)
                
                sender.sendMessage(
                    Component.text("已成功為物品添加 ")
                        .append(quality.getFormattedName())
                        .append(Component.text(" 詞條！"))
                        .color(NamedTextColor.GREEN)
                )
            }
            "reforge" -> {
                val item = sender.inventory.itemInMainHand
                if (item.type.isAir) {
                    sender.sendMessage(Component.text("請手持物品").color(NamedTextColor.RED))
                    return true
                }

                val lockedIndices = if (args.size > 1) {
                    args[1].split(",").mapNotNull { it.toIntOrNull()?.minus(1) }
                } else {
                    emptyList()
                }

                val result = ReforgeManager.reforgeItem(item, lockedIndices)
                when (result) {
                    is ReforgeResult.Success -> {
                        sender.sendMessage(
                            Component.text("重鑄成功！消耗了 ${result.lapisCost} 個青金石和 ${result.diamondBlockCost} 個鑽石磚。")
                                .color(NamedTextColor.GREEN)
                        )
                    }
                    is ReforgeResult.Failure -> {
                        sender.sendMessage(Component.text("重鑄失敗: ${result.message}").color(NamedTextColor.RED))
                    }
                }
            }
            "setaccessory" -> {
                val item = sender.inventory.itemInMainHand
                if (item.type.isAir) {
                    sender.sendMessage(Component.text("請手持物品").color(NamedTextColor.RED))
                    return true
                }
                val meta = item.itemMeta
                meta.persistentDataContainer.set(Keys.CUSTOM_ACCESSORY, org.bukkit.persistence.PersistentDataType.BYTE, 1.toByte())
                item.itemMeta = meta
                sender.sendMessage(Component.text("已將該物品設置為自定義飾品").color(NamedTextColor.GREEN))
            }
            "setid" -> {
                if (args.size < 2) return false
                val item = sender.inventory.itemInMainHand
                if (item.type.isAir) {
                    sender.sendMessage(Component.text("請手持物品").color(NamedTextColor.RED))
                    return true
                }
                val meta = item.itemMeta
                meta.persistentDataContainer.set(Keys.SET_ID, org.bukkit.persistence.PersistentDataType.STRING, args[1])
                item.itemMeta = meta
                sender.sendMessage(Component.text("已將該物品的套裝 ID 設置為: ${args[1]}").color(NamedTextColor.GREEN))
            }
        }

        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        if (args.size == 1) return listOf("give", "reforge", "setaccessory", "setid", "gui")
        if (args.size == 2) {
            if (args[0] == "give") {
                return ReforgeQuality.entries.map { it.name.lowercase() }.filter { it.startsWith(args[1].lowercase()) }
            }
            if (args[0] == "reforge") {
                return listOf("1", "1,2", "1,2,3", "1,2,3,4")
            }
            if (args[0] == "setid") {
                return listOf("warrior", "mage", "archer")
            }
        }
        return emptyList()
    }
}
