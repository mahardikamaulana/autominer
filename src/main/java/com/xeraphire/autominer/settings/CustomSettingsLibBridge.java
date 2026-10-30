package com.xeraphire.autominer.settings;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Isolated dynamic reflection bridge for CustomSettingsLib (Mod Settings Lib).
 * Ensures zero hard compile-time or runtime dependencies on customsettingslib,
 * preventing NoClassDefFoundError and ClassNotFoundException when CustomSettingsLib
 * is not installed or enabled.
 */
public class CustomSettingsLibBridge {

    private static final String CUSTOM_SETTINGS_CLASS = "customsettingslib.settings.CustomModSettings";
    private static volatile Boolean isAvailableCache = null;
    private static final Object LOCK = new Object();
    private static volatile Object customSettingsInstance = null;

    // Cached reflection handles to avoid hot-path lookup overhead
    private static volatile Class<?> cachedInstanceClass = null;
    private static volatile Field cachedSettingsMapField = null;
    private static volatile Field cachedServerDataField = null;
    private static final ConcurrentHashMap<Class<?>, Method> GET_VALUE_METHODS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<?>, Field> VALUE_FIELDS = new ConcurrentHashMap<>();
    private static volatile boolean loggedSyncError = false;

    /**
     * Check if CustomSettingsLib is present on the runtime classpath.
     * Caches the result to avoid repeated classloader lookups.
     *
     * @return true if CustomSettingsLib is present and loaded, false otherwise.
     */
    public static boolean isAvailable() {
        if (isAvailableCache != null) {
            return isAvailableCache;
        }
        synchronized (LOCK) {
            if (isAvailableCache != null) {
                return isAvailableCache;
            }
            try {
                Class.forName(CUSTOM_SETTINGS_CLASS, false, CustomSettingsLibBridge.class.getClassLoader());
                isAvailableCache = true;
                System.out.println("[AutoMiner] CustomSettingsLib detected. In-game settings GUI enabled.");
            } catch (Exception | LinkageError t) {
                isAvailableCache = false;
                System.out.println("[AutoMiner] CustomSettingsLib not detected. Using native ModSettings & Chat Command.");
            }
            return isAvailableCache;
        }
    }

    /**
     * Attempts to dynamically register settings with CustomSettingsLib if available.
     * Completely safe to call regardless of whether CustomSettingsLib is installed.
     *
     * @param settings The native AutoMinerSettings instance
     * @return true if successfully registered with CustomSettingsLib, false otherwise
     */
    public static synchronized boolean tryRegisterSettings(AutoMinerSettings settings) {
        if (settings == null) {
            return false;
        }
        if (customSettingsInstance != null) {
            return true;
        }
        if (!isAvailable()) {
            return false;
        }

        try {
            Class<?> customModSettingsClass = Class.forName(CUSTOM_SETTINGS_CLASS, false, CustomSettingsLibBridge.class.getClassLoader());

            // Callback when player hits Save in CustomSettingsLib GUI
            Runnable onSavedListener = () -> {
                try {
                    syncFromCustomSettings(settings);
                    syncToCustomSettings(settings);
                    settings.save();
                } catch (Exception | LinkageError t) {
                    System.err.println("[AutoMiner] Error reading settings from CustomSettingsLib GUI: " + t.getMessage());
                }
            };

            Constructor<?> constructor = customModSettingsClass.getConstructor(Runnable.class);
            customSettingsInstance = constructor.newInstance(onSavedListener);

            // addServerSettings(String... serverSettingsIDs)
            Method addServerSettingsMethod = customModSettingsClass.getMethod("addServerSettings", String[].class);
            addServerSettingsMethod.invoke(customSettingsInstance, (Object) new String[]{"requireFuel", "miningSpeed", "fuelDuration"});

            // addTextSeparator("generalsection")
            Method addTextSeparatorMethod = customModSettingsClass.getMethod("addTextSeparator", String.class);
            addTextSeparatorMethod.invoke(customSettingsInstance, "generalsection");

            // addBooleanSetting("requireFuel", defaultValue)
            Method addBooleanSettingMethod = customModSettingsClass.getMethod("addBooleanSetting", String.class, boolean.class);
            addBooleanSettingMethod.invoke(customSettingsInstance, "requireFuel", settings.isRequireFuel());

            // addIntSetting("miningSpeed", defaultValue, min, max, displayMode)
            Class<?> displayModeClass = Class.forName("customsettingslib.components.settings.IntSetting$DisplayMode", false, CustomSettingsLibBridge.class.getClassLoader());
            Object inputMode = displayModeClass.getField("INPUT").get(null);
            Method addIntSettingMethod = customModSettingsClass.getMethod("addIntSetting", String.class, int.class, int.class, int.class, displayModeClass);

            addIntSettingMethod.invoke(customSettingsInstance, "miningSpeed", settings.getMiningSpeed(), 1, 10, inputMode);
            addIntSettingMethod.invoke(customSettingsInstance, "fuelDuration", settings.getFuelDuration(), 1, 10, inputMode);

            syncToCustomSettings(settings);
            return true;
        } catch (Exception | LinkageError t) {
            System.err.println("[AutoMiner] Failed to register settings with CustomSettingsLib: " + t.getMessage());
            customSettingsInstance = null;
            return false;
        }
    }

