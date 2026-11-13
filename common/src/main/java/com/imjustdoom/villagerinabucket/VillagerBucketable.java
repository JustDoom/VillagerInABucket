package com.imjustdoom.villagerinabucket;

import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.item.ItemStack;

public interface VillagerBucketable extends Bucketable {
    ItemStack createBucketStack();
}
