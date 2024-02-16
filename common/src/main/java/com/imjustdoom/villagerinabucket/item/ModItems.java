package com.imjustdoom.villagerinabucket.item;

import com.imjustdoom.villagerinabucket.VillagerInABucket;
import com.imjustdoom.villagerinabucket.item.custom.VillagerBucket;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluids;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(VillagerInABucket.MOD_ID, Registries.ITEM);

    public static final RegistrySupplier<VillagerBucket> VILLAGER_IN_A_BUCKET = ITEMS.register("villager_in_a_bucket", () ->
            new VillagerBucket(EntityType.VILLAGER, SoundEvents.VILLAGER_TRADE, new Item.Properties().stacksTo(1).arch$tab(VillagerInABucket.VILLAGERINABUCKET_TAB)));

    public static void init() {
        ITEMS.register();
    }
}
