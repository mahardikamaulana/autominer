package com.xeraphire.autominer.settings;

import customsettingslib.settings.CustomModSettings;

public class AutoMinerCustomSettings extends CustomModSettings implements IAutoMinerSettings {

    public AutoMinerCustomSettings() {
        super();
        addServerSettings("requireFuel", "miningSpeed", "fuelDuration", "enableEndgameMiners");

        addTextSeparator("generalsection");
        addBooleanSetting("requireFuel", true);
        addCustomSetting(new MultiplierSetting("miningSpeed", 1, 1, 10));
        addCustomSetting(new MultiplierSetting("fuelDuration", 1, 1, 10));

        addTextSeparator("balancesection");
        addBooleanSetting("enableEndgameMiners", true);
    }

    @Override
    public boolean isRequireFuel() {
        Object val = getSetting("requireFuel");
        return val instanceof Boolean ? (Boolean) val : true;
    }

    @Override
    public int getMiningSpeed() {
        Object val = getSetting("miningSpeed");
        if (val instanceof Integer) {
            return Math.max(1, Math.min(10, (Integer) val));
        }
        return 1;
    }

    @Override
    public int getFuelDuration() {
        Object val = getSetting("fuelDuration");
        if (val instanceof Integer) {
            return Math.max(1, Math.min(10, (Integer) val));
        }
        return 1;
    }

    @Override
    public boolean isEnableEndgameMiners() {
        Object val = getSetting("enableEndgameMiners");
        return val instanceof Boolean ? (Boolean) val : true;
    }

    @Override
    public void setRequireFuel(boolean requireFuel) {
        setSettingValue("requireFuel", requireFuel);
    }

    @Override
    public void setMiningSpeed(int speed) {
        int val = Math.max(1, Math.min(10, speed));
        setSettingValue("miningSpeed", val);
    }

    @Override
    public void setFuelDuration(int duration) {
        int val = Math.max(1, Math.min(10, duration));
        setSettingValue("fuelDuration", val);
    }

    @Override
    public void setEnableEndgameMiners(boolean enable) {
        setSettingValue("enableEndgameMiners", enable);
    }

    public void setSettingValue(String settingID, Object value) {
        customsettingslib.components.CustomModSetting<?> setting = settingsMap.get(settingID);
        if (setting != null) {
            serverDataSettings.put(settingID, value);
        }
    }
}
