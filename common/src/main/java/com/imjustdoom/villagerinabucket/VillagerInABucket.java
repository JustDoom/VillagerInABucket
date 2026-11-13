package com.imjustdoom.villagerinabucket;

import com.imjustdoom.villagerinabucket.config.Config;
import com.imjustdoom.villagerinabucket.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

import java.io.IOException;
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

    public static void init() {
        try {
            Config.init();
        } catch (IOException exception) {
            System.err.println("There was an error setting up or saving the config file for Villager In A Bucket :(");
            exception.printStackTrace();
        }
    }

    public static void registerDispenserBehaviours() {
        DispenseItemBehavior dispenseItemBehavior = new DefaultDispenseItemBehavior() {
            private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

            public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
                DispensibleContainerItem dispensibleContainerItem = (DispensibleContainerItem) itemStack.getItem();
                BlockPos blockPos = blockSource.getPos().relative(blockSource.getBlockState().getValue(DispenserBlock.FACING));
                Level level = blockSource.getLevel();
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
        DispenserBlock.registerBehavior(ModItems.ZOMBIE_VILLAGER_IN_A_BUCKET, dispenseItemBehavior);
    }

    public static void registerCreativeTabItems(CreativeModeTab.Output output) {
        // TODO: Try account for modded villager types and add them to the creative menu

        output.accept(ModItems.VILLAGER_IN_A_BUCKET);

        output.accept(createCreativeItemStack(EntityType.WANDERING_TRADER, 5));

        for (VillagerType type : BuiltInRegistries.VILLAGER_TYPE) {
            ItemStack itemStack = createCreativeItemStack(EntityType.VILLAGER, VillagerInABucket.VILLAGER_DATA_LIST.getOrDefault(type, 0));

            VillagerData villagerData = new VillagerData(type, VillagerProfession.NONE, 0);
            VillagerData.CODEC.encodeStart(NbtOps.INSTANCE, villagerData)
                    .resultOrPartial(s -> System.err.println("Oh no something happened, no idea how or what the consequences are. Contact Villager In A Bucket support though"))
                    .ifPresent(tag -> itemStack.getOrCreateTag().put("VillagerData", tag));

            output.accept(itemStack);
        }

        output.accept(createCreativeItemStack(EntityType.ZOMBIE_VILLAGER, 6));
    }

    private static ItemStack createCreativeItemStack(EntityType<?> entityType, int customModelData) {
        ItemStack itemStack = new ItemStack(ModItems.VILLAGER_IN_A_BUCKET);
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putInt("CustomModelData", customModelData);
        tag.putString("id", EntityType.getKey(entityType).toString());
        return itemStack;
    }
}
