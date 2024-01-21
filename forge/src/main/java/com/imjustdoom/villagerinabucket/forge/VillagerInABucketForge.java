package com.imjustdoom.villagerinabucket.forge;

import com.imjustdoom.villagerinabucket.VillagerInABucket;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(VillagerInABucket.MOD_ID)
public class VillagerInABucketForge {
    public VillagerInABucketForge() {

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener((FMLCommonSetupEvent e) -> {
        });

//        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, AxifierConfig.SPEC, "dont-run-with-scissors.toml");

        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(VillagerInABucket.MOD_ID, modBus);

        VillagerInABucket.init();
    }
}
