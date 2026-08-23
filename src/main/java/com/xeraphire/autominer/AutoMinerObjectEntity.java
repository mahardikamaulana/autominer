package com.xeraphire.autominer;

import com.xeraphire.autominer.settings.AutoMinerConfig;
import necesse.entity.objectEntity.AnyLogFueledProcessingTechInventoryObjectEntity;
import necesse.inventory.InventoryItem;
import necesse.inventory.recipe.Recipe;
import necesse.inventory.recipe.Tech;
import necesse.level.maps.Level;

public class AutoMinerObjectEntity extends AnyLogFueledProcessingTechInventoryObjectEntity {
    public static int logFuelTime = 60000;
    public static int defaultRecipeProcessTime = 8000;
    public final AutoMinerDefinition definition;

    public AutoMinerObjectEntity(Level level, AutoMinerDefinition definition, int x, int y, Tech... techs) {
        super(level, definition != null ? definition.stringID : "autominer", x, y, 2, 2, false, false, true, techs);
        this.definition = definition;
    }

    public AutoMinerObjectEntity(Level level, String stringID, int x, int y, Tech... techs) {
        super(level, stringID, x, y, 2, 2, false, false, true, techs);
        this.definition = null;
    }

    @Override
    public int getFuelTime(InventoryItem item) {
        int duration = AutoMinerConfig.getFuelDuration();
        return logFuelTime * Math.max(1, duration);
    }

    @Override
    public int getProcessTime(Recipe recipe) {
        int baseTime = this.definition != null ? this.definition.processTimeMs : defaultRecipeProcessTime;
        int speed = Math.max(1, AutoMinerConfig.getMiningSpeed());
        return Math.max(50, baseTime / speed);
    }

    @Override
    public boolean isFuelRunning() {
        if (!AutoMinerConfig.isRequireFuel()) {
            return true;
        }
        return super.isFuelRunning();
    }

    @Override
    public boolean canUseFuel() {
        if (!AutoMinerConfig.isRequireFuel()) {
            return true;
        }
        return super.canUseFuel();
    }

    @Override
    public boolean useFuel(boolean force) {
        if (!AutoMinerConfig.isRequireFuel()) {
            return true;
        }
        return super.useFuel(force);
    }
}
