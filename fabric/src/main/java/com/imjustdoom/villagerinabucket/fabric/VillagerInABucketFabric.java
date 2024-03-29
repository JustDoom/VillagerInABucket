package com.imjustdoom.villagerinabucket.fabric;

import com.imjustdoom.villagerinabucket.VillagerInABucket;
import net.fabricmc.api.ModInitializer;

public class VillagerInABucketFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        VillagerInABucket.init();
        VillagerInABucket.registerDispenserBehaviours();
    }
}
