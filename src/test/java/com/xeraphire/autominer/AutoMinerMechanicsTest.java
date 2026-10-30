package com.xeraphire.autominer;

import com.xeraphire.autominer.settings.AutoMinerConfig;
import com.xeraphire.autominer.settings.CustomSettingsLibBridge;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.Item;
import necesse.inventory.recipe.Recipe;
import necesse.inventory.recipe.Tech;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AutoMinerMechanicsTest {

    private static final Unsafe UNSAFE;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (Unsafe) f.get(null);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @BeforeEach
    void setUp() {
        CustomSettingsLibBridge.resetCache();
        AutoMinerConfig.setRequireFuel(true);
        AutoMinerConfig.setMiningSpeed(1);
        AutoMinerConfig.setFuelDuration(1);
        AutoMinerConfig.setEnableEndgameMiners(true);
    }

    @AfterEach
    void tearDown() {
        AutoMinerConfig.setRequireFuel(true);
        AutoMinerConfig.setMiningSpeed(1);
        AutoMinerConfig.setFuelDuration(1);
        AutoMinerConfig.setEnableEndgameMiners(true);
    }

    private AutoMinerObjectEntity allocateEntity(AutoMinerDefinition def) throws Exception {
        AutoMinerObjectEntity entity = (AutoMinerObjectEntity) UNSAFE.allocateInstance(AutoMinerObjectEntity.class);
        Field defField = AutoMinerObjectEntity.class.getDeclaredField("definition");
        defField.setAccessible(true);
        defField.set(entity, def);
        return entity;
    }

    private AutoMinerObject allocateObject(AutoMinerDefinition def, Recipe prodRecipe) throws Exception {
        AutoMinerObject obj = (AutoMinerObject) UNSAFE.allocateInstance(AutoMinerObject.class);
        Field defField = AutoMinerObject.class.getDeclaredField("definition");
        defField.setAccessible(true);
        defField.set(obj, def);
        obj.productionRecipe = prodRecipe;
        return obj;
    }

    @ParameterizedTest(name = "base={0}ms, speed={1}x -> expected={2}ms")
    @CsvSource({
            "4000, 1, 4000",
            "4000, 2, 2000",
            "4000, 10, 400",
            "8000, 1, 8000",
            "8000, 4, 2000",
            "8000, 10, 800",
            "12000, 3, 4000",
            "16000, 8, 2000",
            "24000, 6, 4000",
            "30000, 10, 3000",
            "100, 10, 50" // minimum floor of 50ms
    })
    @DisplayName("getProcessTime scales inversely with miningSpeed and respects 50ms minimum")
    void processTimeCalculation(int baseTimeMs, int speed, int expectedMs) throws Exception {
        AutoMinerDefinition def = new AutoMinerDefinition(
                "test", "objects/StoneAM", "TECH", "stone", 1, () -> null, "stone", 10, null, baseTimeMs
        );
        AutoMinerObjectEntity entity = allocateEntity(def);
        AutoMinerConfig.setMiningSpeed(speed);

        assertEquals(expectedMs, entity.getProcessTime( null));
    }

    @Test
    @DisplayName("getProcessTime falls back to defaultRecipeProcessTime (8000ms) when definition is null")
    void processTimeFallbackWhenDefinitionNull() throws Exception {
        AutoMinerObjectEntity entity = allocateEntity(null);
        AutoMinerConfig.setMiningSpeed(2);
        assertEquals(4000, entity.getProcessTime(null));
    }

    private InventoryItem allocateInventoryItem(String itemStringID) throws Exception {
        Item item = mock(Item.class);
        when(item.getStringID()).thenReturn(itemStringID);
        InventoryItem invItem = (InventoryItem) UNSAFE.allocateInstance(InventoryItem.class);
        Field itemField = InventoryItem.class.getDeclaredField("item");
        itemField.setAccessible(true);
        itemField.set(invItem, item);
        return invItem;
    }

    @ParameterizedTest(name = "durationMultiplier={0}x -> log={1}ms, fuelskull={2}ms")
    @CsvSource({
            "1, 60000, 300000",
            "2, 120000, 600000",
            "5, 300000, 1500000",
            "10, 600000, 3000000"
    })
    @DisplayName("getFuelTime scales with fuelDuration and gives 5x burn time for fuelskull")
    void fuelTimeCalculation(int multiplier, int expectedLogTime, int expectedSkullTime) throws Exception {
        AutoMinerObjectEntity entity = allocateEntity(AutoMinerDefinition.DEFINITIONS[0]);
        AutoMinerConfig.setFuelDuration(multiplier);

        // Null item or regular log
        assertEquals(expectedLogTime, entity.getFuelTime(null));

        InventoryItem logInvItem = allocateInventoryItem("oaklog");
        assertEquals(expectedLogTime, entity.getFuelTime(logInvItem));

        // Fuel skull item (5x)
        InventoryItem skullInvItem = allocateInventoryItem("fuelskull");
        assertEquals(expectedSkullTime, entity.getFuelTime(skullInvItem));
        assertTrue(entity.isValidFuelItem(skullInvItem));
    }

    @Test
    @DisplayName("When requireFuel is false, getNextFuelBurnTime returns full fuel time without consuming")
    void freeFuelWhenRequireFuelFalse() throws Exception {
        AutoMinerObjectEntity entity = allocateEntity(AutoMinerDefinition.DEFINITIONS[0]);
        AutoMinerConfig.setRequireFuel(false);
        AutoMinerConfig.setFuelDuration(2);

        assertEquals(120000, entity.getNextFuelBurnTime(false));
        assertEquals(120000, entity.getNextFuelBurnTime(true));
    }

    @Test
    @DisplayName("Endgame miners are always available and active")
    void endgameMinerToggleBehavior() throws Exception {
        // Tier 1 miner (stone, 4000ms, isEndgame = false)
        AutoMinerDefinition tier1Def = AutoMinerDefinition.DEFINITIONS[0];
        assertFalse(tier1Def.isEndgame);

        // Tier 4 miner (ascendedshard, 30000ms, isEndgame = true)
        AutoMinerDefinition tier4Def = AutoMinerDefinition.DEFINITIONS[AutoMinerDefinition.DEFINITIONS.length - 1];
        assertTrue(tier4Def.isEndgame);

        AutoMinerObjectEntity tier1Entity = allocateEntity(tier1Def);
        AutoMinerObjectEntity tier4Entity = allocateEntity(tier4Def);

        Recipe dummyRecipe = mock(Recipe.class);
        AutoMinerObject tier1Obj = allocateObject(tier1Def, dummyRecipe);
        AutoMinerObject tier4Obj = allocateObject(tier4Def, dummyRecipe);

        AutoMinerConfig.setRequireFuel(false);
        assertFalse(tier1Entity.isEndgameDisabled());
        assertFalse(tier4Entity.isEndgameDisabled());
        assertEquals(60000, tier4Entity.getNextFuelBurnTime(false));
        assertEquals(1, tier4Obj.streamSettlementRecipes(null, 0, 0).count());
        assertTrue(tier4Entity.canUseFuel());

        // Calling setEnableEndgameMiners(false) does not disable endgame miners
        AutoMinerConfig.setEnableEndgameMiners(false);
        assertTrue(AutoMinerConfig.isEnableEndgameMiners());
        assertFalse(tier4Entity.isEndgameDisabled());
        assertEquals(60000, tier4Entity.getNextFuelBurnTime(false));
        assertEquals(1, tier4Obj.streamSettlementRecipes(null, 0, 0).count());
        assertEquals(1, tier1Obj.streamSettlementRecipes(null, 0, 0).count());
    }

    @Test
    @DisplayName("Toggling requireFuel from false to true cuts off active virtual free fuel immediately")
    void freeFuelCutoffWhenRequireFuelReEnabled() throws Exception {
        AutoMinerObjectEntity entity = allocateEntity(AutoMinerDefinition.DEFINITIONS[0]);

        AutoMinerConfig.setRequireFuel(false);
        assertTrue(entity.canUseFuel());
        assertEquals(60000, entity.getNextFuelBurnTime(true));
        assertTrue(entity.isBurningFreeFuel());

        // Re-enable fuel requirement mid-burn -> isFuelRunning must immediately return false
        AutoMinerConfig.setRequireFuel(true);
        assertFalse(entity.isFuelRunning());
    }

    @Test
    @DisplayName("getFuelRequestOptions respects requireFuel setting")
    void settlementFuelRequestOptions() throws Exception {
        AutoMinerObject obj = allocateObject(AutoMinerDefinition.DEFINITIONS[0], null);

        AutoMinerConfig.setRequireFuel(true);
        assertNotNull(obj.getFuelRequestOptions(null, 0, 0));

        AutoMinerConfig.setRequireFuel(false);
        assertNull(obj.getFuelRequestOptions(null, 0, 0));
    }
}
