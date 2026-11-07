package com.xeraphire.autominer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class AutoMinerDefinitionTest {

    static Stream<AutoMinerDefinition> definitionProvider() {
        return Arrays.stream(AutoMinerDefinition.DEFINITIONS);
    }

    @Test
    @DisplayName("DEFINITIONS array should not be empty and have expected count")
    void definitionsArrayNotEmpty() {
        assertNotNull(AutoMinerDefinition.DEFINITIONS);
        assertEquals(75, AutoMinerDefinition.DEFINITIONS.length, "Expected 75 AutoMiner definitions");
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("definitionProvider")
    @DisplayName("Log miners are fuel-ratio locked, cost only saplings, and never beat planting per sapling")
    void woodMinersBalanced(AutoMinerDefinition def) {
        boolean producesLog = def.craftedItem.endsWith("log");
        assertEquals(producesLog, def.isFuelRatioLocked(), "Only log miners may be fuel-ratio locked: " + def.stringID);
        if (!producesLog) {
            return;
        }
        assertEquals(AutoMinerDefinition.WOOD_LOG_RATIO, def.fuelLogRatio);
        assertEquals(def.craftedItem.replace("log", "sapling"), def.ingredientItem, "Wood miner must cost its own sapling: " + def.stringID);
        assertEquals(1, def.craftedAmount);

        // Vanilla planting yield per sapling: 4.5 logs per ~37.5 min, Deadwood ~3 logs per ~52.5 min
        double plantingLogsPerMinPerSapling = "deadwoodlog".equals(def.craftedItem) ? 3.0 / 52.5 : 4.5 / 37.5;
        double minerNetLogsPerMin = def.fuelLogRatio - 1;
        assertTrue(minerNetLogsPerMin > 0, "Wood miner must net positive wood: " + def.stringID);
        assertTrue(minerNetLogsPerMin / def.ingredientAmount <= plantingLogsPerMinPerSapling,
                "Wood miner out-produces planting the same saplings: " + def.stringID);
    }

    @Test
    @DisplayName("All stringIDs and techIDs must be globally unique")
    void uniqueStringIdsAndTechIds() {
        Set<String> stringIds = new HashSet<>();
        Set<String> techIds = new HashSet<>();

        for (AutoMinerDefinition def : AutoMinerDefinition.DEFINITIONS) {
            assertTrue(stringIds.add(def.stringID), "Duplicate stringID found: " + def.stringID);
            assertTrue(techIds.add(def.techID), "Duplicate techID found: " + def.techID);
        }
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("definitionProvider")
    @DisplayName("Each definition has valid non-blank parameters and positive quantities")
    void validDefinitionFields(AutoMinerDefinition def) {
        assertNotNull(def.stringID);
        assertFalse(def.stringID.isBlank(), "stringID must not be blank");
        assertEquals(def.stringID.toLowerCase(), def.stringID, "stringID must be lowercase: " + def.stringID);

        assertNotNull(def.texturePath);
        assertTrue(def.texturePath.startsWith("objects/"), "texturePath should start with objects/: " + def.texturePath);

        assertNotNull(def.techID);
        assertFalse(def.techID.isBlank(), "techID must not be blank");

        assertNotNull(def.craftedItem);
        assertFalse(def.craftedItem.isBlank(), "craftedItem must not be blank");
        assertTrue(def.craftedAmount > 0, "craftedAmount must be > 0 for " + def.stringID);

        assertNotNull(def.craftStation, "craftStation supplier must not be null for " + def.stringID);

        assertNotNull(def.ingredientItem);
        assertFalse(def.ingredientItem.isBlank(), "ingredientItem must not be blank");
        assertTrue(def.ingredientAmount > 0, "ingredientAmount must be > 0 for " + def.stringID);

        assertTrue(def.processTimeMs >= 1000, "processTimeMs must be >= 1000ms for " + def.stringID);
    }

    @Test
    @DisplayName("Default 9-arg constructor sets processTimeMs to 8000, and 10-arg allows an explicit process time")
    void convenienceConstructorDefaults() {
        AutoMinerDefinition def = new AutoMinerDefinition(
                "testminer", "objects/StoneAM", "TESTTECH", "stone", 1,
                () -> null, "stone", 10, null
        );
        assertEquals(8000, def.processTimeMs);

        AutoMinerDefinition explicitTime = new AutoMinerDefinition(
                "customminer", "objects/StoneAM", "CUSTOMTECH", "stone", 1,
                () -> null, "stone", 10, null, 12000
        );
        assertEquals(12000, explicitTime.processTimeMs);
    }

    @Test
    @DisplayName("Every showAfter target must be either 'forge' or a previously registered definition")
    void validShowAfterProgressionChain() {
        Set<String> seenIds = new HashSet<>();
        seenIds.add("forge");

        for (AutoMinerDefinition def : AutoMinerDefinition.DEFINITIONS) {
            if (def.showAfter != null) {
                assertTrue(
                        seenIds.contains(def.showAfter),
                        "Definition '" + def.stringID + "' references unknown or later showAfter '" + def.showAfter + "'"
                );
            }
            seenIds.add(def.stringID);
        }
    }

    @ParameterizedTest(name = "Textures for {0}")
    @MethodSource("definitionProvider")
    @DisplayName("Every definition has both an object texture PNG and an item texture PNG")
    void textureFilesExist(AutoMinerDefinition def) {
        File objectTexture = new File("src/main/resources/" + def.texturePath + ".png");
        assertTrue(objectTexture.isFile(), "Missing object texture file: " + objectTexture.getPath() + " for " + def.stringID);
        assertTrue(objectTexture.length() > 0, "Empty object texture file: " + objectTexture.getPath());

        // Overlay miners build their item icon at runtime from the base body plus the game's own item icon
        if (def.getIconOverlayTexture() != null) {
            assertEquals("items/" + def.craftedItem, def.getIconOverlayTexture());
            return;
        }

        File itemTexture = new File("src/main/resources/items/" + def.stringID + ".png");
        assertTrue(itemTexture.isFile(), "Missing item texture file: " + itemTexture.getPath() + " for " + def.stringID);
        assertTrue(itemTexture.length() > 0, "Empty item texture file: " + itemTexture.getPath());
    }

    @ParameterizedTest(name = "Locale {0}.lang")
    @ValueSource(strings = {"en", "de", "es", "kr"})
    @DisplayName("Every locale file contains translations for all definitions, tooltips, and settings UI")
    void localizationCompleteness(String locale) throws IOException {
        File langFile = new File("src/main/resources/locale/" + locale + ".lang");
        assertTrue(langFile.isFile(), "Missing locale file: " + langFile.getPath());

        List<String> lines = Files.readAllLines(langFile.toPath(), StandardCharsets.UTF_8);
        Map<String, Map<String, String>> sections = new HashMap<>();
        String currentSection = "";

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
                continue;
            }
            if (line.startsWith("[") && line.endsWith("]")) {
                currentSection = line.substring(1, line.length() - 1).trim();
                sections.putIfAbsent(currentSection, new HashMap<>());
            } else {
                int eq = line.indexOf('=');
                if (eq > 0) {
                    String key = line.substring(0, eq).trim();
                    String val = line.substring(eq + 1).trim();
                    sections.computeIfAbsent(currentSection, k -> new HashMap<>()).put(key, val);
                }
            }
        }

        Map<String, String> objectSection = sections.get("object");
        assertNotNull(objectSection, "Missing [object] section in " + locale + ".lang");

        for (AutoMinerDefinition def : AutoMinerDefinition.DEFINITIONS) {
            String translated = objectSection.get(def.stringID);
            assertNotNull(translated, "Missing [object] translation for '" + def.stringID + "' in " + locale + ".lang");
            assertFalse(translated.isBlank(), "Blank [object] translation for '" + def.stringID + "' in " + locale + ".lang");
        }

        Map<String, String> tooltipSection = sections.get("itemtooltip");
        assertNotNull(tooltipSection, "Missing [itemtooltip] section in " + locale + ".lang");
        assertNotNull(tooltipSection.get("autotip"), "Missing itemtooltip.autotip in " + locale + ".lang");
        assertFalse(tooltipSection.get("autotip").isBlank());

        Map<String, String> settingsSection = sections.get("settingsui");
        assertNotNull(settingsSection, "Missing [settingsui] section in " + locale + ".lang");
        for (String key : List.of("generalsection", "requireFuel", "miningSpeed", "fuelDuration")) {
            String val = settingsSection.get(key);
            assertNotNull(val, "Missing [settingsui] key '" + key + "' in " + locale + ".lang");
            assertFalse(val.isBlank(), "Blank [settingsui] key '" + key + "' in " + locale + ".lang");
        }
    }
}

