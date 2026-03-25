package com.imjustdoom.villagerinabucket.folia;

import com.imjustdoom.villagerinabucket.folia.listener.VillagerInteractListener;
import com.imjustdoom.villagerinabucket.folia.util.FoliaUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public class VillagerInABucketPlugin extends JavaPlugin {

    public static NamespacedKey KEY_VILLAGER_BUCKET;
    public static NamespacedKey KEY_ENTITY_TYPE;
    public static NamespacedKey KEY_ENTITY_DATA;

    private boolean enableZombieVillager = true;
    private boolean harmReputation = false;

    @Override
    public void onEnable() {
        KEY_VILLAGER_BUCKET = new NamespacedKey(this, "villager_bucket");
        KEY_ENTITY_TYPE = new NamespacedKey(this, "entity_type");
        KEY_ENTITY_DATA = new NamespacedKey(this, "entity_data");

        saveDefaultConfig();
        enableZombieVillager = getConfig().getBoolean("enable-zombie-villager", true);
        harmReputation = getConfig().getBoolean("harm-reputation", false);

        FoliaUtil.init();

        getServer().getPluginManager().registerEvents(
                new VillagerInteractListener(this), this
        );

        getLogger().info("VillagerInABucket (Folia) enabled! Folia support: " + FoliaUtil.isFolia());
    }

    public boolean isEnableZombieVillager() {
        return enableZombieVillager;
    }

    public boolean isHarmReputation() {
        return harmReputation;
    }
}
