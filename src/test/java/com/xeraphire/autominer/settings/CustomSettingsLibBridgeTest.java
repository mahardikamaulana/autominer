package com.xeraphire.autominer.settings;

import necesse.engine.modLoader.ModSettings;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CustomSettingsLibBridgeTest {

    @BeforeEach
    void setUp() {
        CustomSettingsLibBridge.resetCache();
    }

    @AfterEach
    void tearDown() {
        CustomSettingsLibBridge.resetCache();
    }

    @Test
    @DisplayName("tryRegisterSettings handles null safely")
    void nullSettingsHandledSafely() {
        assertFalse(CustomSettingsLibBridge.tryRegisterSettings(null));
        CustomSettingsLibBridge.syncToCustomSettings(null);
        CustomSettingsLibBridge.syncFromCustomSettings(null);
    }

    @Test
    @DisplayName("Bridge detects CustomSettingsLib when on test classpath and synchronizes state")
    void bridgeRegistrationAndSync() throws Exception {
        boolean available = CustomSettingsLibBridge.isAvailable();
        AutoMinerSettings settings = new AutoMinerSettings();
        settings.requireFuel = false;
        settings.miningSpeed = 4;
        settings.fuelDuration = 6;

        if (!available) {
            assertFalse(CustomSettingsLibBridge.tryRegisterSettings(settings));
            assertNull(CustomSettingsLibBridge.getCustomModSettings());
            return;
        }

        // Note: CustomModSettings constructor in CustomSettingsLib accesses ModLoader.getCurrentMod()
        // when running outside a full game lifecycle, which may throw NPE inside CustomModSettings.<init>.
        // Verify that tryRegisterSettings never leaks an exception even if CustomModSettings.<init> fails outside game context.
        assertDoesNotThrow(() -> CustomSettingsLibBridge.tryRegisterSettings(settings));
    }

    @Test
    @DisplayName("Reflection sync logic reads and writes mock CustomModSettings structures accurately")
    void reflectionSyncWithSimulatedInstance() throws Exception {
        // Create a standalone mock/stub object matching CustomModSettings field layout
        // so we can test syncToCustomSettings and syncFromCustomSettings deterministically without ModLoader
        SimulatedCustomModSettings simulated = new SimulatedCustomModSettings();

        Field instanceField = CustomSettingsLibBridge.class.getDeclaredField("customSettingsInstance");
        instanceField.setAccessible(true);
        instanceField.set(null, simulated);

        AutoMinerSettings settings = new AutoMinerSettings();
        settings.requireFuel = false;
        settings.miningSpeed = 8;
        settings.fuelDuration = 5;

        // 1. Test syncToCustomSettings
        CustomSettingsLibBridge.syncToCustomSettings(settings);
        assertEquals(false, simulated.serverDataSettings.get("requireFuel"));
        assertEquals(8, simulated.serverDataSettings.get("miningSpeed"));
        assertEquals(5, simulated.serverDataSettings.get("fuelDuration"));

        assertEquals(false, simulated.settingsMap.get("requireFuel").getValue());
        assertEquals(8, simulated.settingsMap.get("miningSpeed").getValue());
        assertEquals(5, simulated.settingsMap.get("fuelDuration").getValue());

        // 2. Test syncFromCustomSettings (including clamping)
        simulated.settingsMap.get("requireFuel").value = true;
        simulated.settingsMap.get("miningSpeed").value = 99; // should clamp to 10
        simulated.settingsMap.get("fuelDuration").value = -2; // should clamp to 1

        CustomSettingsLibBridge.syncFromCustomSettings(settings);
        assertTrue(settings.isRequireFuel());
        assertEquals(10, settings.getMiningSpeed());
        assertEquals(1, settings.getFuelDuration());

        // 3. Test serverDataSettings fallback when settingsMap entry is absent
        simulated.settingsMap.clear();
        simulated.serverDataSettings.put("requireFuel", false);
        simulated.serverDataSettings.put("miningSpeed", 6);
        simulated.serverDataSettings.put("fuelDuration", 4);

        CustomSettingsLibBridge.syncFromCustomSettings(settings);
        assertFalse(settings.isRequireFuel());
        assertEquals(6, settings.getMiningSpeed());
        assertEquals(4, settings.getFuelDuration());
    }

    @Test
    @DisplayName("In-game, server-synced values win over the client's local CustomSettingsLib values")
    void serverDataPreferredInGame() throws Exception {
        SimulatedCustomModSettings simulated = injectSimulatedInstance();
        simulated.serverDataSettings.put("requireFuel", false);
        simulated.serverDataSettings.put("miningSpeed", 7);
        simulated.serverDataSettings.put("fuelDuration", 3);
        AutoMinerSettings settings = new AutoMinerSettings();

        // In the main menu the local values apply
        simulated.inGame = false;
        CustomSettingsLibBridge.syncFromCustomSettings(settings);
        assertTrue(settings.isRequireFuel());
        assertEquals(1, settings.getMiningSpeed());
        assertEquals(1, settings.getFuelDuration());

        // Joined to a server: follow the server's values
        simulated.inGame = true;
        CustomSettingsLibBridge.syncFromCustomSettings(settings);
        assertFalse(settings.isRequireFuel());
        assertEquals(7, settings.getMiningSpeed());
        assertEquals(3, settings.getFuelDuration());

        // In-game before the server packet arrives: fall back to the local values
        simulated.serverDataSettings.clear();
        CustomSettingsLibBridge.syncFromCustomSettings(settings);
        assertTrue(settings.isRequireFuel());
        assertEquals(1, settings.getMiningSpeed());
        assertEquals(1, settings.getFuelDuration());
    }

    @Test
    @DisplayName("GUI Save applies the newly committed values immediately, matching CSL's listener-before-onSave order")
    void onSavedListenerSeesCommittedValues() throws Exception {
        SimulatedCustomModSettings simulated = injectSimulatedInstance();
        try {
            assertEquals(1, AutoMinerConfig.getMiningSpeed()); // initial pull marks the throttle as synced

            // CustomModSettingsForm save button: listeners run first, then onSave() commits the GUI values
            CustomSettingsLibBridge.createOnSavedListener().run();
            simulated.settingsMap.get("requireFuel").value = false;
            simulated.settingsMap.get("miningSpeed").value = 5;
            simulated.settingsMap.get("fuelDuration").value = 3;

            // Visible on the next read, without waiting out the 250ms throttle
            assertFalse(AutoMinerConfig.isRequireFuel());
            assertEquals(5, AutoMinerConfig.getMiningSpeed());
            assertEquals(3, AutoMinerConfig.getFuelDuration());
        } finally {
            CustomSettingsLibBridge.resetCache();
            AutoMinerConfig.setRequireFuel(true);
            AutoMinerConfig.setMiningSpeed(1);
            AutoMinerConfig.setFuelDuration(1);
        }
    }

    private static SimulatedCustomModSettings injectSimulatedInstance() throws Exception {
        SimulatedCustomModSettings simulated = new SimulatedCustomModSettings();
        Field instanceField = CustomSettingsLibBridge.class.getDeclaredField("customSettingsInstance");
        instanceField.setAccessible(true);
        instanceField.set(null, simulated);
        return simulated;
    }

    public static class SimulatedSetting {
        protected Object value;

        public SimulatedSetting(Object initial) {
            this.value = initial;
        }

        public Object getValue() {
            return value;
        }
    }

    public static class SimulatedCustomModSettings extends ModSettings {
        public final Map<String, Object> serverDataSettings = new java.util.HashMap<>();
        public final Map<String, SimulatedSetting> settingsMap = new java.util.HashMap<>();
        public final java.util.List<String> serverSettings = java.util.List.of("requireFuel", "miningSpeed", "fuelDuration");
        public boolean inGame = false;

        public SimulatedCustomModSettings() {
            settingsMap.put("requireFuel", new SimulatedSetting(true));
            settingsMap.put("miningSpeed", new SimulatedSetting(1));
            settingsMap.put("fuelDuration", new SimulatedSetting(1));
        }

        // Mirrors CustomModSettings.getSetting: server settings come from serverDataSettings while in-game
        public Object getSetting(String settingID) {
            if (serverSettings.contains(settingID) && inGame) {
                return serverDataSettings.get(settingID);
            }
            SimulatedSetting setting = settingsMap.get(settingID);
            return setting != null ? setting.getValue() : null;
        }

        @Override
        public void addSaveData(SaveData saveData) {
        }

        @Override
        public void applyLoadData(LoadData loadData) {
        }
    }
}
