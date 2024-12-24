package com.imjustdoom.villagerinabucket;

import com.imjustdoom.villagerinabucket.config.Config;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class VillagerInABucket {
    public static final String MOD_ID = "villagerinabucket";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Map<VillagerType, String> VILLAGER_DATA_LIST = new HashMap<>() {
        {
            put(VillagerType.DESERT, "desert");
            put(VillagerType.SAVANNA, "savanna");
            put(VillagerType.SNOW, "snow");
            put(VillagerType.SWAMP, "swamp");
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

        DispenserBlock.registerBehavior(ModItems.VILLAGER_IN_A_BUCKET.right, dispenseItemBehavior);
        DispenserBlock.registerBehavior(ModItems.WANDERING_TRADER_IN_A_BUCKET.right, dispenseItemBehavior);
    }

    public static void init() {
        try {
            Config.init();
        } catch (IOException exception) {
            System.err.println("There was an error setting up or saving the config file for Villager In A Bucket :(");
            exception.printStackTrace();
        }
    }
}
