package com.imjustdoom.villagerinabucket.platform;

import com.imjustdoom.villagerinabucket.platform.services.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class FabricPlatformHelper implements IPlatformHelper {
    @Override public Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
