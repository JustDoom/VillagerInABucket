package com.imjustdoom.villagerinabucket.config.neoforge;

import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class ConfigImpl {

    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
