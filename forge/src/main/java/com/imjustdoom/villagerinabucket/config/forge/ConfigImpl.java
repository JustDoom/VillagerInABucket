package com.imjustdoom.villagerinabucket.config.forge;

import net.minecraftforge.fml.loading.FMLPaths;
import java.nio.file.Path;

public class ConfigImpl {
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}