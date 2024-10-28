package com.imjustdoom.villagerinabucket.item;

import com.imjustdoom.villagerinabucket.VillagerInABucket;
import com.imjustdoom.villagerinabucket.item.custom.VillagerBucket;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

public class ModItems {

    public static final Item VILLAGER_IN_A_BUCKET = new VillagerBucket(EntityType.VILLAGER, SoundEvents.VILLAGER_TRADE, new Item.Properties().stacksTo(1).setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(VillagerInABucket.MOD_ID, "villager_in_a_bucket"))));

    public static final Item WANDERING_TRADER_IN_A_BUCKET = new VillagerBucket(EntityType.WANDERING_TRADER, SoundEvents.VILLAGER_TRADE, new Item.Properties().stacksTo(1).setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(VillagerInABucket.MOD_ID, "wandering_trader_in_a_bucket"))));
}
