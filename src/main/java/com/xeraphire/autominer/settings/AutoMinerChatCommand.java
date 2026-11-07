package com.xeraphire.autominer.settings;

import necesse.engine.commands.AutoComplete;
import necesse.engine.commands.ChatCommand;
import necesse.engine.commands.CommandLog;
import necesse.engine.commands.PermissionLevel;
import necesse.engine.network.client.Client;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;

import java.util.ArrayList;
import java.util.List;

public class AutoMinerChatCommand extends ChatCommand {

    public AutoMinerChatCommand() {
        super("autominer", PermissionLevel.USER);
    }

    @Override
    public String getUsage() {
        return "status | speed <1-10> | fueltime <1-10> | requirefuel <true/false>";
    }

    @Override
    public String getAction() {
        return "View or configure AutoMiner settings";
    }

    @Override
    public String getCurrentUsage(Client client, Server server, ServerClient serverClient, String[] args) {
        return getUsage();
    }

    @Override
    public List<AutoComplete> autocomplete(Client client, Server server, ServerClient serverClient, String[] args) {
        List<AutoComplete> list = new ArrayList<>();
        if (args.length <= 1) {
            String curr = args.length == 1 ? args[0].toLowerCase() : "";
            for (String sub : new String[]{"status", "speed", "fueltime", "fuelduration", "requirefuel", "fuel"}) {
                if (sub.startsWith(curr)) {
                    list.add(new AutoComplete(args.length, sub));
                }
            }
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            String curr = args[1].toLowerCase();
            if (sub.equals("requirefuel") || sub.equals("fuel")) {
                if ("true".startsWith(curr)) list.add(new AutoComplete(args.length, "true"));
                if ("false".startsWith(curr)) list.add(new AutoComplete(args.length, "false"));
            } else if (sub.equals("speed") || sub.equals("fueltime") || sub.equals("fuelduration")) {
                if ("1".startsWith(curr)) list.add(new AutoComplete(args.length, "1"));
                if ("2".startsWith(curr)) list.add(new AutoComplete(args.length, "2"));
                if ("3".startsWith(curr)) list.add(new AutoComplete(args.length, "3"));
                if ("5".startsWith(curr)) list.add(new AutoComplete(args.length, "5"));
                if ("10".startsWith(curr)) list.add(new AutoComplete(args.length, "10"));
            }
        }
        return list;
    }

    @Override
    public boolean run(Client client, Server server, ServerClient serverClient, ArrayList<String> args, CommandLog log) {
        if (args.isEmpty() || args.get(0).equalsIgnoreCase("status")) {
            boolean hasLib = AutoMinerConfig.isCustomSettingsLibDetected();
            log.add("=== AutoMiner Settings (" + (hasLib ? "CustomSettingsLib GUI" : "Native ModSettings") + ") ===");
            log.add("• Require Fuel: " + AutoMinerConfig.isRequireFuel());
            log.add("• Mining Speed Multiplier: " + AutoMinerConfig.getMiningSpeed() + "x");
            log.add("• Fuel Duration Multiplier: " + AutoMinerConfig.getFuelDuration() + "x");
            return true;
        }

        boolean isSingleplayer = server != null && server.isSingleplayer();
        boolean isAdmin = serverClient == null || serverClient.getPermissionLevel().getLevel() >= PermissionLevel.ADMIN.getLevel();
        if (!isSingleplayer && !isAdmin) {
            log.add("You do not have permission to modify AutoMiner settings.");
            return false;
        }

        String sub = args.get(0).toLowerCase();
        if (args.size() < 2) {
            log.add("Usage: /autominer " + getUsage());
            return false;
        }

        String val = args.get(1);
        switch (sub) {
            case "speed":
                try {
                    int speed = Integer.parseInt(val);
                    if (speed < 1 || speed > 10) {
                        log.add("Speed must be between 1 and 10.");
                        return false;
                    }
                    AutoMinerConfig.setMiningSpeed(speed);
                    CustomSettingsLibBridge.broadcastServerSettings(server);
                    log.add("Mining speed multiplier set to " + AutoMinerConfig.getMiningSpeed() + "x");
                    return true;
                } catch (NumberFormatException e) {
                    log.add("Invalid integer: " + val);
                    return false;
                }
            case "fueltime":
            case "fuelduration":
                try {
                    int fuel = Integer.parseInt(val);
                    if (fuel < 1 || fuel > 10) {
                        log.add("Fuel time must be between 1 and 10.");
                        return false;
                    }
                    AutoMinerConfig.setFuelDuration(fuel);
                    CustomSettingsLibBridge.broadcastServerSettings(server);
                    log.add("Fuel duration multiplier set to " + AutoMinerConfig.getFuelDuration() + "x");
                    return true;
                } catch (NumberFormatException e) {
                    log.add("Invalid integer: " + val);
                    return false;
                }
            case "requirefuel":
            case "fuel":
                Boolean req = parseStrictBoolean(val);
                if (req == null) {
                    log.add("Invalid boolean '" + val + "'. Expected true or false.");
                    return false;
                }
                AutoMinerConfig.setRequireFuel(req);
                CustomSettingsLibBridge.broadcastServerSettings(server);
                log.add("Require fuel set to: " + AutoMinerConfig.isRequireFuel());
                return true;
            default:
                log.add("Unknown subcommand: " + sub + ". Usage: /autominer " + getUsage());
                return false;
        }
    }

    private static Boolean parseStrictBoolean(String val) {
        if ("true".equalsIgnoreCase(val)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(val)) {
            return Boolean.FALSE;
        }
        return null;
    }
}
