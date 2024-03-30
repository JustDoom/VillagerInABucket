package com.imjustdoom.villagerinabucket.item;

import com.imjustdoom.villagerinabucket.VillagerInABucket;
import com.imjustdoom.villagerinabucket.item.custom.VillagerBucket;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Registry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(VillagerInABucket.MOD_ID, Registry.ITEM_REGISTRY);

    public static final RegistrySupplier<VillagerBucket> VILLAGER_IN_A_BUCKET = ITEMS.register("villager_in_a_bucket", () ->
            new VillagerBucket(EntityType.VILLAGER, SoundEvents.VILLAGER_TRADE, new Item.Properties().stacksTo(1).tab(VillagerInABucket.VILLAGERINABUCKET_TAB)));

    public static final RegistrySupplier<VillagerBucket> WANDERING_TRADER_IN_A_BUCKET = ITEMS.register("wandering_trader_in_a_bucket", () ->
            new VillagerBucket(EntityType.WANDERING_TRADER, SoundEvents.VILLAGER_TRADE, new Item.Properties().stacksTo(1).tab(VillagerInABucket.VILLAGERINABUCKET_TAB)));

    public static void init() {
        ITEMS.register();
    }
}
