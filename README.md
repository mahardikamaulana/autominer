# Autominer Fix — Necesse Mod

[![Game Version](https://img.shields.io/badge/Necesse-v1.3.2-blue.svg)](https://necesse.com/)
[![Mod Version](https://img.shields.io/badge/version-1.0.0-green.svg)]()
[![License](https://img.shields.io/badge/license-MIT-orange.svg)]()

Automatic mineral and resource mining mod for **Necesse** (v1.3.2+).

This mod provides automatic miners for **55+** ores, gems, stones, dungeon drops, and monster materials organized cleanly across progression tiers, complete with in-game configuration via **CustomSettingsLib** and fallback chat commands.

> **Credits:** Original mod created by **KKDJ20** and **Agath**. Maintained and updated by **Xeraphire**.

---

## 🌟 Features

- ⛏️ **55+ Auto Miners**: Mine everything from basic stone & copper to mid-game demonic drops, deep cave ores, and endgame upgrade materials.
- ⚙️ **In-Game Settings UI**: Full integration with [CustomSettingsLib](https://steamcommunity.com/sharedfiles/filedetails/?id=3440421710) for real-time in-game configuration.
- 💬 **Fallback Chat Command**: If CustomSettingsLib is not installed, all settings can be adjusted on-the-fly using `/autominer`.
- 🪵 **Fuel System**: Optional wood fuel requirement (configurable on/off, customizable fuel consumption duration).
- ⚡ **Speed Multipliers**: Configurable mining speed (1x to 5x+).
- 🏆 **Tier-Based Progression**: Miners unlock progressively at matching crafting stations (Workstation → Demonic Workstation → Tungsten Workstation → Fallen Workstation).
- 🌐 **Multi-language Support**: English (`en`), Korean (`kr`), German (`de`), and Spanish (`es`).

---

## 📦 Miner Progression & Tiers

### Tier 1: Workstation (Early Game / Surface)
* **Earth & Blocks**: Stone, Clay, Sand, Sandstone, Snowstone, Swampstone
* **Surface Ores**: Copper Ore, Iron Ore, Gold Ore, Frost Shards
* **Gems & Crystals**: Amethyst, Topaz, Emerald, Sapphire, Ruby, Diamond, Amber
* **Mob Drops**: Bat Wing, Bone, Silk, Wool, Cloth Scraps

### Tier 2: Demonic Workstation (Mid Game / Demonic)
* **Demonic Minerals**: Ivy Ore, Quartz, Spiderite Ore, Life Quartz
* **Essences & Dungeon Drops**: Void Shard, Ectoplasm, Phantom Dust, Blood Essence, Slime Essence, Slime Matter, Slimeum, Spider Essence, Spider Venom, Cave Spider Gland, Spider Stone, Worm Carapace

### Tier 3: Tungsten Workstation (Deep Caves)
* **Deep Earth & Stones**: Deep Stone, Deep Sandstone, Deep Snowstone, Deep Swampstone, Granite, Basalt, Runestone
* **Deep & Glacial Ores**: Tungsten Ore, Obsidian, Glacial Ore, Glacial Shard, Ancient Fossil Ore, Mycelium Ore, Nightsteel Ore

### Tier 4: Fallen Workstation (Endgame)
* **Endgame Shards & Crystals**: Upgrade Shards, Alchemy Shards, Altar Dust, Omnicrystal

---

## ⚙️ Configuration

### Option A: CustomSettingsLib (Recommended)
Install [CustomSettingsLib](https://steamcommunity.com/sharedfiles/filedetails/?id=3440421710) to access the visual settings menu:
1. Press `Esc` → **Settings** → **Mod Settings** → **AutoMiner**.
2. Customize:
   - **Require Wood Log Fuel**: Toggle fuel requirement on/off (Default: `ON`).
   - **Mining Speed Multiplier**: Adjust mining speed (Default: `1x`, up to `5x`).
   - **Fuel Duration Multiplier**: Adjust wood log burn duration (Default: `1x`, up to `5x`).
   - **Enable Endgame Miners**: Enable/disable endgame miners like Shards & Omnicrystal (Default: `ON`).

### Option B: Chat Commands (No External Mods Required)
If CustomSettingsLib is not installed, use the `/autominer` chat command:
- `/autominer status` — Display current configuration.
- `/autominer fuel <true|false>` — Enable or disable fuel requirement.
- `/autominer speed <1-5>` — Set mining speed multiplier.
- `/autominer fuelduration <1-5>` — Set fuel duration multiplier.
- `/autominer endgame <true|false>` — Enable or disable endgame miners.

---

## 📥 Installation

### Steam Workshop
1. Subscribe to the mod on the Steam Workshop.
2. (Optional but recommended) Subscribe to **CustomSettingsLib** for the in-game settings UI.
3. Launch Necesse and enable both mods in the **Mods** menu.

### Manual Installation
1. Download `AutominerFix-1.3.2-1.0.0.jar` from Releases.
2. Copy the `.jar` file to your Necesse mods directory:
   - **Windows**: `%APPDATA%/Necesse/mods/`
   - **macOS**: `~/Library/Application Support/Necesse/mods/`
   - **Linux**: `~/.config/Necesse/mods/`
3. Start Necesse and enable the mod in the **Mods** menu.

---

## 🛠️ Building from Source

### Prerequisites
- Java JDK 17+
- Necesse installed via Steam

### Build Steps
```bash
# Clone the repository
git clone https://github.com/mahardikamaulana/autominer.git
cd autominer

# Build the mod JAR
./gradlew buildModJar

# Build and automatically copy to your local Necesse mods folder
./gradlew copyMod
```

---

## 📜 Credits & License

- **Original Authors**: KKDJ20 & Agath ([Original Workshop](https://steamcommunity.com/sharedfiles/filedetails/?id=3440421710))
- **Maintainer**: Xeraphire
- Licensed under the [MIT License](LICENSE).
