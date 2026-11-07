# Autominer Fix

![Preview](src/main/resources/preview.png)

An automated mining and resource generation mod for **Necesse** (v1.3.3+).

Adds placeable automated miners for 75 ores, gems, stone types, wood logs, mob drops, and endgame crafting materials.

## Features

- **75 Automated Miners**: Covers early-game stones and ores up to endgame materials and shards.
- **Tier-Based Unlocks**: Miners are crafted across progression workstations (Workstation, Demonic Workstation, Tungsten Workstation, Fallen Workstation).
- **Configurable Fuel System**: Optional wood fuel requirement with configurable burn duration.
- **Configurable Speed**: Adjustable mining speed multiplier (1x to 10x).
- **In-Game Settings**: Configurable via [CustomSettingsLib](https://steamcommunity.com/sharedfiles/filedetails/?id=3440421710) GUI (`Settings` → `Mod Settings` → `AutoMiner`).
- **Chat Command Fallback**: Control settings in-game via `/autominer` without extra mods.
- **Multi-language Support**: English (`en`), Korean (`kr`), German (`de`), and Spanish (`es`).

## Miner Progression

### Tier 1: Workstation
- **Earth & Blocks**: Stone, Clay, Sand, Sandstone, Snowstone, Swampstone
- **Ores**: Copper Ore, Iron Ore, Gold Ore, Frost Shards
- **Gems**: Amethyst, Topaz, Emerald, Sapphire, Ruby, Diamond, Amber
- **Mob Drops**: Bat Wing, Bone, Silk, Wool, Cloth Scraps, Leather
- **Wood**: Oak, Birch, Maple, Spruce, Pine, Palm, Willow Logs (see [Wood Miners](#wood-miners))

### Tier 2: Demonic Workstation
- **Minerals**: Ivy Ore, Quartz, Spiderite Ore, Life Quartz
- **Essences & Drops**: Void Shard, Ectoplasm, Phantom Dust, Blood Essence, Slime Essence, Slime Matter, Slimeum, Spider Essence, Spider Venom, Cave Spider Gland, Spider Stone, Worm Carapace
- **Exotic Wood**: Dryad Log, Deadwood Log

### Tier 3: Tungsten Workstation
- **Deep Stones**: Deep Stone, Deep Sandstone, Deep Snowstone, Deep Swampstone, Granite, Basalt, Runestone
- **Deep & Glacial Ores**: Tungsten Ore, Obsidian, Glacial Ore, Glacial Shard, Ancient Fossil Ore, Mycelium Ore, Nightsteel Ore

### Tier 4: Fallen Workstation
- **Endgame Materials**: Upgrade Shards, Alchemy Shards, Altar Dust, Omnicrystal, Shadow Essence, Cryo Essence, Bio Essence, Primordial Essence, Crystal Essence, Radiated Essence, Pristine Gear, Fuel Skull, Ascended Crystal (Shard)
- **High-Density Fuel**: Fuel Skulls can also be burned in any autominer for 2x the burn duration of standard logs!

### Wood Miners
Wood miners are crafted purely from their own sapling (40 each, 90 for Deadwood) and take one tile like every other autominer.
- **Fuel-locked output**: With fuel required, a wood miner always produces **5 logs per log burned** (12s per log at default settings), netting 4 logs/min. The Mining Speed setting doesn't change this ratio, so wood miners can't be used to mint unlimited fuel; one wood miner can keep about four other autominers fueled.
- **Balanced against planting**: Per sapling spent, a wood miner yields about 80% of what planting those saplings would, in exchange for a single tile and no chopping or replanting.
- **Fuel disabled**: Wood miners follow the normal Mining Speed multiplier.

## Configuration

### In-Game UI (via CustomSettingsLib)
If [CustomSettingsLib](https://steamcommunity.com/sharedfiles/filedetails/?id=3440421710) is installed, navigate to **Settings** → **Mod Settings** → **AutoMiner**:
- **Require Wood Log Fuel**: Toggle fuel requirement (Default: `ON`).
- **Mining Speed Multiplier**: Adjust production rate (1x - 10x).
- **Fuel Duration Multiplier**: Adjust fuel burn duration (1x - 10x).

### Chat Commands
Settings can also be modified using chat commands:
- `/autominer status` — Show current configuration.
- `/autominer fuel <true|false>` — Toggle fuel requirement.
- `/autominer speed <1-10>` — Set mining speed multiplier.
- `/autominer fuelduration <1-10>` — Set fuel duration multiplier.

## Installation

### Steam Workshop
1. Subscribe to the mod on the Steam Workshop.
2. (Optional) Subscribe to **CustomSettingsLib** for the in-game settings GUI.
3. Enable both in the Necesse **Mods** menu.

### Manual Installation
1. Download the latest `.jar` from Releases.
2. Place the file into your Necesse `mods/` directory:
   - **Windows**: `%APPDATA%/Necesse/mods/`
   - **macOS**: `~/Library/Application Support/Necesse/mods/`
   - **Linux**: `~/.config/Necesse/mods/`
3. Enable the mod in the in-game **Mods** menu.

## Building from Source

Requires JDK 17+.

```bash
# Clone repository
git clone https://github.com/mahardikamaulana/autominer.git
cd autominer

# Build JAR
./gradlew buildModJar

# Build and copy directly to local Necesse mods directory
./gradlew copyMod
```

## Credits

- **Original Mod**: KKDJ20, Agath, Mako
- **Maintainer**: Xeraphire
- **License**: [MIT](LICENSE)
