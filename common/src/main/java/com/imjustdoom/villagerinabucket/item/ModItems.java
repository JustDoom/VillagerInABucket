package com.imjustdoom.villagerinabucket.item;

import com.imjustdoom.villagerinabucket.item.custom.VillagerBucket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

public class ModItems {

    public static final Item VILLAGER_IN_A_BUCKET = new VillagerBucket(EntityType.VILLAGER, SoundEvents.VILLAGER_TRADE, new Item.Properties().stacksTo(1));

    public static final Item WANDERING_TRADER_IN_A_BUCKET = new VillagerBucket(EntityType.WANDERING_TRADER, SoundEvents.VILLAGER_TRADE, new Item.Properties().stacksTo(1));
}
