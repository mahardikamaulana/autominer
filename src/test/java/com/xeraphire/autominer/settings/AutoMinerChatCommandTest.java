package com.xeraphire.autominer.settings;

import necesse.engine.commands.AutoComplete;
import necesse.engine.commands.CommandLog;
import necesse.engine.commands.PermissionLevel;
import necesse.engine.network.client.Client;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AutoMinerChatCommandTest {

    private AutoMinerChatCommand command;
    private Client client;
    private Server server;
    private ServerClient serverClient;
    private CommandLog log;

    @BeforeEach
    void setUp() {
        CustomSettingsLibBridge.resetCache();
        AutoMinerConfig.setRequireFuel(true);
        AutoMinerConfig.setMiningSpeed(1);
        AutoMinerConfig.setFuelDuration(1);

        command = new AutoMinerChatCommand();
        client = mock(Client.class);
        server = mock(Server.class);
        serverClient = mock(ServerClient.class);
        log = mock(CommandLog.class);

        when(serverClient.getPermissionLevel()).thenReturn(PermissionLevel.ADMIN);
        when(server.isSingleplayer()).thenReturn(false);
    }

    @AfterEach
    void tearDown() {
        AutoMinerConfig.setRequireFuel(true);
        AutoMinerConfig.setMiningSpeed(1);
        AutoMinerConfig.setFuelDuration(1);
    }

    private ArrayList<String> argsOf(String... items) {
        return new ArrayList<>(Arrays.asList(items));
    }

    @Test
    @DisplayName("Metadata methods return non-empty usage and action descriptions")
    void commandMetadata() {
        assertNotNull(command.getUsage());
        assertFalse(command.getUsage().isBlank());
        assertNotNull(command.getAction());
        assertFalse(command.getAction().isBlank());
        assertEquals(command.getUsage(), command.getCurrentUsage(client, server, serverClient, new String[0]));
    }

    @Test
    @DisplayName("Running with no args or 'status' prints current settings and returns true")
    void statusCommand() {
        when(serverClient.getPermissionLevel()).thenReturn(PermissionLevel.USER);

        assertTrue(command.run(client, server, serverClient, argsOf(), log));
        assertTrue(command.run(client, server, serverClient, argsOf("status"), log));
        assertTrue(command.run(client, server, serverClient, argsOf("STATUS"), log));

        verify(log, atLeast(10)).add(anyString());
    }

    @Test
    @DisplayName("Multiplayer non-admin USER is blocked from modifying settings")
    void permissionDeniedForMultiplayerUser() {
        when(server.isSingleplayer()).thenReturn(false);
        when(serverClient.getPermissionLevel()).thenReturn(PermissionLevel.USER);

        assertFalse(command.run(client, server, serverClient, argsOf("speed", "5"), log));
        assertEquals(1, AutoMinerConfig.getMiningSpeed());
        verify(log).add(contains("permission"));
    }

    @Test
    @DisplayName("Singleplayer USER (cheats off) is allowed to modify settings")
    void permissionAllowedForSingleplayerUser() {
        when(server.isSingleplayer()).thenReturn(true);
        when(serverClient.getPermissionLevel()).thenReturn(PermissionLevel.USER);

        assertTrue(command.run(client, server, serverClient, argsOf("speed", "5"), log));
        assertEquals(5, AutoMinerConfig.getMiningSpeed());
    }

    @Test
    @DisplayName("Server console (serverClient == null) is allowed to modify settings")
    void permissionAllowedForServerConsole() {
        assertTrue(command.run(client, server, null, argsOf("speed", "6"), log));
        assertEquals(6, AutoMinerConfig.getMiningSpeed());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 5, 10})
    @DisplayName("Valid speed values [1..10] succeed and update config")
    void validSpeedCommand(int speed) {
        assertTrue(command.run(client, server, serverClient, argsOf("speed", String.valueOf(speed)), log));
        assertEquals(speed, AutoMinerConfig.getMiningSpeed());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "11", "-1", "abc", ""})
    @DisplayName("Invalid speed values fail and do not change config")
    void invalidSpeedCommand(String invalidVal) {
        AutoMinerConfig.setMiningSpeed(3);
        assertFalse(command.run(client, server, serverClient, argsOf("speed", invalidVal), log));
        assertEquals(3, AutoMinerConfig.getMiningSpeed());
    }

    @ParameterizedTest
    @CsvSource({
            "fueltime, 4",
            "fuelduration, 9",
            "FUELTIME, 1",
            "FUELDURATION, 10"
    })
    @DisplayName("fueltime and fuelduration aliases accept valid values [1..10]")
    void validFuelDurationCommand(String alias, int duration) {
        assertTrue(command.run(client, server, serverClient, argsOf(alias, String.valueOf(duration)), log));
        assertEquals(duration, AutoMinerConfig.getFuelDuration());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "11", "-5", "xyz"})
    @DisplayName("Invalid fueltime values fail and leave config unchanged")
    void invalidFuelDurationCommand(String invalidVal) {
        AutoMinerConfig.setFuelDuration(2);
        assertFalse(command.run(client, server, serverClient, argsOf("fueltime", invalidVal), log));
        assertEquals(2, AutoMinerConfig.getFuelDuration());
    }

    @ParameterizedTest
    @CsvSource({
            "requirefuel, false, false",
            "requirefuel, true, true",
            "fuel, FALSE, false",
            "fuel, TRUE, true"
    })
    @DisplayName("requirefuel and fuel aliases accept strict true/false booleans")
    void validRequireFuelCommand(String alias, String input, boolean expected) {
        assertTrue(command.run(client, server, serverClient, argsOf(alias, input), log));
        assertEquals(expected, AutoMinerConfig.isRequireFuel());
    }

    @ParameterizedTest
    @ValueSource(strings = {"yes", "no", "1", "0", "tru", "fals"})
    @DisplayName("Invalid boolean strings for requirefuel are rejected")
    void invalidBooleansRejected(String invalidBool) {
        AutoMinerConfig.setRequireFuel(true);
        assertFalse(command.run(client, server, serverClient, argsOf("requirefuel", invalidBool), log));
        assertTrue(AutoMinerConfig.isRequireFuel());
    }

    @Test
    @DisplayName("Missing second argument or unknown subcommand fails and logs usage")
    void missingArgOrUnknownSubcommand() {
        assertFalse(command.run(client, server, serverClient, argsOf("speed"), log));
        assertFalse(command.run(client, server, serverClient, argsOf("unknownsub", "1"), log));
    }

    @Test
    @DisplayName("Autocomplete suggests matching subcommands on first argument and values on second argument")
    void autocompleteBehavior() {
        List<AutoComplete> rootSuggestions = command.autocomplete(client, server, serverClient, new String[]{""});
        assertEquals(6, rootSuggestions.size());

        List<AutoComplete> fuelSubSuggestions = command.autocomplete(client, server, serverClient, new String[]{"fuel"});
        assertEquals(3, fuelSubSuggestions.size()); // fueltime, fuelduration, fuel

        List<AutoComplete> boolSuggestions = command.autocomplete(client, server, serverClient, new String[]{"fuel", "t"});
        assertEquals(1, boolSuggestions.size());

        List<AutoComplete> numSuggestions = command.autocomplete(client, server, serverClient, new String[]{"speed", "1"});
        assertEquals(2, numSuggestions.size()); // "1" and "10"

        List<AutoComplete> extraArgs = command.autocomplete(client, server, serverClient, new String[]{"speed", "1", "extra"});
        assertTrue(extraArgs.isEmpty());
    }
}

