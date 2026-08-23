package com.xeraphire.autominer;

import java.util.function.Supplier;
import necesse.engine.registries.RecipeTechRegistry;
import necesse.inventory.recipe.Recipe;
import necesse.inventory.recipe.Tech;

public class AutoMinerDefinition {
    public final String stringID;
    public final String texturePath;
    public final String techID;
    public final String craftedItem;
    public final int craftedAmount;
    public final Supplier<Tech> craftStation;
    public final String ingredientItem;
    public final int ingredientAmount;
    public final String showAfter;
    public final int processTimeMs;

    public Tech tech;
    public AutoMinerObject object;
    public Recipe productionRecipe;
    public Recipe stationRecipe;

    public AutoMinerDefinition(
            String stringID,
            String texturePath,
            String techID,
            String craftedItem,
            int craftedAmount,
            Supplier<Tech> craftStation,
            String ingredientItem,
            int ingredientAmount,
            String showAfter,
            int processTimeMs
    ) {
        this.stringID = stringID;
        this.texturePath = texturePath;
        this.techID = techID;
        this.craftedItem = craftedItem;
        this.craftedAmount = craftedAmount;
        this.craftStation = craftStation;
        this.ingredientItem = ingredientItem;
        this.ingredientAmount = ingredientAmount;
        this.showAfter = showAfter;
        this.processTimeMs = processTimeMs;
    }

    public AutoMinerDefinition(
            String stringID,
            String texturePath,
            String techID,
            String craftedItem,
            int craftedAmount,
            Supplier<Tech> craftStation,
            String ingredientItem,
            int ingredientAmount,
            String showAfter
    ) {
        this(stringID, texturePath, techID, craftedItem, craftedAmount, craftStation, ingredientItem, ingredientAmount, showAfter, 8000);
    }

