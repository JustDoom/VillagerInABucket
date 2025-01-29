package com.imjustdoom.villagerinabucket.config.fabric;

import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Path;

public class ConfigImpl {
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
}