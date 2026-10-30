package com.xeraphire.autominer.settings;

import necesse.engine.modLoader.ModSettings;

public class AutoMinerConfig {
    private static final AutoMinerSettings settings = new AutoMinerSettings();
    private static final long SYNC_INTERVAL_NANOS = 250_000_000L; // 250ms throttle for hot-path tick reads
    private static volatile long lastSyncNanos = 0L;

    public static ModSettings initSettings() {
        settings.autoSaveEnabled = true;
        if (CustomSettingsLibBridge.tryRegisterSettings(settings)) {
            ModSettings customSettings = CustomSettingsLibBridge.getCustomModSettings();
            if (customSettings != null) {
                return customSettings;
            }
        }
        return settings;
    }

    public static AutoMinerSettings getSettings() {
        long now = System.nanoTime();
        if (lastSyncNanos == 0L || now - lastSyncNanos >= SYNC_INTERVAL_NANOS) {
            lastSyncNanos = now;
            CustomSettingsLibBridge.syncFromCustomSettings(settings);
        }
        return settings;
    }

    static void markSyncedNow() {
        lastSyncNanos = System.nanoTime();
    }

    static void invalidateSyncTimer() {
        lastSyncNanos = 0L;
    }

    public static boolean isCustomSettingsLibDetected() {
        return CustomSettingsLibBridge.isAvailable();
    }

    public static boolean isRequireFuel() {
        return getSettings().isRequireFuel();
    }

    public static int getMiningSpeed() {
        return getSettings().getMiningSpeed();
    }

    public static int getFuelDuration() {
        return getSettings().getFuelDuration();
    }

    public static boolean isEnableEndgameMiners() {
        return true;
    }

    public static void setRequireFuel(boolean requireFuel) {
        settings.setRequireFuel(requireFuel);
    }

    public static void setMiningSpeed(int speed) {
        settings.setMiningSpeed(speed);
    }

    public static void setFuelDuration(int duration) {
        settings.setFuelDuration(duration);
    }

    public static void setEnableEndgameMiners(boolean enable) {
        settings.setEnableEndgameMiners(true);
    }
}
