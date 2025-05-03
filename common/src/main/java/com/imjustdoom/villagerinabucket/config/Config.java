package com.imjustdoom.villagerinabucket.config;

import com.imjustdoom.villagerinabucket.platform.Services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class Config {
    private static Path FILE_PATH;
    private static Properties PROPERTIES;

    public static boolean ZOMBIE_VILLAGER = true;
    public static boolean HARM_REPUTATION = false;

    public static void init() throws IOException {
        PROPERTIES = new Properties();
        FILE_PATH = Path.of(Services.PLATFORM.getConfigPath() + "/villager-in-a-bucket.properties");
        if (!FILE_PATH.toFile().exists()) {
            new File(FILE_PATH.toString()).createNewFile();
        }
        PROPERTIES.load(new FileInputStream(FILE_PATH.toFile()));

        ZOMBIE_VILLAGER = getBoolean("enable-zombie-villager", String.valueOf(ZOMBIE_VILLAGER));
        HARM_REPUTATION = getBoolean("harm-reputation", String.valueOf(HARM_REPUTATION));

        save();
    }

    private static String getString(final String setting, final String defaultValue) {
        String value = PROPERTIES.getProperty(setting);
        if (value == null) {
            PROPERTIES.setProperty(setting, defaultValue);
            value = defaultValue;
        }
        return value;
    }

    private static int getInt(final String setting, final String defaultValue) {
        String value = PROPERTIES.getProperty(setting);
        if (value == null) {
            PROPERTIES.setProperty(setting, defaultValue);
            value = defaultValue;
        }
        return Integer.parseInt(value);
    }

    private static float getFloat(final String setting, final String defaultValue) {
        String value = PROPERTIES.getProperty(setting);
        if (value == null) {
            PROPERTIES.setProperty(setting, defaultValue);
            value = defaultValue;
        }
        return Float.parseFloat(value);
    }

    private static boolean getBoolean(final String setting, final String defaultValue) {
        String value = PROPERTIES.getProperty(setting);
        if (value == null) {
            PROPERTIES.setProperty(setting, defaultValue);
            value = defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    private static List<String> getStringList(final String setting, final String defaultValue) {
        String value = PROPERTIES.getProperty(setting);
        if (value == null) {
            PROPERTIES.setProperty(setting, defaultValue);
            value = defaultValue;
        }
        return Arrays.asList(value.split(","));
    }

    public static void save() throws IOException {
        PROPERTIES.store(new FileWriter(FILE_PATH.toFile()),
                """
                        Config for Villager In A Bucket
                        'enable-zombie-villager' should the ability to pick up Zombie Villagers in buckets be enabled. Default true
                        'harm-reputation' controls whether the reputation of the picked up villager should be harmed (Equal to punching it)
                        """);
    }
}
