package com.imjustdoom.villagerinabucket.platform;

import com.imjustdoom.villagerinabucket.platform.services.IPlatformHelper;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class ForgePlatformHelper implements IPlatformHelper {
    @Override public Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
