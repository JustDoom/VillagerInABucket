package com.imjustdoom.villagerinabucket.config;

import dev.architectury.injectables.annotations.ExpectPlatform;

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

    public static boolean ZOMBIE_VILLAGER;

    public static void init() throws IOException {
        PROPERTIES = new Properties();
        FILE_PATH = Path.of(getConfigDirectory() + "/villager-in-a-bucket.properties");
        if (!FILE_PATH.toFile().exists()) {
            new File(FILE_PATH.toString()).createNewFile();
        }
        PROPERTIES.load(new FileInputStream(FILE_PATH.toFile()));

        ZOMBIE_VILLAGER = getBoolean("enable-zombie-villager", "true");

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
                        """);
    }

    @ExpectPlatform
    public static Path getConfigDirectory() {
        throw new AssertionError();
    }
}