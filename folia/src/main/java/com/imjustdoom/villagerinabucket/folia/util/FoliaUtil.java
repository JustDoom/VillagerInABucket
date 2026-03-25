package com.imjustdoom.villagerinabucket.folia.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public final class FoliaUtil {

    private static boolean folia = false;

    private FoliaUtil() {}

    public static void init() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException e) {
            folia = false;
        }
    }

    public static boolean isFolia() {
        return folia;
    }

    public static void runAtEntity(Plugin plugin, Entity entity, Runnable task) {
        if (folia) {
            entity.getScheduler().run(plugin, scheduledTask -> task.run(), null);
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    public static void runAtLocation(Plugin plugin, Location location, Runnable task) {
        if (folia) {
            Bukkit.getRegionScheduler().run(plugin, location, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    public static void runAtEntityDelayed(Plugin plugin, Entity entity, Runnable task, long delayTicks) {
        if (folia) {
            entity.getScheduler().runDelayed(plugin, scheduledTask -> task.run(), null, delayTicks);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }
}
