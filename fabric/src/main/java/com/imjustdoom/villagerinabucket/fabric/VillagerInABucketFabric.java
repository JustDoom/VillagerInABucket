package com.imjustdoom.villagerinabucket.fabric;

import com.imjustdoom.villagerinabucket.VillagerInABucket;
import com.imjustdoom.villagerinabucket.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class VillagerInABucketFabric implements ModInitializer {

    private static final CreativeModeTab VILLAGERINABUCKET_TAB = FabricItemGroup.builder()
            .icon(() -> new ItemStack(ModItems.VILLAGER_IN_A_BUCKET))
            .title(Component.translatable("category.villagerinabucket.villagerinabucket_tab"))
            .displayItems((params, output) -> VillagerInABucket.registerCreativeTabItems(output))
            .build();

    @Override
    public void onInitialize() {
        VillagerInABucket.init();

        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(VillagerInABucket.MOD_ID, "villager_in_a_bucket"), ModItems.VILLAGER_IN_A_BUCKET);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(VillagerInABucket.MOD_ID, "wandering_trader_in_a_bucket"), ModItems.WANDERING_TRADER_IN_A_BUCKET);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(VillagerInABucket.MOD_ID, "zombie_villager_in_a_bucket"), ModItems.ZOMBIE_VILLAGER_IN_A_BUCKET);

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, new ResourceLocation(VillagerInABucket.MOD_ID, "villagerinabucket_tab"), VILLAGERINABUCKET_TAB);

        VillagerInABucket.registerDispenserBehaviours();
    }
}
