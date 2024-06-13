package com.imjustdoom.villagerinabucket;

import com.imjustdoom.villagerinabucket.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

import java.util.HashMap;
import java.util.Map;

public class VillagerInABucket {
    public static final String MOD_ID = "villagerinabucket";

    public static final Map<VillagerType, Integer> VILLAGER_DATA_LIST = new HashMap<>() {
        {
            put(VillagerType.DESERT, 1);
            put(VillagerType.SAVANNA, 2);
            put(VillagerType.SNOW, 3);
            put(VillagerType.SWAMP, 4);
        }
    };

    public static void registerDispenserBehaviours() {
        DispenseItemBehavior dispenseItemBehavior = new DefaultDispenseItemBehavior() {
            private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

            public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
                DispensibleContainerItem dispensibleContainerItem = (DispensibleContainerItem) itemStack.getItem();
                BlockPos blockPos = blockSource.pos().relative(blockSource.state().getValue(DispenserBlock.FACING));
                Level level = blockSource.level();
                if (dispensibleContainerItem.emptyContents(null, level, blockPos, null)) {
                    dispensibleContainerItem.checkExtraContent(null, level, itemStack, blockPos);
                    return new ItemStack(Items.BUCKET);
                } else {
                    return this.defaultDispenseItemBehavior.dispense(blockSource, itemStack);
                }
            }
        };

        DispenserBlock.registerBehavior(ModItems.VILLAGER_IN_A_BUCKET, dispenseItemBehavior);
        DispenserBlock.registerBehavior(ModItems.WANDERING_TRADER_IN_A_BUCKET, dispenseItemBehavior);
    }
}
