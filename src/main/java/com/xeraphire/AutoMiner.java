package com.xeraphire;

import com.xeraphire.autominer.AutoMinerDefinition;
import com.xeraphire.autominer.AutoMinerObject;
import com.xeraphire.autominer.settings.AutoMinerChatCommand;
import com.xeraphire.autominer.settings.AutoMinerConfig;
import necesse.engine.commands.CommandsManager;
import necesse.engine.modLoader.ModSettings;
import necesse.engine.modLoader.annotations.ModEntry;
import necesse.engine.registries.ObjectRegistry;
import necesse.engine.registries.RecipeTechRegistry;
import necesse.inventory.recipe.Ingredient;
import necesse.inventory.recipe.Recipe;
import necesse.inventory.recipe.Recipes;
import necesse.inventory.recipe.Tech;

@ModEntry
public class AutoMiner {

    public ModSettings initSettings() {
        return AutoMinerConfig.initSettings();
    }

    public void init() {
        for (AutoMinerDefinition def : AutoMinerDefinition.DEFINITIONS) {
            Tech tech = RecipeTechRegistry.registerTech(def.techID, def.techID);
            AutoMinerObject obj = new AutoMinerObject(def, tech);
            def.tech = tech;
            def.object = obj;
            ObjectRegistry.registerObject(def.stringID, obj, 100.0f, true);
        }

        CommandsManager.registerServerCommand(new AutoMinerChatCommand());
        CommandsManager.registerClientCommand(new AutoMinerChatCommand());
    }

    public void initResources() {
    }

    public void postInit() {
        for (AutoMinerDefinition def : AutoMinerDefinition.DEFINITIONS) {
            // Recipe to craft the Auto Miner station
            Recipe stationRecipe = new Recipe(
                    def.stringID,
                    1,
                    def.craftStation.get(),
                    new Ingredient[]{new Ingredient(def.ingredientItem, def.ingredientAmount)}
            );
            if (def.showAfter != null) {
                stationRecipe.showAfter(def.showAfter);
            }
            Recipes.registerModRecipe(stationRecipe);
            def.stationRecipe = stationRecipe;

            // Recipe for the Auto Miner station to produce the resource
            Recipe productionRecipe = new Recipe(def.craftedItem, def.craftedAmount, def.tech, new Ingredient[0]);
            Recipes.registerModRecipe(productionRecipe);
            def.productionRecipe = productionRecipe;
            if (def.object != null) {
                def.object.productionRecipe = productionRecipe;
            }
        }
    }
}
