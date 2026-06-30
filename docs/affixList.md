# AffixForge — 自定義詞條與效果清單 (Affix List)

所有詞條皆具備等級制度（Level I ~ Max），並分為三大流派。

## ⚔️ A 流派：戰術輸出與機制 (Combat & Offense)
1.  **狂戰士契約 (Berserker) | Max LV: V**
    *   效果：造成的傷害提升 `Level * 8%`，但自身受到的所有傷害也提升 `Level * 4%`。
2.  **破甲 (Armor Piercing) | Max LV: V**
    *   效果：攻擊時無視目標 `Level * 10%` 的護甲值。
3.  **背擊加成 (Backstab) | Max LV: III**
    *   效果：從背後襲擊敵人時，造成的傷害額外提升 `Level * 15%`。
4.  **絕地反擊 (Last Stand) | Max LV: V**
    *   效果：自身血量每降低 10%，造成的傷害便提升 `Level * 2%`。
5.  **精準連擊 (Combo Finisher) | Max LV: I**
    *   效果：連續 5 次近戰擊中目標未中斷，第 6 次攻擊造成 200% 傷害。

## 🛡️ B 流派：生存防禦與反制 (Defense & Survival)
1.  **相位閃避 (Phase Shift) | Max LV: III**
    *   效果：受到傷害時，有 `Level * 5%` 的機率進入 0.5 秒的完全無敵狀態（冷卻時間 15 秒）。
2.  **反震甲殼 (Reflective Shell) | Max LV: IV**
    *   效果：受到遠程投射物傷害時，有 `Level * 15%` 的機率將彈頭原路反彈給攻擊者。
3.  **飲血 (Life Steal) | Max LV: III**
    *   效果：造成近戰傷害時，將傷害量的 `Level * 3%` 轉化為自身生命值。
4.  **瀕死護盾 (Lifeline Shield) | Max LV: III**
    *   效果：當血量低於 20% 時，立即獲得一個等同於最大生命值 `Level * 15%` 的黃心傷害吸收盾（冷卻 60 秒）。

## 🎒 C 流派：功能實用與探索 (Utility & Exploration)
*極其適合綁定在自定義飾物（Accessory）上。*
1.  **地質雷達 (Geological Radar) | Max LV: III**
    *   效果：切換為蹲下狀態時，會以粒子效果顯示周圍 `Level * 4` 格內稀有礦石（鑽石、獄髓等）的輪廓。
2.  **經驗引力 (XP Vacuum) | Max LV: V**
    *   效果：自動吸附周圍經驗球與掉落物的半徑增加 `Level * 2` 格。
3.  **液體排斥 (Hydro-Phobic) | Max LV: II**
    *   效果：在水中的移動速度增加 `Level * 25%`，且 LV: II 時完全免疫水中的挖掘減速懲罰。
4.  **幸運眷顧 (Looting Mastery) | Max LV: III**
    *   效果：擊殺生物時的「掠奪」效果額外提升 `Level` 級（可突破原版附魔上限）。

## 💀 D 流派：惡魔詛咒 (Curses)
*洗練時有極低機率出現，佔用槽位且帶來負面效果，需重新洗練消除。*
1.  **易損壞 (Fragile) | Max LV: I**
    *   效果：每次消耗道具耐久度時，額外多扣除 3 點耐久。
2.  **珍珠銷毀 (Pearl Void) | Max LV: I**
    *   效果：擊殺終界使者時，絕對不會掉落終界珍珠（即使有掠奪附魔也無效）。
    
## 💀 E 流派：裝飾
*洗練時有低機率出現，佔用槽位且帶來裝飾效果。*
1.  **七彩虹 (Rainbow) | Max LV: I**
    *   效果：武器或飾物在世界中會呈現彩虹色的粒子效果，僅為裝飾用途，無實際戰鬥效果。
2.  * *光影流動 (Shimmer) | Max LV: I**
    *   效果：武器或飾物在世界中會呈現光影流動的粒子效果，僅為裝飾用途，無實際戰鬥效果。
3.  **你睇我唔到 (Size Reduce) | Max LV: V**
    *   效果：實際讓用戶能夠變小，縮小比例為 `Level * 20%`。
4.  **高人一等 (Size Enlarge) | Max LV: V**
    *   效果：實際讓用戶能夠變小，放大比例為 `Level * 20%`。