    public static final AutoMinerDefinition[] DEFINITIONS = new AutoMinerDefinition[] {
        // ==========================================
        // TIER 1: Workstation (Early Game / Surface)
        // ==========================================

        // 1. Earth & Basic Blocks (Output: 2 or 1, Process: 4s)
        new AutoMinerDefinition("autominerstone", "objects/StoneAM", "AUTOMINERSTONETECH", "stone", 2, () -> RecipeTechRegistry.getTech("workstation"), "stone", 25, "forge", 4000),
        new AutoMinerDefinition("autominerclay", "objects/ClayAM", "AUTOMINERCLAYTECH", "clay", 1, () -> RecipeTechRegistry.getTech("workstation"), "clay", 25, "autominerstone", 4000),
        new AutoMinerDefinition("autominersand", "objects/autominersand", "AUTOMINERSANDTECH", "sandtile", 1, () -> RecipeTechRegistry.getTech("workstation"), "sandtile", 25, "autominerclay", 4000),
        new AutoMinerDefinition("autominersandstone", "objects/autominersandstone", "AUTOMINERSANDSTONETECH", "sandstone", 2, () -> RecipeTechRegistry.getTech("workstation"), "sandstone", 25, "autominersand", 4000),
        new AutoMinerDefinition("autominersnowstone", "objects/autominersnowstone", "AUTOMINERSNOWSTONETECH", "snowstone", 2, () -> RecipeTechRegistry.getTech("workstation"), "snowstone", 25, "autominersandstone", 4000),
        new AutoMinerDefinition("autominerswampstone", "objects/autominerswampstone", "AUTOMINERSWAMPSTONETECH", "swampstone", 2, () -> RecipeTechRegistry.getTech("workstation"), "swampstone", 25, "autominersnowstone", 4000),

        // 2. Early Cave & Surface Ores (Output: 1, Process: 8s)
        new AutoMinerDefinition("autominercopper", "objects/CopperAM", "AUTOMINERCOPPERTECH", "copperore", 1, () -> RecipeTechRegistry.getTech("workstation"), "copperore", 15, "autominerstone", 8000),
        new AutoMinerDefinition("automineriron", "objects/IronAM", "AUTOMINERIRONTECH", "ironore", 1, () -> RecipeTechRegistry.getTech("workstation"), "ironore", 15, "autominercopper", 8000),
        new AutoMinerDefinition("autominergold", "objects/GoldAM", "AUTOMINERGOLDTECH", "goldore", 1, () -> RecipeTechRegistry.getTech("workstation"), "goldore", 15, "automineriron", 8000),
        new AutoMinerDefinition("autominermfrostshards", "objects/FrostShardAM", "AUTOMINERFROSTSHARDTECH", "frostshard", 1, () -> RecipeTechRegistry.getTech("workstation"), "frostshard", 15, "autominergold", 8000),

        // 3. Early Cave Gems & Crystals (Output: 1, Process: 8s)
        new AutoMinerDefinition("automineramethyst", "objects/automineramethyst", "AUTOMINERAMETHYSTTECH", "amethyst", 1, () -> RecipeTechRegistry.getTech("workstation"), "amethyst", 5, "autominergold", 8000),
        new AutoMinerDefinition("autominertopaz", "objects/autominertopaz", "AUTOMINERTOPAZTECH", "topaz", 1, () -> RecipeTechRegistry.getTech("workstation"), "topaz", 10, "automineramethyst", 8000),
        new AutoMinerDefinition("automineremerald", "objects/automineremerald", "AUTOMINEREMERALDTECH", "emerald", 1, () -> RecipeTechRegistry.getTech("workstation"), "emerald", 5, "autominertopaz", 8000),
        new AutoMinerDefinition("autominersapphire", "objects/autominersapphire", "AUTOMINERSAPPHIRETECH", "sapphire", 1, () -> RecipeTechRegistry.getTech("workstation"), "sapphire", 5, "automineremerald", 8000),
        new AutoMinerDefinition("autominerruby", "objects/autominerruby", "AUTOMINERRUBYTECH", "ruby", 1, () -> RecipeTechRegistry.getTech("workstation"), "ruby", 5, "autominersapphire", 8000),
        new AutoMinerDefinition("autominerdiamond", "objects/autominerdiamond", "AUTOMINERDIAMONDTECH", "pearlescentdiamond", 1, () -> RecipeTechRegistry.getTech("workstation"), "pearlescentdiamond", 5, "autominerruby", 8000),
        new AutoMinerDefinition("automineramber", "objects/automineramber", "AUTOMINERAMBERTECH", "amber", 1, () -> RecipeTechRegistry.getTech("workstation"), "amber", 10, "autominerdiamond", 8000),

        // 4. Basic Creature Drops & Materials (Output: 1, Process: 8s)
        new AutoMinerDefinition("autominerbatwing", "objects/autominerbatwing", "AUTOMINERBATWINGTECH", "batwing", 1, () -> RecipeTechRegistry.getTech("workstation"), "batwing", 15, "autominerstone", 8000),
        new AutoMinerDefinition("autominerbone", "objects/autominerbone", "AUTOMINERBONETECH", "bone", 1, () -> RecipeTechRegistry.getTech("workstation"), "bone", 15, "autominerbatwing", 8000),
        new AutoMinerDefinition("autominersilk", "objects/autominersilk", "AUTOMINERSILKTECH", "silk", 1, () -> RecipeTechRegistry.getTech("workstation"), "silk", 15, "autominerbone", 8000),
        new AutoMinerDefinition("autominerwool", "objects/WoolAM", "AUTOMINERWOOLTECH", "wool", 1, () -> RecipeTechRegistry.getTech("workstation"), "wool", 10, "autominersilk", 8000),
        new AutoMinerDefinition("autominerclothscraps", "objects/autominerclothscraps", "AUTOMINERCLOTHSCRAPSTECH", "clothscraps", 1, () -> RecipeTechRegistry.getTech("workstation"), "clothscraps", 15, "autominerwool", 8000),

        // ==========================================
        // TIER 2: Demonic Workstation (Mid Game)
        // ==========================================

        // 1. Demonic Ores & Minerals (Output: 1, Process: 12s)
        new AutoMinerDefinition("autominerivy", "objects/IvyAM", "AUTOMINERIVYTECH", "ivyore", 1, () -> RecipeTechRegistry.getTech("demonic"), "ivybar", 15, null, 12000),
        new AutoMinerDefinition("autominerquartz", "objects/QuartzAM", "AUTOMINERQUARTZTECH", "quartz", 1, () -> RecipeTechRegistry.getTech("demonic"), "quartz", 15, "autominerivy", 12000),
        new AutoMinerDefinition("autominerspideriteore", "objects/autominerspideriteore", "AUTOMINERSPIDERITEORETECH", "spideriteore", 1, () -> RecipeTechRegistry.getTech("demonic"), "spideritebar", 15, "autominerquartz", 12000),
        new AutoMinerDefinition("autominerlifequartz", "objects/autominerlifequartz", "AUTOMINERLIFEQUARTZTECH", "lifequartz", 1, () -> RecipeTechRegistry.getTech("demonic"), "lifequartz", 15, "autominerspideriteore", 12000),

        // 2. Dungeon Drops & Essences (Output: 1, Process: 12s)
        new AutoMinerDefinition("autominervoidshard", "objects/autominervoidshard", "AUTOMINERVOIDSHARDTECH", "voidshard", 1, () -> RecipeTechRegistry.getTech("demonic"), "voidshard", 15, "autominerlifequartz", 12000),
        new AutoMinerDefinition("autominerectoplasm", "objects/autominerectoplasm", "AUTOMINERECTOPLASMTECH", "ectoplasm", 1, () -> RecipeTechRegistry.getTech("demonic"), "ectoplasm", 15, "autominervoidshard", 12000),
        new AutoMinerDefinition("autominerphantomdust", "objects/autominerphantomdust", "AUTOMINERPHANTOMDUSTTECH", "phantomdust", 1, () -> RecipeTechRegistry.getTech("demonic"), "phantomdust", 15, "autominerectoplasm", 12000),
        new AutoMinerDefinition("autominerbloodessence", "objects/autominerbloodessence", "AUTOMINERBLOODESSENCETECH", "bloodessence", 1, () -> RecipeTechRegistry.getTech("demonic"), "bloodessence", 10, "autominerphantomdust", 12000),
        new AutoMinerDefinition("autominerslimeessence", "objects/autominerslimeessence", "AUTOMINERSLIMEESSENCETECH", "slimeessence", 1, () -> RecipeTechRegistry.getTech("demonic"), "slimeessence", 10, "autominerbloodessence", 12000),
        new AutoMinerDefinition("autominerslimematter", "objects/autominerslimematter", "AUTOMINERSLIMEMATTERTECH", "slimematter", 1, () -> RecipeTechRegistry.getTech("demonic"), "slimematter", 15, "autominerslimeessence", 12000),
        new AutoMinerDefinition("autominerslimeum", "objects/autominerslimeum", "AUTOMINERSLIMEUMTECH", "slimeum", 1, () -> RecipeTechRegistry.getTech("demonic"), "slimeum", 15, "autominerslimematter", 12000),
        new AutoMinerDefinition("autominerspideressence", "objects/autominerspideressence", "AUTOMINERSPIDERESSENCETECH", "spideressence", 1, () -> RecipeTechRegistry.getTech("demonic"), "spideressence", 10, "autominerslimeum", 12000),
        new AutoMinerDefinition("autominerspidervenom", "objects/autominerspidervenom", "AUTOMINERSPIDERVENOMTECH", "spidervenom", 1, () -> RecipeTechRegistry.getTech("demonic"), "spidervenom", 15, "autominerspideressence", 12000),
        new AutoMinerDefinition("autominercavespidergland", "objects/autominercavespidergland", "AUTOMINERCAVESPIDERGLANDTECH", "cavespidergland", 1, () -> RecipeTechRegistry.getTech("demonic"), "cavespidergland", 15, "autominerspidervenom", 12000),
        new AutoMinerDefinition("autominerspiderstone", "objects/autominerspiderstone", "AUTOMINERSPIDERSTONETECH", "spiderstone", 1, () -> RecipeTechRegistry.getTech("demonic"), "spiderstone", 10, "autominercavespidergland", 12000),
        new AutoMinerDefinition("autominerwormcarapace", "objects/autominerwormcarapace", "AUTOMINERWORMCARAPACETECH", "wormcarapace", 1, () -> RecipeTechRegistry.getTech("demonic"), "wormcarapace", 15, "autominerspiderstone", 12000),

        // ==========================================
        // TIER 3: Tungsten Workstation (Deep Caves)
        // ==========================================

        // 1. Deep Earth & Hard Stone (Output: 2 or 1, Process: 8s)
        new AutoMinerDefinition("autominerdeepstone", "objects/DeepStoneAM", "AUTOMINERDEEPSTONETECH", "deepstone", 2, () -> RecipeTechRegistry.getTech("tungstenworkstation"), "deepstone", 25, null, 8000),
        new AutoMinerDefinition("autominerdeepsandstone", "objects/autominerdeepsandstone", "AUTOMINERDEEPSANDSTONETECH", "deepsandstone", 1, () -> RecipeTechRegistry.getTech("tungstenworkstation"), "deepsandstone", 25, "autominerdeepstone", 8000),
        new AutoMinerDefinition("autominerdeepsnowstone", "objects/autominerdeepsnowstone", "AUTOMINERDEEPSNOWSTONETECH", "deepsnowstone", 1, () -> RecipeTechRegistry.getTech("tungstenworkstation"), "deepsnowstone", 25, "autominerdeepsandstone", 8000),
        new AutoMinerDefinition("autominerdeepswampstone", "objects/autominerdeepswampstone", "AUTOMINERDEEPSWAMPSTONETECH", "deepswampstone", 1, () -> RecipeTechRegistry.getTech("tungstenworkstation"), "deepswampstone", 25, "autominerdeepsnowstone", 8000),
        new AutoMinerDefinition("autominergranite", "objects/autominergranite", "AUTOMINERGRANITETECH", "granite", 2, () -> RecipeTechRegistry.getTech("tungstenworkstation"), "granite", 25, "autominerdeepswampstone", 8000),
        new AutoMinerDefinition("autominerbasalt", "objects/autominerbasalt", "AUTOMINERBASALTTECH", "basalt", 2, () -> RecipeTechRegistry.getTech("tungstenworkstation"), "basalt", 25, "autominergranite", 8000),
        new AutoMinerDefinition("autominerrunestone", "objects/autominerrunestone", "AUTOMINERRUNESTONETECH", "runestone", 1, () -> RecipeTechRegistry.getTech("tungstenworkstation"), "runestone", 15, "autominerbasalt", 8000),

        // 2. Deep Cave & Glacial Ores (Output: 1, Process: 16s)
        new AutoMinerDefinition("autominertungsten", "objects/TungstenAM", "AUTOMINERTUNGSTENTECH", "tungstenore", 1, () -> RecipeTechRegistry.getTech("tungstenworkstation"), "tungstenbar", 15, "autominerrunestone", 16000),
        new AutoMinerDefinition("autominerobsidian", "objects/autominerobsidian", "AUTOMINEROBSIDIANTECH", "obsidian", 1, () -> RecipeTechRegistry.getTech("tungstenworkstation"), "obsidian", 15, "autominertungsten", 16000),
        new AutoMinerDefinition("autominerglacial", "objects/GlacialAM", "AUTOMINERGLACIALTECH", "glacialore", 1, () -> RecipeTechRegistry.getTech("tungstenworkstation"), "glacialbar", 15, "autominerobsidian", 16000),
        new AutoMinerDefinition("autominerglacialshard", "objects/autominerglacialshard", "AUTOMINERGLACIALSHARDTECH", "glacialshard", 1, () -> RecipeTechRegistry.getTech("tungstenworkstation"), "glacialshard", 15, "autominerglacial", 16000),
        new AutoMinerDefinition("autominerancientfossil", "objects/ancientfossilam", "AUTOMINERANCIENTFOSSILTECH", "ancientfossilore", 1, () -> RecipeTechRegistry.getTech("tungstenworkstation"), "ancientfossilbar", 15, "autominerglacialshard", 16000),
        new AutoMinerDefinition("autominermycelium", "objects/MyceliumAM", "AUTOMINERMYCELIUMTECH", "myceliumore", 1, () -> RecipeTechRegistry.getTech("tungstenworkstation"), "myceliumbar", 15, "autominerancientfossil", 16000),
        new AutoMinerDefinition("autominernightsteelore", "objects/autominernightsteelore", "AUTOMINERNIGHTSTEELORETECH", "nightsteelore", 1, () -> RecipeTechRegistry.getTech("tungstenworkstation"), "nightsteelbar", 15, "autominermycelium", 16000),

        // ==========================================
        // TIER 4: Fallen Workstation (Endgame)
        // ==========================================

        // Endgame Shards, Crystals & Dust (Output: 1, Process: 24s)
        new AutoMinerDefinition("autominerupgradeshards", "objects/upgradeshardsAM", "AUTOMINERUPGRADESHARDSTECH", "upgradeshard", 1, () -> RecipeTechRegistry.getTech("fallen"), "upgradeshard", 15, null, 24000),
        new AutoMinerDefinition("automineralchemyshards", "objects/alchemyshardsAM", "AUTOMINERALCHEMYSHARDSTECH", "alchemyshard", 1, () -> RecipeTechRegistry.getTech("fallen"), "alchemyshard", 15, "autominerupgradeshards", 24000),
        new AutoMinerDefinition("automineraltardust", "objects/AltarDustAM", "AUTOMINERALTARDUSTTECH", "altardust", 1, () -> RecipeTechRegistry.getTech("fallen"), "altardust", 10, "automineralchemyshards", 24000),
        new AutoMinerDefinition("automineromnicrystal", "objects/automineromnicrystal", "AUTOMINEROMNICRYSTALTECH", "omnicrystal", 1, () -> RecipeTechRegistry.getTech("fallen"), "omnicrystal", 5, "automineraltardust", 24000),
        new AutoMinerDefinition("autominershadowessence", "objects/autominershadowessence", "AUTOMINERSHADOWESSENCETECH", "shadowessence", 1, () -> RecipeTechRegistry.getTech("fallen"), "shadowessence", 10, "automineromnicrystal", 24000),
        new AutoMinerDefinition("autominercryoessence", "objects/autominercryoessence", "AUTOMINERCRYOESSENCETECH", "cryoessence", 1, () -> RecipeTechRegistry.getTech("fallen"), "cryoessence", 10, "autominershadowessence", 24000),
        new AutoMinerDefinition("autominerbioessence", "objects/autominerbioessence", "AUTOMINERBIOESSENCETECH", "bioessence", 1, () -> RecipeTechRegistry.getTech("fallen"), "bioessence", 10, "autominercryoessence", 24000),
        new AutoMinerDefinition("autominerprimordialessence", "objects/autominerprimordialessence", "AUTOMINERPRIMORDIALESSENCETECH", "primordialessence", 1, () -> RecipeTechRegistry.getTech("fallen"), "primordialessence", 10, "autominerbioessence", 24000)
    };
}
