package com.xeraphire.autominer.settings;

import java.io.File;
import necesse.engine.GlobalData;
import necesse.engine.modLoader.ModSettings;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;

public class AutoMinerSettings extends ModSettings implements IAutoMinerSettings {
    public volatile boolean requireFuel = true;
    public volatile int miningSpeed = 1;
    public volatile int fuelDuration = 1;
    public volatile boolean autoSaveEnabled = false;

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

    // Same keys and layout as the CustomSettingsLib settings, so both writers produce an identical cfg file
    @Override
    public void addSaveData(SaveData save) {
        save.addBoolean("requireFuel", requireFuel);
        save.addInt("miningSpeed", miningSpeed);
        save.addInt("fuelDuration", fuelDuration);
    }

    @Override
    public void applyLoadData(LoadData save) {
        requireFuel = save.getBoolean("requireFuel", requireFuel);
        miningSpeed = Math.max(1, Math.min(10, save.getInt("miningSpeed", miningSpeed)));
        fuelDuration = Math.max(1, Math.min(10, save.getInt("fuelDuration", fuelDuration)));
        CustomSettingsLibBridge.syncToCustomSettings(this);
    }

    public void save() {
        try {
            File cfgFile = new File(GlobalData.cfgPath() + "/mods/xeraphire.autominer.cfg");
            if (cfgFile.getParentFile() != null && !cfgFile.getParentFile().exists()) {
                cfgFile.getParentFile().mkdirs();
            }
            SaveData save = new SaveData("SETTINGS");
            addSaveData(save);
            SaveData root = new SaveData("");
            root.addSaveData(save);
            root.saveScript(cfgFile);
        } catch (Exception | LinkageError t) {
            System.err.println("[AutoMiner] Failed to save config to disk: " + t.getMessage());
        }
    }

    private void saveIfEnabled() {
        if (autoSaveEnabled) {
            save();
        }
    }

    @Override
    public void setRequireFuel(boolean requireFuel) {
        this.requireFuel = requireFuel;
        CustomSettingsLibBridge.syncToCustomSettings(this);
        saveIfEnabled();
    }

    @Override
    public void setMiningSpeed(int speed) {
        this.miningSpeed = Math.max(1, Math.min(10, speed));
        CustomSettingsLibBridge.syncToCustomSettings(this);
        saveIfEnabled();
    }

    @Override
    public void setFuelDuration(int duration) {
        this.fuelDuration = Math.max(1, Math.min(10, duration));
        CustomSettingsLibBridge.syncToCustomSettings(this);
        saveIfEnabled();
    }
}
