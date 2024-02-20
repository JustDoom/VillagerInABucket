package com.imjustdoom.villagerinabucket;

import com.imjustdoom.villagerinabucket.item.ModItems;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class VillagerInABucket {
    public static final String MOD_ID = "villagerinabucket";

    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> VILLAGERINABUCKET_TAB = TABS.register(
            "villagerinabucket_tab",
            () -> {
                CreativeModeTab tab = CreativeTabRegistry.create(
                        Component.translatable("category.villagerinabucket.villagerinabucket_tab"),
                        () -> new ItemStack(ModItems.VILLAGER_IN_A_BUCKET.get())
                );

                return tab;
            }
    );

    public static void init() {
        TABS.register();

        ModItems.init();
    }
}
