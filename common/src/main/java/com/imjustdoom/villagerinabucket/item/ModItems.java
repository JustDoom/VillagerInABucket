package com.imjustdoom.villagerinabucket.item;

import com.imjustdoom.villagerinabucket.VillagerInABucket;
import com.imjustdoom.villagerinabucket.item.custom.VillagerBucket;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.lang.reflect.InvocationTargetException;

public class ModItems {
    public static final ImmutablePair<ResourceKey<Item>, Item> VILLAGER_IN_A_BUCKET = registerBucketItem("villager_in_a_bucket", VillagerBucket.class, EntityType.VILLAGER, SoundEvents.VILLAGER_TRADE, new Item.Properties().stacksTo(1));
    public static final ImmutablePair<ResourceKey<Item>, Item> WANDERING_TRADER_IN_A_BUCKET = registerBucketItem("wandering_trader_in_a_bucket", VillagerBucket.class, EntityType.WANDERING_TRADER, SoundEvents.WANDERING_TRADER_NO, new Item.Properties().stacksTo(1));
    public static final ImmutablePair<ResourceKey<Item>, Item> ZOMBIE_VILLAGER_IN_A_BUCKET = registerBucketItem("zombie_villager_in_a_bucket", VillagerBucket.class, EntityType.ZOMBIE_VILLAGER, SoundEvents.ZOMBIE_VILLAGER_AMBIENT, new Item.Properties().stacksTo(1));

    public static ImmutablePair<ResourceKey<Item>, Item> registerBucketItem(String id, Class<?> clazz, EntityType<?> entityType, SoundEvent soundEvent, Item.Properties properties) {
        try {
            ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(VillagerInABucket.MOD_ID, id));
            return new ImmutablePair<>(key, (Item) clazz.getConstructor(EntityType.class, SoundEvent.class, Item.Properties.class).newInstance(entityType, soundEvent, properties.setId(key)));
        } catch (InvocationTargetException | InstantiationException | NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
