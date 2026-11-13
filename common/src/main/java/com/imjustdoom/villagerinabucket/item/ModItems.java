package com.imjustdoom.villagerinabucket.item;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

public class ModItems {

    public static final Item VILLAGER_IN_A_BUCKET = new VillagerBucketItem(EntityType.VILLAGER, new Item.Properties().stacksTo(1));

    // Legacy items remaining ONLY for backwards compatability
    public static final Item WANDERING_TRADER_IN_A_BUCKET = new VillagerBucketItem(EntityType.WANDERING_TRADER, new Item.Properties().stacksTo(1));
    public static final Item ZOMBIE_VILLAGER_IN_A_BUCKET = new VillagerBucketItem(EntityType.ZOMBIE_VILLAGER, new Item.Properties().stacksTo(1));
}
