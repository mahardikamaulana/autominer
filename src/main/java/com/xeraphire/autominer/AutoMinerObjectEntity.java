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
    private boolean burningFreeFuel = false;

    public AutoMinerObjectEntity(Level level, AutoMinerDefinition definition, int x, int y, Tech... techs) {
        super(level, definition != null ? definition.stringID : "autominer", x, y, 2, 2, false, false, true, techs);
        this.definition = definition;
    }

    public boolean isEndgameDisabled() {
        return false;
    }

    public boolean isBurningFreeFuel() {
        return this.burningFreeFuel;
    }

    @Override
    public boolean isFuelRunning() {
        if (this.isEndgameDisabled()) {
            return false;
        }
        if (this.burningFreeFuel && AutoMinerConfig.isRequireFuel()) {
            return false;
        }
        return super.isFuelRunning();
    }

    @Override
    public boolean isValidFuelItem(InventoryItem item) {
        if (item != null && item.item != null && "fuelskull".equals(item.item.getStringID())) {
            return true;
        }
        return super.isValidFuelItem(item);
    }

    @Override
    public int getFuelTime(InventoryItem item) {
        int duration = AutoMinerConfig.getFuelDuration();
        int baseTime = (item != null && item.item != null && "fuelskull".equals(item.item.getStringID()))
                ? logFuelTime * 5
                : logFuelTime;
        return baseTime * Math.max(1, duration);
    }

    @Override
    public int getProcessTime(Recipe recipe) {
        int baseTime = this.definition != null ? this.definition.processTimeMs : defaultRecipeProcessTime;
        int speed = Math.max(1, AutoMinerConfig.getMiningSpeed());
        return Math.max(50, baseTime / speed);
    }

    @Override
    public boolean canUseFuel() {
        if (this.isEndgameDisabled()) {
            return false;
        }
        if (!AutoMinerConfig.isRequireFuel()) {
            return true;
        }
        return super.canUseFuel();
    }

    @Override
    public int getNextFuelBurnTime(boolean consume) {
        if (this.isEndgameDisabled()) {
            if (consume) {
                this.burningFreeFuel = false;
            }
            return 0;
        }
        if (!AutoMinerConfig.isRequireFuel()) {
            if (consume) {
                this.burningFreeFuel = true;
            }
            return this.getFuelTime(null);
        }
        int burnTime = this.itemToBurnTime(consume, (item) -> {
            if (this.isValidFuelItem(item)) {
                return this.getFuelTime(item);
            }
            return 0;
        });
        if (consume && burnTime > 0) {
            this.burningFreeFuel = false;
        }
        return burnTime;
    }
}
