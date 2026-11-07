package com.xeraphire.autominer.settings;

import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class AutoMinerSettingsTest {

    private AutoMinerSettings settings;

    @BeforeEach
    void setUp() {
        CustomSettingsLibBridge.resetCache();
        settings = new AutoMinerSettings();
    }

    @Test
    @DisplayName("Default settings match expected initial state")
    void defaultValues() {
        assertTrue(settings.isRequireFuel());
        assertEquals(1, settings.getMiningSpeed());
        assertEquals(1, settings.getFuelDuration());
    }

    @ParameterizedTest(name = "input={0} -> expected={1}")
    @CsvSource({
            "-10, 1",
            "0, 1",
            "1, 1",
            "5, 5",
            "10, 10",
            "11, 10",
            "999, 10"
    })
    @DisplayName("setMiningSpeed and setFuelDuration clamp values to [1, 10]")
    void boundaryClamping(int input, int expected) {
        settings.setMiningSpeed(input);
        assertEquals(expected, settings.getMiningSpeed());

        settings.setFuelDuration(input);
        assertEquals(expected, settings.getFuelDuration());
    }

    @Test
    @DisplayName("Boolean setters update state accurately")
    void booleanSetters() {
        settings.setRequireFuel(false);
        assertFalse(settings.isRequireFuel());
        settings.setRequireFuel(true);
        assertTrue(settings.isRequireFuel());

    }

    @Test
    @DisplayName("SaveData and LoadData round-trip preserves all settings")
    void saveAndLoadRoundTrip() {
        settings.requireFuel = false;
        settings.miningSpeed = 7;
        settings.fuelDuration = 4;

        SaveData save = new SaveData("SETTINGS");
        settings.addSaveData(save);

        LoadData load = save.toLoadData();
        AutoMinerSettings restored = new AutoMinerSettings();
        restored.applyLoadData(load);

        assertFalse(restored.isRequireFuel());
        assertEquals(7, restored.getMiningSpeed());
        assertEquals(4, restored.getFuelDuration());
    }

    @Test
    @DisplayName("applyLoadData clamps out-of-range values from edited config files")
    void applyLoadDataClampsOutOfBounds() {
        SaveData save = new SaveData("SETTINGS");
        save.addBoolean("requireFuel", false);
        save.addInt("miningSpeed", 50);
        save.addInt("fuelDuration", -3);

        AutoMinerSettings restored = new AutoMinerSettings();
        restored.applyLoadData(save.toLoadData());

        assertFalse(restored.isRequireFuel());
        assertEquals(10, restored.getMiningSpeed());
        assertEquals(1, restored.getFuelDuration());
    }

    @Test
    @DisplayName("applyLoadData falls back to safe defaults when keys are missing")
    void applyLoadDataEmptyData() {
        SaveData emptySave = new SaveData("SETTINGS");
        AutoMinerSettings restored = new AutoMinerSettings();
        restored.applyLoadData(emptySave.toLoadData());

        assertTrue(restored.isRequireFuel());
        assertEquals(1, restored.getMiningSpeed());
        assertEquals(1, restored.getFuelDuration());
    }

    @Test
    @DisplayName("AutoMinerConfig static facade delegates and clamps properly")
    void autoMinerConfigFacade() {
        AutoMinerConfig.setRequireFuel(false);
        AutoMinerConfig.setMiningSpeed(8);
        AutoMinerConfig.setFuelDuration(3);

        assertFalse(AutoMinerConfig.isRequireFuel());
        assertEquals(8, AutoMinerConfig.getMiningSpeed());
        assertEquals(3, AutoMinerConfig.getFuelDuration());

        // Restore defaults
        AutoMinerConfig.setRequireFuel(true);
        AutoMinerConfig.setMiningSpeed(1);
        AutoMinerConfig.setFuelDuration(1);
    }
}