    /**
     * Synchronizes current CustomModSettings values back into native AutoMinerSettings fields.
     *
     * @param settings The native AutoMinerSettings instance
     */
    public static void syncFromCustomSettings(AutoMinerSettings settings) {
        Object instance = customSettingsInstance;
        if (instance == null || settings == null) {
            return;
        }
        try {
            Map<String, Object> settingsMap = getSettingsMap(instance);
            Map<String, Object> serverData = getServerDataMap(instance);

            if (settingsMap != null || serverData != null) {
                Object rf = readSettingValue(settingsMap, serverData, "requireFuel");
                if (rf instanceof Boolean) {
                    settings.requireFuel = (Boolean) rf;
                }

                Object ms = readSettingValue(settingsMap, serverData, "miningSpeed");
                if (ms instanceof Integer) {
                    settings.miningSpeed = Math.max(1, Math.min(10, (Integer) ms));
                }

                Object fd = readSettingValue(settingsMap, serverData, "fuelDuration");
                if (fd instanceof Integer) {
                    settings.fuelDuration = Math.max(1, Math.min(10, (Integer) fd));
                }
            }
            AutoMinerConfig.markSyncedNow();
        } catch (Exception | LinkageError e) {
            logSyncErrorOnce("syncFromCustomSettings", e);
        }
    }

    /**
     * Returns the CustomModSettings instance cast as ModSettings if registered, or null.
     */
    public static necesse.engine.modLoader.ModSettings getCustomModSettings() {
        if (customSettingsInstance instanceof necesse.engine.modLoader.ModSettings) {
            return (necesse.engine.modLoader.ModSettings) customSettingsInstance;
        }
        return null;
    }

    /**
     * Synchronizes current native AutoMinerSettings values into CustomModSettings fields.
     *
     * @param settings The native AutoMinerSettings instance
     */
    public static void syncToCustomSettings(AutoMinerSettings settings) {
        Object instance = customSettingsInstance;
        if (instance == null || settings == null) {
            return;
        }
        try {
            Map<String, Object> settingsMap = getSettingsMap(instance);
            Map<String, Object> serverData = getServerDataMap(instance);

            writeSettingValue(settingsMap, serverData, "requireFuel", settings.isRequireFuel());
            writeSettingValue(settingsMap, serverData, "miningSpeed", settings.getMiningSpeed());
            writeSettingValue(settingsMap, serverData, "fuelDuration", settings.getFuelDuration());
            AutoMinerConfig.markSyncedNow();
        } catch (Exception | LinkageError e) {
            logSyncErrorOnce("syncToCustomSettings", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getSettingsMap(Object instance) throws ReflectiveOperationException {
        Class<?> cls = instance.getClass();
        Field f = cachedSettingsMapField;
        if (f == null || cachedInstanceClass != cls) {
            f = cls.getField("settingsMap");
            cachedSettingsMapField = f;
            cachedInstanceClass = cls;
        }
        return (Map<String, Object>) f.get(instance);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getServerDataMap(Object instance) throws ReflectiveOperationException {
        Class<?> cls = instance.getClass();
        Field f = cachedServerDataField;
        if (f == null || !f.getDeclaringClass().isAssignableFrom(cls)) {
            f = cls.getField("serverDataSettings");
            cachedServerDataField = f;
        }
        return (Map<String, Object>) f.get(instance);
    }

    private static Object readSettingValue(Map<String, Object> settingsMap, Map<String, Object> serverData, String key)
            throws ReflectiveOperationException {
        if (settingsMap != null) {
            Object settingObj = settingsMap.get(key);
            if (settingObj != null) {
                Class<?> sCls = settingObj.getClass();
                Method getVal = GET_VALUE_METHODS.get(sCls);
                if (getVal == null) {
                    getVal = sCls.getMethod("getValue");
                    GET_VALUE_METHODS.put(sCls, getVal);
                }
                Object val = getVal.invoke(settingObj);
                if (val != null) {
                    return val;
                }
            }
        }
        if (serverData != null) {
            return serverData.get(key);
        }
        return null;
    }

    private static void writeSettingValue(Map<String, Object> settingsMap, Map<String, Object> serverData, String settingID, Object value)
            throws ReflectiveOperationException {
        if (serverData != null) {
            serverData.put(settingID, value);
        }
        if (settingsMap != null) {
            Object settingObj = settingsMap.get(settingID);
            if (settingObj != null) {
                Class<?> sCls = settingObj.getClass();
                Field valueField = VALUE_FIELDS.get(sCls);
                if (valueField == null) {
                    Class<?> curr = sCls;
                    while (curr != null && curr != Object.class) {
                        try {
                            valueField = curr.getDeclaredField("value");
                            valueField.setAccessible(true);
                            VALUE_FIELDS.put(sCls, valueField);
                            break;
                        } catch (NoSuchFieldException e) {
                            curr = curr.getSuperclass();
                        }
                    }
                }
                if (valueField != null) {
                    valueField.set(settingObj, value);
                }
            }
        }
    }

    private static void logSyncErrorOnce(String context, Throwable e) {
        if (!loggedSyncError) {
            loggedSyncError = true;
            System.err.println("[AutoMiner] Reflection error in " + context + ": " + e.getMessage());
        }
    }

    /**
     * Resets cached availability and instance (useful for testing).
     */
    public static void resetCache() {
        synchronized (LOCK) {
            isAvailableCache = null;
            customSettingsInstance = null;
            cachedInstanceClass = null;
            cachedSettingsMapField = null;
            cachedServerDataField = null;
            GET_VALUE_METHODS.clear();
            VALUE_FIELDS.clear();
            loggedSyncError = false;
        }
        AutoMinerConfig.invalidateSyncTimer();
    }
}
