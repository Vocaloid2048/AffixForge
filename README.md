<div align="center">

# 🌌 AffixForge

### *Breathe RPG Soul into Your Minecraft Equipment & Accessories*

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.20%20%2F%201.21-emerald?style=for-the-badge&logo=minecraft)](https://papermc.io)
[![Language](https://img.shields.io/badge/Language-Kotlin-purple?style=for-the-badge&logo=kotlin)](https://kotlinlang.org)
[![Platform](https://img.shields.io/badge/Platform-PaperMC%20%2F%20Purpur-orange?style=for-the-badge)](https://papermc.io)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE)

---

[繁體中文](README.md) | [English](README_EN.md)

</div>

**AffixForge** 是一款專為 Minecraft 高性能伺服器（Paper/Purpur）打造的次世代 RPG 裝備詞條與鍛造洗練系統。擺脫傳統低效且易被篡改的 Lore 文本偵測，本插件完全基於 Minecraft 原生的 **PersistentDataContainer (PDC)** 技術，提供極致流暢、安全且自由度極高的自定義屬性體驗。

---

## ✨ 核心特性 | Features

* 💎 **五階品級體系 (Rarity Tiers)**
    * 裝備劃分為 **普通、罕見、稀有、史詩、傳說**，隨機賦予 1 至 5 條不等的高級詞條。
* ⚔️ **四大詞條維度 (Affix Categories)**
    * **基礎屬性：** 血量上限、攻擊力、移動速度等。
    * **元素特效：** 攻擊附帶凋零/燃燒，受擊觸發機率護盾。
    * **特殊機制：** 暴擊率/暴擊傷害、吸血、背擊加成。
    * **套裝共鳴：** 同時裝備多件同系列裝備/飾物，解鎖強力套裝效果（Set Bonus）。
* 🎲 **動態洗練與戰略鎖定 (Reforging & Locking)**
    * 消耗**青金石**隨機重置詞條；引入經濟槓桿，可消耗 **1/2/4/8 個鑽石磚** 鎖定 1~4 條極品屬性，進行定向洗練！
* 💍 **全面支援自定義飾物 (Accessory Support)**
    * 不僅支援 Vanilla 武器盔甲，更內置獨立的飾物檢測與 API 接口。只要帶有特定 NBT 標記，即可將其轉化為護符、項鍊等動態屬性載體。
* ⚡ **極致性能 (Performance First)**
    * 全面採用 **Kotlin** 慣用法開發，輕量化非同步事件監聽，拒絕卡頓。

---

## 🛠️ 開發計畫與架構 | Roadmap

本專案採用模組化迭代開發，底層架構如下：

- [x] **Phase 1:** 核心數據模型、Kotlin 專屬 NBT 序列化器與動態 Lore 渲染引擎。
- [ ] **Phase 2:** 可自定義配置的 `AffixPool.yml` 詞條池與權重隨機生成算法。
- [ ] **Phase 3:** 鍛造台洗練演算法與鑽石磚鎖定代價扣除機制。
- [ ] **Phase 4:** 裝備/自定義飾物全域屬性監聽器與套裝檢查器。
- [ ] **Phase 5:** 箱子介面（Inventory UI）互動式鍛造監控 GUI。

---

## ⌨️ 管理員指令 | Commands

| 指令 (Command) | 權限 (Permission) | 說明 (Description) |
| :--- | :--- | :--- |
| `/affixforge reload` | `affixforge.admin` | 重載插件設定檔 |
| `/affixforge give <品質>` | `affixforge.admin` | 為手上的物品隨機生成指定品級的詞條 |
| `/affixforge reforge` | `affixforge.use` | 開啟自定義鍛造洗練 GUI 介面 |

---

## 🧱 開發者 API | Developer API

如果你想讓你的自定義物品插件（如 MythicMobs、Oraxen）生成的物品直接變成「可鍛造飾物」，只需在代碼中為該 `ItemStack` 寫入以下 `PersistentDataContainer` 標籤：

```kotlin
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataType

val key = NamespacedKey(plugin, "custom_accessory")
itemStack.itemMeta?.let { meta ->
    meta.persistentDataContainer.set(key, PersistentDataType.BOOLEAN, true)
    itemStack.itemMeta = meta
}