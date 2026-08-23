package com.xeraphire.autominer.settings;

import necesse.engine.modLoader.ModSettings;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;

public class AutoMinerFallbackSettings extends ModSettings implements IAutoMinerSettings {
    public boolean requireFuel = true;
    public int miningSpeed = 1;
    public int fuelDuration = 1;
    public boolean enableEndgameMiners = true;

    @Override
    public boolean isRequireFuel() {
        return requireFuel;
    }

    @Override
    public int getMiningSpeed() {
        return miningSpeed;
    }

    @Override
    public int getFuelDuration() {
        return fuelDuration;
    }

    @Override
    public boolean isEnableEndgameMiners() {
        return enableEndgameMiners;
    }

    @Override
    public void addSaveData(SaveData save) {
        save.addBoolean("requireFuel", requireFuel);
        save.addInt("miningSpeed", miningSpeed);
        save.addInt("fuelDuration", fuelDuration);
        save.addBoolean("enableEndgameMiners", enableEndgameMiners);
    }

    @Override
    public void applyLoadData(LoadData save) {
        requireFuel = save.getBoolean("requireFuel", requireFuel);
        miningSpeed = Math.max(1, Math.min(10, save.getInt("miningSpeed", miningSpeed)));
        fuelDuration = Math.max(1, Math.min(10, save.getInt("fuelDuration", fuelDuration)));
        enableEndgameMiners = save.getBoolean("enableEndgameMiners", enableEndgameMiners);
    }

    @Override
    public void setRequireFuel(boolean requireFuel) {
        this.requireFuel = requireFuel;
    }

    @Override
    public void setMiningSpeed(int speed) {
        this.miningSpeed = Math.max(1, Math.min(10, speed));
    }

    @Override
    public void setFuelDuration(int duration) {
        this.fuelDuration = Math.max(1, Math.min(10, duration));
    }

    @Override
    public void setEnableEndgameMiners(boolean enable) {
        this.enableEndgameMiners = enable;
    }
}
