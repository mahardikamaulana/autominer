package com.xeraphire.autominer.settings;

import necesse.engine.modLoader.ModSettings;

public class AutoMinerConfig {
    private static ModSettings activeSettings;

    public static ModSettings initSettings() {
        try {
            Class.forName("customsettingslib.settings.CustomModSettings");
            activeSettings = (ModSettings) Class.forName("com.xeraphire.autominer.settings.AutoMinerCustomSettings")
                    .getDeclaredConstructor().newInstance();
        } catch (Throwable t) {
            activeSettings = new AutoMinerFallbackSettings();
        }
        return activeSettings;
    }

    public static ModSettings getActiveSettings() {
        if (activeSettings == null) {
            initSettings();
        }
        return activeSettings;
    }

    public static boolean isCustomSettingsLibDetected() {
        return !(getActiveSettings() instanceof AutoMinerFallbackSettings);
    }

    public static IAutoMinerSettings getSettings() {
        ModSettings settings = getActiveSettings();
        if (settings instanceof IAutoMinerSettings) {
            return (IAutoMinerSettings) settings;
        }
        return null;
    }

    public static boolean isRequireFuel() {
        IAutoMinerSettings s = getSettings();
        return s != null ? s.isRequireFuel() : true;
    }

    public static int getMiningSpeed() {
        IAutoMinerSettings s = getSettings();
        return s != null ? s.getMiningSpeed() : 1;
    }

    public static int getFuelDuration() {
        IAutoMinerSettings s = getSettings();
        return s != null ? s.getFuelDuration() : 1;
    }

    public static boolean isEnableEndgameMiners() {
        IAutoMinerSettings s = getSettings();
        return s != null ? s.isEnableEndgameMiners() : true;
    }

    public static void setRequireFuel(boolean requireFuel) {
        IAutoMinerSettings s = getSettings();
        if (s != null) {
            s.setRequireFuel(requireFuel);
        }
    }

    public static void setMiningSpeed(int speed) {
        IAutoMinerSettings s = getSettings();
        if (s != null) {
            s.setMiningSpeed(speed);
        }
    }

    public static void setFuelDuration(int duration) {
        IAutoMinerSettings s = getSettings();
        if (s != null) {
            s.setFuelDuration(duration);
        }
    }

    public static void setEnableEndgameMiners(boolean enable) {
        IAutoMinerSettings s = getSettings();
        if (s != null) {
            s.setEnableEndgameMiners(enable);
        }
    }
}
