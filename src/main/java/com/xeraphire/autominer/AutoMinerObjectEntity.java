package com.xeraphire.autominer;

import com.xeraphire.autominer.settings.AutoMinerConfig;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.entity.objectEntity.AnyLogFueledProcessingTechInventoryObjectEntity;
import necesse.inventory.InventoryItem;
import necesse.inventory.recipe.Recipe;
import necesse.inventory.recipe.Tech;
import necesse.level.maps.Level;

public class AutoMinerObjectEntity extends AnyLogFueledProcessingTechInventoryObjectEntity {
    public static int logFuelTime = 60000;
    public static int fuelSkullMultiplier = 2;
    public static int defaultRecipeProcessTime = 8000;
    public final AutoMinerDefinition definition;
    private boolean burningFreeFuel = false;

    public AutoMinerObjectEntity(Level level, AutoMinerDefinition definition, int x, int y, Tech... techs) {
        super(level, definition != null ? definition.stringID : "autominer", x, y, 2, 2, false, false, true, techs);
        this.definition = definition;
    }

    public boolean isBurningFreeFuel() {
        return this.burningFreeFuel;
    }

    @Override
    public boolean isFuelRunning() {
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
                ? logFuelTime * fuelSkullMultiplier
                : logFuelTime;
        return baseTime * Math.max(1, duration);
    }

    @Override
    public int getProcessTime(Recipe recipe) {
        if (this.definition != null && this.definition.isFuelRatioLocked() && AutoMinerConfig.isRequireFuel()) {
            // Tie output to fuel burn so wood miners always yield exactly fuelLogRatio logs per log, whatever the settings
            int duration = Math.max(1, AutoMinerConfig.getFuelDuration());
            return Math.max(50, logFuelTime * duration / this.definition.fuelLogRatio);
        }
        int baseTime = this.definition != null ? this.definition.processTimeMs : defaultRecipeProcessTime;
        int speed = Math.max(1, AutoMinerConfig.getMiningSpeed());
        return Math.max(50, baseTime / speed);
    }

    @Override
    public boolean canUseFuel() {
        if (!AutoMinerConfig.isRequireFuel()) {
            return true;
        }
        return super.canUseFuel();
    }

    @Override
    public int getNextFuelBurnTime(boolean consume) {
        if (!AutoMinerConfig.isRequireFuel()) {
            if (consume) {
                this.setBurningFreeFuel(true);
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
            this.setBurningFreeFuel(false);
        }
        return burnTime;
    }

    private void setBurningFreeFuel(boolean burningFreeFuel) {
        if (this.burningFreeFuel != burningFreeFuel) {
            this.burningFreeFuel = burningFreeFuel;
            // The flag rides on the progress packet, so clients see free-fuel cutoffs too
            this.markProgressDirty();
        }
    }

    // Persist the free-fuel flag so a free burn in progress can't survive a reload with requireFuel enabled
    @Override
    public void addSaveData(SaveData save) {
        super.addSaveData(save);
        save.addBoolean("burningFreeFuel", this.burningFreeFuel);
    }

    @Override
    public void applyLoadData(LoadData save) {
        super.applyLoadData(save);
        this.burningFreeFuel = save.getBoolean("burningFreeFuel", false);
    }

    @Override
    public void setupContentPacket(PacketWriter writer) {
        super.setupContentPacket(writer);
        writer.putNextBoolean(this.burningFreeFuel);
    }

    @Override
    public void applyContentPacket(PacketReader reader) {
        super.applyContentPacket(reader);
        this.burningFreeFuel = reader.getNextBoolean();
    }

    @Override
    public void setupProgressPacket(PacketWriter writer) {
        super.setupProgressPacket(writer);
        writer.putNextBoolean(this.burningFreeFuel);
    }

    @Override
    public void applyProgressPacket(PacketReader reader) {
        super.applyProgressPacket(reader);
        this.burningFreeFuel = reader.getNextBoolean();
    }
}
